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
 * Calculates the power of the first argument value raised to the power of the
 * second argument value.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class PowFunction extends Function {

    private Value value1;

    private Value value2;
    
    /**
     * Constructs a new power function with the specified argument values.
     * 
     * @param value1 the first argument value
     * @param value2 the second argument value
     */
    public PowFunction(Value value1, Value value2) {
        super();
        this.value1 = value1;
        this.value2 = value2;
    }
    
    /**
     * Returns the result type for this function, {@link Type#DOUBLE}.
     * 
     * @return the result type
     */
    protected Type getType() {
    	return Type.DOUBLE;
    }
    
    /**
     * Evaluates this function by calculating the power of the first argument
     * value raised to the power of the second argument value.
     * 
     * @return the result of this function.
     */
    protected Value evaluate() {
        return new DoubleValue(getDoubleValue());
    }
    
    /**
     * Evaluates this function by calculating the power of the first argument
     * value raised to the power of the second argument value. The result is converted
     * to an integer.
     * 
     * @return the integer result of this function
     */
    protected int getIntValue() {
    	return (int)getDoubleValue();
    }
    
    /**
     * Evaluates this function by calculating the power of the first argument
     * value raised to the power of the second argument value. The result is converted
     * to a double.
     * 
     * @return the double result of this function
     */
    protected double getDoubleValue() {
        return Math.pow(value1.getDoubleValue(), value2.getDoubleValue());
    }
    
    /**
     * Evaluates this function by calculating the power of the first argument
     * value raised to the power of the second argument value. The result is converted
     * to a string.
     * 
     * @return the string result of this function
     */
    protected String getStringValue() {
        return Double.toString(getDoubleValue());
    }
    
    /**
     * Evaluates this function by calculating the power of the first argument
     * value raised to the power of the second argument value. The result is converted
     * to a boolean.
     * 
     * @return <code>true</code> if the result != 0.0d, <code>false</code> otherwise
     */
    protected boolean getBooleanValue() {
    	return getDoubleValue() != 0.0d;
    }
    
    /**
     * Creates bytecode for evaluating this function. The specified method visitor is
     * used for creating bytecode.
     * 
     * @param visitor the methos visitor
     * @param type the type
     */
    void compile(MethodVisitor visitor, Type type) {
    	value1.compile(visitor, Type.DOUBLE);
    	value2.compile(visitor, Type.DOUBLE);
    	visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "pow", "(DD)D");
    }
}
