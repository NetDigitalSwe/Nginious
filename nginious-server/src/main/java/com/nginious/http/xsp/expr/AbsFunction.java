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
 * Calculates the absolute value of an argument value
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class AbsFunction extends MathFunction {
	
	/**
	 * Constructs a new absolute function with the specified argument value.
	 * 
	 * @param value the argument value
	 */
    public AbsFunction(Value value) {
        super("abs", value);
    }
    
    /**
     * Returns the return type for this function.
     * 
     * @return the return type
     */
    protected Type getType() {
    	if(value.getType() == Type.ANY){
    		return Type.DOUBLE;
    	}
    	
    	return value.getType();
    }
    
    /**
     * Calculates the absolute value of this functions argument value.
     * 
     * @return the result of this function
     */
    protected Value evaluate() {
    	if(value.getType() == Type.INT) {
    		return new IntegerValue(getIntValue());
    	}
        
    	return new DoubleValue(getDoubleValue());
    }
    
    /**
     * Calculates the absolute value of this functions argument value. The
     * result is converted to an integer.
     * 
     * @return the integer result of this function
     */
    protected int getIntValue() {
    	return Math.abs(value.getIntValue());
    }
    
    /**
     * Calculates the absolute value of this functions argument value. The result
     * is converted to a double.
     * 
     * @return the double result of this function
     */
    protected double getDoubleValue() {
        return Math.abs(value.getDoubleValue());
    }
    
    /**
     * Calculates the absolute value of this functions argument value. The result
     * is converted to a string.
     * 
     * @return the string result of this function
     */
    protected String getStringValue() {
    	if(value.getType() == Type.INT) {
    		return Integer.toString(getIntValue());
    	}
        
    	return Double.toString(getDoubleValue());
    }
        
    /**
     * Creates bytecode for evaluating this function. Bytecode is generated using
     * the specified method visitor which evaluates this value with the
     * specified type as result.
     * 
     * @param visitor the method visitor
     * @param type the type
     */
    void compile(MethodVisitor visitor, Type type) {
    	if(value.getType() == Type.DOUBLE || value.getType() == Type.ANY) {
    		value.compile(visitor, Type.DOUBLE);
    		visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "abs", "(D)D");
    	} else {
    		value.compile(visitor, Type.INT);
    		visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "abs", "(I)I");    		
    	}
    }
}
