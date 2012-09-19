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
 * A mathematical function which maps to a statis method in class {@link java.lang.Math}.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public abstract class MathFunction extends Value {
	
	protected String name;
	
	protected Value value;
	
	/**
	 * Constructs a new math function with the specified method name and value to
	 * evaluate.
	 * 
	 * @param name the function name
	 * @param value the value
	 */
	protected MathFunction(String name, Value value) {
		super();
		this.name = name;
		this.value = value;
	}
	
	/**
	 * The function name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Returns type of value that this function produces when evaluated, {@link Type#DOUBLE}.
	 * 
	 * @return the value type
	 */
    protected Type getType() {
    	return Type.DOUBLE;
    }
    
    /**
     * Evaluates this function.
     * 
     * @return the result value
     */
    protected Value evaluate() {
        return new DoubleValue(getDoubleValue());
    }
    
    /**
     * Evaluates this function and converts the result to an integer.
     * 
     * @return the integer result of evaluating this function
     */
    protected int getIntValue() {
    	return (int)getDoubleValue();
    }
    
    /**
     * Evaluates this function and converts the result to a string.
     * 
     * @return the string result of evaluating this function
     */
    protected String getStringValue() {
        return Double.toString(getDoubleValue());
    }
    
    /**
     * Evaluates this function and converts the result to a boolean.
     * 
     * @return the boolean result of evaluating this function
     */
    protected boolean getBooleanValue() {
    	return getDoubleValue() != 0.0d;
    }
    
    /**
     * Creates bytecode for evaluating this function. The specified method visitor is used
     * for generating bytecode.
     * 
     * @param visitor the method visitor
     * @param type the type
     */
    void compile(MethodVisitor visitor, Type type) {
    	value.compile(visitor, Type.DOUBLE);
    	visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", getName(), "(D)D");
     }
}
