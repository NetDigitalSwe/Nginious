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
 * A subtraction operator which takes two values and subtract them. Below is an
 * example which evaluates to 3.
 * 
 * <pre>
 * 5 - 2
 * </pre>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class SubOperator extends ArithmeticOperator {
	
	/**
	 * Constructs a new subtraction operator with the specified left and right value
	 * arguments.
	 * 
	 * @param value1 the left side value
	 * @param value2 the right side value
	 */
    public SubOperator(Value value1, Value value2) {
    	super(value1, value2);
    }
    
    /**
     * Subtracts the two value arguments and converts the result to an integer.
     * 
     * @return the integer result of subtracting the argument values
     */
    protected int getIntValue() {
    	return value1.getIntValue() - value2.getIntValue();
    }
    
    /**
     * Subtract the two value arguments and convers the result to a double.
     * 
     * @return the double result of subtractin the argument values
     */
    protected double getDoubleValue() {
    	return value1.getDoubleValue() - value2.getDoubleValue();
    }
    
    /**
     * Creates bytecode for subtracting the two value arguments. The bytecode is
     * generated using the specified method visitor. The bytecode produces a result 
     * of specified type.
     * 
     * @param visitor the method visitor
     * @param type the type
     *
     */
    void compile(MethodVisitor visitor, Type type) {
    	type = resolveType(this.value1, this.value2);
    	value1.compile(visitor, type);
    	value2.compile(visitor, type);
    	
    	if(type == Type.DOUBLE) {
    		visitor.visitInsn(Opcodes.DSUB);
    	} else {
    		visitor.visitInsn(Opcodes.ISUB);
    	}
    }
}

