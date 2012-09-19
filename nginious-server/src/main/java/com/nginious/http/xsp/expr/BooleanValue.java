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
 * A value of type boolean.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class BooleanValue extends Value {

    private boolean value;

    /**
     * Constructs a new boolean value with the specified value.
     * 
     * @param value <code>true</code> or <code>false</code>
     */
    public BooleanValue(boolean value) {
        this.value = value;
    }
    
    /**
     * Returns this values type.
     * 
     * @return the type for this value
     */
    protected Type getType() {
    	return Type.BOOLEAN;
    }
    
    /**
     * Evaluates this value.
     * 
     * @return result of evaluation
     */
    protected Value evaluate() {
        return this;
    }
    
    /**
     * Returns this boolean value as an integer.
     * 
     * @return <code>1</code> if this boolean is <code>true</code>, <code>0</code> otherwise
     */
    protected int getIntValue() {
    	return this.value ? 1 : 0;
    }
    
    /**
     * Returns this boolean value as a double.
     * 
     * @return <code>1.0d</code> if this boolean is <code>true</code>, <code>0.0d</code> otherwise
     */
    protected double getDoubleValue() {
        return this.value ? 1.0d : 0.0d;
    }
    
    /**
     * Returns this boolean value as a string.
     * 
     * @return this boolean value as a string
     */
    protected String getStringValue() {
        return Boolean.toString(this.value);
    }
    
    /**
     * Returns this boolean value.
     * 
     * @return this boolean value
     */
    protected boolean getBooleanValue() {
    	return this.value;
    }
    
    /**
     * Creates bytecode for evaluating this boolean value. Bytecode is create
     * using the specified method visitor. This value is converted to the
     * specified type if needed.
     * 
     * @param visitor the method visitor
     * @param type the type
     */
    void compile(MethodVisitor visitor, Type type) {
    	if(type == Type.DOUBLE) {
    		visitor.visitLdcInsn(this.value ? 1.0d : 0.0d);
    	} else if(type == Type.INT) {
    		visitor.visitLdcInsn(this.value ? 1 : 0);
    	} else if(type == Type.BOOLEAN) {
    		visitor.visitLdcInsn(this.value);
    	}
    }
}
