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

import org.objectweb.asm.MethodVisitor;

/**
 * A value of type double.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class DoubleValue extends Value {

    private double value;
    
    /**
     * Constructs a new double value with the specified double.
     * 
     * @param value the double value
     */
    public DoubleValue(double value) {
        this.value = value;
    }
    
    /**
     * Returns this values type, {@link Type#DOUBLE}.
     * 
     * @return this values type
     */
    protected Type getType() {
    	return Type.DOUBLE;
    }
    
    /**
     * Evaluates this double value.
     * 
     * @return result of evaluating this value
     */
    protected Value evaluate() {
        return this;
    }
    
    /**
     * Evaluates this double value as an integer.
     * 
     * @return this value converted to an integer
     */
    protected int getIntValue() {
    	return (int)this.value;
    }
    
    /**
     * Evaluates this double value.
     * 
     * @return this values double value
     */
    protected double getDoubleValue() {
        return this.value;
    }
    
    /**
     * Evaluates this double value as a string.
     * 
     * @return this value converted to a string
     */
    protected String getStringValue() {
        return Double.toString(this.value);
    }
    
    /**
     * Evaluates this double value as a boolean.
     * 
     * @return <code>true</code> if this double value is not <code>0.0d</code>, <code>false</code>
     * 	if this value is <code>0.0d</code>
     */
    protected boolean getBooleanValue() {
    	return this.value != 0.0d;
    }
    
    /**
     * Creates bytecode for evaluating this double value. The specified method visitor
     * is used for creating bytecode which evaluates this value with the specified
     * type as result.
     * 
     * @param visitor the methods visitor
     * @param type the type
     */
    void compile(MethodVisitor visitor, Type type) {
    	if(type == Type.DOUBLE) {
    		visitor.visitLdcInsn(this.value);
    	} else if(type == Type.INT) {
    		visitor.visitLdcInsn((int)this.value);
    	}
    }
}
