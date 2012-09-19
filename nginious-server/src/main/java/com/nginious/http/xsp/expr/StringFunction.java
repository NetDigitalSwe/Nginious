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

package com.nginious.http.xsp.expr;

/**
 * A string function operates on a string argument and produces a string as result.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public abstract class StringFunction extends Value {
	
	protected String name;
	
	protected Value value;
	
	/**
	 * Constructs a new string function with the specified name and string 
	 * argument value.
	 * 
	 * @param name the function name
	 * @param value the argument value
	 */
	protected StringFunction(String name, Value value) {
		super();
		this.name = name;
		this.value = value;
	}
	
	/**
	 * Returns the name of this function.
	 * 
	 * @return the function name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Returns the result type produced by this function, {@link Type#STRING}.
	 * 
	 * @return the result type for this function
	 */
    protected Type getType() {
    	return Type.STRING;
    }
    
    /**
     * Evaluates this string function.
     * 
     * @return the result of this function
     */
    protected Value evaluate() {
    	String value = getStringValue();
    	
    	if(value != null) {
    		return new StringValue(value);
    	}
    	
    	return new NullValue();
    }
    
    /**
     * Evaluates this string function and converts the result to an integer.
     * 
     * @return the integer result of this function
     */
    protected int getIntValue() {
    	String value = getStringValue();
    	
    	if(value == null) {
    		return 0;
    	}
    	
    	return Integer.parseInt(value);
    }
    
    /**
     * Evaluates this string function and converts the result to a double.
     * 
     * @return the double result of this function
     */
    protected double getDoubleValue() {
    	String value = getStringValue();
    	
    	if(value == null) {
    		return 0.0d;
    	}
    	
        return Double.parseDouble(value);
    }
    
    /**
     * Evaluates this string function and converts the result to a boolean.
     * 
     * @return <code>true</code> if the string result value contains a true value,
     * 	otherwise <code>false</code>
     */
    protected boolean getBooleanValue() {
    	String value = getStringValue();
    	
    	if(value == null) {
    		return false;
    	}
    	
    	return Boolean.parseBoolean(value);
    }
}
