/**
 * Copyright 2012 NetDigital Sweden AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.nginious.http.cmd;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Manages mapping between a set method in a bean and a command line argument. A command line argument is built
 * up from bean introspection where all set methods are searched for {@link CommandLine} annotations
 * which describe the command line arguments for a set method.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
class CommandLineArgument {
	
	private String longName;
	
	private String shortName;
	
	private String description;
	
	private boolean mandatory;
	
	private boolean set;
	
	private Object defaultValue;
	
	private Method getMethod;
	
	private Method setMethod;
	
	private Object target;
	
	/**
	 * Constructs a new command line argument for the specified set and get methods found in the specified target bean.
	 * The provided command line mapping contains information about command line options that maps to the specified set
	 * method.
	 * 
	 * @param mapping the command line mapping
	 * @param getMethod the bean get method
	 * @param setMethod the bean set method
	 * @param target the target bean
	 */
	CommandLineArgument(CommandLine mapping, Method getMethod, Method setMethod, Object target) {
		try {
			this.longName = mapping.longName();
			this.shortName = mapping.shortName();
			this.description = mapping.description();
			this.mandatory = mapping.mandatory();
			this.getMethod = getMethod;
			this.setMethod = setMethod;
			this.defaultValue = getMethod.invoke(target);
			Class<?>[] parameters = setMethod.getParameterTypes();
			
			if(parameters.length != 1 || !parameters[0].equals(getMethod.getReturnType())) {
				String property = CommandLineArguments.createPropertyNameFromMethodName(getMethod.getName());
				throw new RuntimeException("Get method doesn't match set method for property '" + property + "'");
			}
			
			this.target = target;
		} catch(Exception e) {
			String property = CommandLineArguments.createPropertyNameFromMethodName(getMethod.getName());
			throw new RuntimeException("Can't get default value for property '" + property + "'", e);
		}
	}
	
	/**
	 * Returns whether or not setting of this command line argument is mandatory.
	 * 
	 * @return <code>true</code> if mandatory, <code>false</code> otherwise
	 */
	boolean isMandatory() {
		return this.mandatory;
	}
	
	/**
	 * Returns whether or not this command line argument has been set.
	 * 
	 * @return <code>true</code> if set, <code>false</code> otherwise
	 */
	boolean isSet() {
		return this.set;
	}
	
	/**
	 * Returns default value for this command line argument. The default value is returned by the get method
	 * that this command line argument is assigned to.
	 * 
	 * @return the default value
	 */
	Object getDefaultValue() {
		return this.defaultValue;
	}
	
	/**
	 * Returns long form name for this command line argument.
	 * 
	 * @return the long form name
	 */
	String getLongName() {
		return this.longName;
	}
	
	/**
	 * Returns short form name for this command line argument.
	 * 
	 * @return the short form name
	 */
	String getShortName() {
		return this.shortName;
	}
	
	/**
	 * Returns description for this command line argument.
	 * 
	 * @return the description
	 */
	String getDescription() {
		return this.description;
	}
	
	/**
	 * Returns whether or not this command line argument expects a value. If the set method that this command line
	 * argument is assigned to expects a boolean then a value is not expected for the command line argument.
	 * 
	 * @return <code>true</code> if a value is expected, <code>false</code> otherwise
	 */
	boolean expectsValue() {
		return !getMethod.getReturnType().equals(Boolean.class) && !getMethod.getReturnType().equals(boolean.class);
	}
	
	/**
	 * Sets value to the specified possible value and calls the bean set method for this command line
	 * argument.
	 * 
	 * @param possibleValue the value
	 * @return <code>true</code> if value was used, <code>false</code> otherwise
	 * @throws CommandLineException if possible value is invalid
	 */
	boolean setShortFormValue(String possibleValue) throws CommandLineException {
		if(possibleValue == null && expectsValue()) {
			throw new CommandLineException("Argument " + this.shortName + " requires a value");
		}
		
		return setValue(possibleValue, "-" + this.shortName);
	}
	
	/**
	 * Sets value to the specified possible value and calls the bean set method for this command line
	 * argument.
	 * 
	 * @param possibleValue the value
	 * @return <code>true</code> if value was used, <code>false</code> otherwise
	 * @throws CommandLineException if possible value is invalid
	 */
	boolean setLongFormValue(String possibleValue) throws CommandLineException {
		if(possibleValue == null && expectsValue()) {
			throw new CommandLineException("Argument " + this.longName + " requires a value");
		}
		
		return setValue(possibleValue, "--" + this.longName);
	}
	
	/**
	 * Calls set method in bean with specified possible value. The value is converted to the
	 * appropriate type based on bean set method introspection.
	 * 
	 * @param possibleValue the value
	 * @param argument the argument name
	 * @return <code>true</code> if value was used, <code>false</code> otherwise
	 * @throws CommandLineException if unable to call set method in bean
	 */
	private boolean setValue(String possibleValue, String argument) throws CommandLineException {
		Class<?> clazzType = getMethod.getReturnType();
		ArgumentType type = ArgumentType.getType(clazzType);
		boolean valueUsed = true;
		
		try {
			switch(type) {
			case BYTE:
				setByteValue(possibleValue, argument);
				break;
				
			case SHORT:
				setShortValue(possibleValue, argument);
				break;
				
			case INTEGER:
				setIntegerValue(possibleValue, argument);
				break;
				
			case LONG:
				setLongValue(possibleValue, argument);
				break;
				
			case FLOAT:
				setFloatValue(possibleValue, argument);
				break;
				
			case DOUBLE:
				setDoubleValue(possibleValue, argument);
				break;
				
			case STRING:
				setStringValue(possibleValue);
				break;
				
			case BOOLEAN:
				setBooleanValue();
				valueUsed = false;
				break;
			}
		} catch(IllegalAccessException e) {
			String property = CommandLineArguments.createPropertyNameFromMethodName(setMethod.getName());
			throw new RuntimeException("Can't set value for property '" + property + "'", e);
		} catch(InvocationTargetException e) {
			String property = CommandLineArguments.createPropertyNameFromMethodName(setMethod.getName());
			throw new RuntimeException("Can't set value for property '" + property + "'", e);			
		}
		
		this.set = true;
		return valueUsed;
	}
	
	/**
	 * Converts the specified possible value to a byte then calls the beans set method.
	 * 
	 * @param possibleValue the value
	 * @param argument the argument name
	 * @throws CommandLineException if unable to convert value to a byte
	 * @throws InvocationTargetException if bean set method call fails
	 * @throws IllegalAccessException if bean set method call fails
	 */
	private void setByteValue(String possibleValue, String argument) throws CommandLineException, InvocationTargetException, IllegalAccessException {
		try {
			byte byteValue = Byte.parseByte(possibleValue);
			setMethod.invoke(this.target, byteValue);
		} catch(NumberFormatException e) {
			throw new CommandLineException("Can't convert '" + possibleValue + "' to a byte for argument '" + argument + "'", e);			
		}
	}
	
	/**
	 * Converts the specified possible value to a short then calls the beans set method.
	 * 
	 * @param possibleValue the value
	 * @param argument the argument name
	 * @throws CommandLineException if unable to convert value to a short
	 * @throws InvocationTargetException if bean set method call fails
	 * @throws IllegalAccessException if bean set method call fails
	 */
	private void setShortValue(String possibleValue, String argument)  throws CommandLineException, InvocationTargetException, IllegalAccessException {
		try {
			short shortValue = Short.parseShort(possibleValue);
			setMethod.invoke(this.target, shortValue);
		} catch(NumberFormatException e) {
			throw new CommandLineException("Can't convert '" + possibleValue + "' to a short for argument '" + argument + "'", e);			
		}
	}
	
	/**
	 * Converts the specified possible value to an integer then calls the beans set method.
	 * 
	 * @param possibleValue the value
	 * @param argument the argument name
	 * @throws CommandLineException if unable to convert value to an integer
	 * @throws InvocationTargetException if bean set method call fails
	 * @throws IllegalAccessException if bean set method call fails
	 */
	private void setIntegerValue(String possibleValue, String argument) throws CommandLineException, InvocationTargetException, IllegalAccessException {
		try {
			int intValue = Integer.parseInt(possibleValue);
			setMethod.invoke(this.target, intValue);
		} catch(NumberFormatException e) {
			throw new CommandLineException("Can't convert '" + possibleValue + "' to a int for argument '" + argument + "'", e);			
		}
	}
	
	/**
	 * Converts the specified possible value to a long then calls the beans set method.
	 * 
	 * @param possibleValue the value
	 * @param argument the argument name
	 * @throws CommandLineException if unable to convert value to a long
	 * @throws InvocationTargetException if bean set method call fails
	 * @throws IllegalAccessException if bean set method call fails
	 */
	private void setLongValue(String possibleValue, String argument) throws CommandLineException, InvocationTargetException, IllegalAccessException {
		try {
			long longValue = Long.parseLong(possibleValue);
			setMethod.invoke(this.target, longValue);
		} catch(NumberFormatException e) {
			throw new CommandLineException("Can't convert '" + possibleValue + "' to a long for argument '" + argument + "'", e);			
		}
	}
	
	/**
	 * Converts the specified possible value to a float then calls the beans set method.
	 * 
	 * @param possibleValue the value
	 * @param argument the argument name
	 * @throws CommandLineException if unable to convert value to a float
	 * @throws InvocationTargetException if bean set method call fails
	 * @throws IllegalAccessException if bean set method call fails
	 */
	private void setFloatValue(String possibleValue, String argument) throws CommandLineException, InvocationTargetException, IllegalAccessException {
		try {
			float floatValue = Float.parseFloat(possibleValue);
			setMethod.invoke(this.target, floatValue);
		} catch(NumberFormatException e) {
			throw new CommandLineException("Can't convert '" + possibleValue + "' to a float for argument '" + argument + "'", e);			
		}
	}
	
	/**
	 * Converts the specified possible value to a double then calls the beans set method.
	 * 
	 * @param possibleValue the value
	 * @param argument the argument name
	 * @throws CommandLineException if unable to convert value to a double
	 * @throws InvocationTargetException if bean set method call fails
	 * @throws IllegalAccessException if bean set method call fails
	 */
	private void setDoubleValue(String possibleValue, String argument) throws CommandLineException, InvocationTargetException, IllegalAccessException {
		try {
			double doubleValue = Double.parseDouble(possibleValue);
			setMethod.invoke(this.target, doubleValue);
		} catch(NumberFormatException e) {
			throw new CommandLineException("Can't convert '" + possibleValue + "' to a double for argument '" + argument + "'", e);
		}
	}
	
	/**
	 * Calls the beans set method with the specified possible value.
	 * 
	 * @param possibleValue the value
	 * @throws InvocationTargetException if bean set method call fails
	 * @throws IllegalAccessException if bean set method call fails
	 */
	private void setStringValue(String possibleValue) throws InvocationTargetException, IllegalAccessException {
		setMethod.invoke(this.target, possibleValue);		
	}
	
	/**
	 * Calls the beans set method with a <code>true</code> value.
	 *  
	 * @throws InvocationTargetException if bean set method call fails
	 * @throws IllegalAccessException if bean set method call fails
	 */
	private void setBooleanValue() throws InvocationTargetException, IllegalAccessException {
		setMethod.invoke(this.target, true);		
	}
}
