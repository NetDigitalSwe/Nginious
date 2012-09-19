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
 * A value of type integer.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class IntegerValue extends Value {

    private int value;
    
    /**
     * Constructs a new integer value with the specified value
     * 
     * @param value the integer value
     */
    public IntegerValue(int value) {
        this.value = value;
    }
    
    /**
     * Returns this values type, {@link Type#INT}.
     * 
     * @return this values type
     */
    protected Type getType() {
    	return Type.INT;
    }
    
    /**
     * Evaluates this integer value.
     * 
     * @return this value
     */
    protected Value evaluate() {
        return this;
    }
    
    /**
     * Evaluates this value as an integer.
     * 
     * @return this values integer value
     */
    protected int getIntValue() {
    	return this.value;
    }
    
    /**
     * Evaluates this integer value as a double.
     * 
     * @return this value converted to a double
     */
    protected double getDoubleValue() {
        return (double)this.value;
    }

    /**
     * Evaluates this integer value as a string.
     * 
     * @return this value converted to a string
     */
    protected String getStringValue() {
        return Integer.toString(value);
    }
    
    /**
     * Evaluates this integer values as a boolean.
     * 
     * @return <code>true</code> if this integer value is not <code>0</code>,
     * 	<code>false</code> if this integer value is <code>0</code>
     */
    protected boolean getBooleanValue() {
    	return this.value != 0;
    }
    
    /**
     * Creates bytecode for evaluating this value. Bytecode is generated using
     * the specified method visitor which evaluates this value with the
     * specified type as result.
     * 
     * @param visitor the method visitor
     * @param type the type
     */
    void compile(MethodVisitor visitor, Type type) {
    	if(type == Type.DOUBLE) {
    		visitor.visitLdcInsn((double)this.value);
    	} else if(type == Type.INT) {
    		visitor.visitLdcInsn(this.value);
    	}
    }
    
    /**
     * Returns a description of this integer value.
     * 
     * @return a description of this integer value
     */
    public String toString() {
    	return Integer.toString(this.value);
    }
}
