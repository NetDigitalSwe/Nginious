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
 * A boolean operator produces a boolean value as result when evaluated.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public abstract class BooleanOperator extends Value {
	
	protected Value value1;
	
	protected Value value2;
	
	/**
	 * Constructs a new boolean operator from the specified values.
	 * 
	 * @param value1 first value
	 * @param value2 second value
	 */
	public BooleanOperator(Value value1, Value value2) {
		this.value1 = value1;
		this.value2 = value2;
	}
	
	/**
	 * Returns this operators type.
	 * 
	 * @return this operators type
	 * @see Type
	 */
    protected Type getType() {
    	return Type.BOOLEAN;
    }
    
    /**
     * Evaluates this boolean operator and returns a double value as result.
     * 
     * @return a double value with <code>1.0d</code> if this boolean operator evaluates to
     * 	<code>true</code>, otherwise <code>0.0d</code>
     */
    protected Value evaluate() {
        return new DoubleValue(getDoubleValue());
    }
    
    /**
     * Evaluates this boolean operator and returns an integer value as result.
     * 
     * @return <a integer value with <code>1</code> if this boolean operator evaluates to
     * 	<code>true</code>, otherwise <code>0</code>
     */
    protected int getIntValue() {
    	return getBooleanValue() ? 1 : 0;
    }
    
    /**
     * Evaluates this boolean operator and returns a double value as result.
     * 
     * @return a double value with <code>1.0d</code> if this boolean operator evaluates to
     * 	<code>true</code>, otherwise <code>0.0d</code>
     */
    protected double getDoubleValue() {
    	return getBooleanValue() ? 1.0d : 0.0d;
    }
    
    /**
     * Evaluates this boolean operator and returns a string value as result.
     * 
     * @return a string value with <code>true</code> if this boolean operator evaluates to
     * 	<code>true</code>, otherwise <code>false</code>
     */
    protected String getStringValue() {
    	return Boolean.toString(getBooleanValue());
    }
}
