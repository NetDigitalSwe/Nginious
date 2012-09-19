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
 * A modulo operator which calculates the modulo of the left side argument value to
 * the right side argument value.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class ModOperator extends ArithmeticOperator {
	
	/**
	 * Constructs a new modulo operator with the two specified argument
	 * values.
	 * 
	 * @param value1 the left side argument value
	 * @param value2 the right side argument value
	 */
    public ModOperator(Value value1, Value value2) {
    	super(value1, value2);
    }
    
    /**
     * Calculates the modulo of the left side argument value to the right
     * side argument value and returns the result as an integer.
     * 
     * @return the result of this operator
     */
    protected int getIntValue() {
    	return value1.getIntValue() % value2.getIntValue();
    }
    
    /**
     * Calculates the modulo of the left side argument value to the right
     * side argument value and returns the result as a double.
     * 
     * @return the result of this operator
     */
    protected double getDoubleValue() {
    	return value1.getDoubleValue() % value2.getDoubleValue();
    }

    /**
     * Creats bytecode for calculating the result for this operator. The bytecode
     * is generated using the specified method visitor. The result is generated
     * as the specified type.
     * 
     * @param visitor the method visitor
     * @param type the type
     */
    void compile(MethodVisitor visitor, Type type) {
    	type = resolveType(this.value1, this.value2);
    	value1.compile(visitor, type);
    	value2.compile(visitor, type);
    	
    	if(type == Type.DOUBLE) {
    		visitor.visitInsn(Opcodes.DREM);
    	} else {
    		visitor.visitInsn(Opcodes.IREM);
    	}
    }
}

