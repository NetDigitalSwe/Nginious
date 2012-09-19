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
 * Compares two argument values and produces a boolean result of the
 * comparison.
 * 
 * @author Bojan Pisler, NetDigital Sweden
 *
 */
public abstract class ComparisonOperator extends Value {

	protected Value value1;

    protected Value value2;
    
    /**
     * Constructs a new comparison operator with the specified two argument
     * values.
     * 
     * @param value1 the left side argument value
     * @param value2 the right side argument value
     */
    protected ComparisonOperator(Value value1, Value value2) {
    	this.value1 = value1;
    	this.value2 = value2;
    }
    
    /**
     * Returns the return type for this operator, {@link Type#BOOLEAN}.
     * 
     * @return the return type for this operator
     */
    protected Type getType() {
    	return Type.BOOLEAN;
    }
    
    /**
     * Evaluates this comparison operator.
     * 
     * @return <code>true</code> if comparison results in true, <code>false</code> otherwise
     */
    protected Value evaluate() {
        return new BooleanValue(getBooleanValue());
    }
    
    /**
     * Evaluates this comparison operator and converts the result to an integer.
     * 
     * @return <code>1</code> if the comparison results in true, <code>0</code> otherwise
     */
    protected int getIntValue() {
    	return getBooleanValue() ? 1 : 0;
    }
    
    /**
     * Evaluates this comparison operator and converts the result to a double.
     * 
     * @return <code>1.0d</code> if the comparison results in true, <code>0.0d</code> otherwise
     */
    protected double getDoubleValue() {
    	return getBooleanValue() ? 1.0d : 0.0d;
    }

    /**
     * Evaluates this comparison operator and converts the result to a string.
     * 
     * @return <code>"true"</code> if the comparison results in true, <code>"false"</code> otherwise
     */
    protected String getStringValue() {
    	return Boolean.toString(getBooleanValue());
    }    
}
