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
 * An arithmetic operator calculates its value using two values and a
 * arithmetic operator when evaluated.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public abstract class ArithmeticOperator extends Value {
	
	protected Value value1;
	
	protected Value value2;
	
	/**
	 * Constructs a new arithmetic operator with the two specified values.
	 * 
	 * @param value1 left side value
	 * @param value2 right side value
	 */
	public ArithmeticOperator(Value value1, Value value2) {
		this.value1 = value1;
		this.value2 = value2;
	}
	
	/**
	 * Resolves the type for this arithmetic operator.
	 * 
	 * @return the type
	 * @see Value#resolveType(Value, Value)
	 */
    protected Type getType() {
    	return resolveType(this.value1, this.value2);
    }
    
    /**
     * Evaluates this arithmetic operator.
     * 
     * @return the result value
     */
    protected Value evaluate() {
    	Type type = getType();
    	Value returnValue = null;
    	
    	switch(type) {
    	case DOUBLE:
    		returnValue = new DoubleValue(getDoubleValue());
    		break;
    		
    	case INT:
    		returnValue = new IntegerValue(getIntValue());
    		break;
    	}
    	
    	return returnValue;
    }
    
    /**
     * Evaluates this arithmetic operator as a string.
     * 
     * @return a result value as a string
     */
    protected String getStringValue() {
    	Type type = getType();
    	String value = null;
    	
    	switch(type) {
    	case DOUBLE:
    		value = Double.toString(getDoubleValue());
    		break;
    		
    	case INT:
    		value = Integer.toString(getIntValue());
    		break;
    	}
    	
    	return value;
    }
    
    /**
     * Evaluates this arithmetic operator as a boolean.
     * 
     * @return <code>true</code> if result value != 0, <code>false</code> otherwise
     */
    protected boolean getBooleanValue() {
    	return getDoubleValue() != 0.0d;    	
    }
}
