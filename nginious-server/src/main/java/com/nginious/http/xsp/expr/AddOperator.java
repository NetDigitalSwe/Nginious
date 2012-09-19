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
import org.objectweb.asm.Opcodes;

/**
 * An add arithmetic operator adds two values.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class AddOperator extends ArithmeticOperator {
	
	/**
	 * Constructs a new add operator with the two specified values.
	 * 
	 * @param value1 left side value
	 * @param value2 right side value
	 */
    public AddOperator(Value value1, Value value2) {
    	super(value1, value2);
    }
    
    /**
     * Evaluates this add operator as an integer which adds the two values.
     * 
     * @return the result of adding the two values
     */
    protected int getIntValue() {
    	return value1.getIntValue() + value2.getIntValue();
    }
    
    /**
     * Evaluates this add operator as a double which adds the two values.
     * 
     * @return the result of adding the two values
     */
    protected double getDoubleValue() {
    	return value1.getDoubleValue() + value2.getDoubleValue();
    }
    
    /**
     * Evaluates this add operator as a boolean which adds the two values.
     * 
     * @return <code>true</code> if result != 0, <code>false</code> otherwise
     */
    protected boolean getBooleanValue() {
    	return getDoubleValue() != 0.0d;
    }
    
    /**
     * Creates bytecode for adding the two values. The bytecode is generated
     * using the specified method visitor.
     * 
     * @param visitor the method visitor for generating bytecode
     * @param type the type to when generating bytecode for adding the values
     */
    void compile(MethodVisitor visitor, Type type) {
    	type = resolveType(this.value1, this.value2);
    	value1.compile(visitor, type);
    	value2.compile(visitor, type);
    	
    	if(type == Type.DOUBLE) {
    		visitor.visitInsn(Opcodes.DADD);
    	} else {
    		visitor.visitInsn(Opcodes.IADD);
    	}
    }
    
    /**
     * Returns a description of this add operator.
     * 
     * @return a description of this add operator
     */
    public String toString() {
    	return "(" + this.value1 + " + " + this.value2 + ")";
    }
}

