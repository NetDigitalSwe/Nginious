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
 * A division operator which divides the first argument value with
 * the second.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class DivOperator extends ArithmeticOperator {

	/**
	 * Constructs a new division operator with the specified two
	 * argument values.
	 * 
	 * @param value1 the first argument value
	 * @param value2 the second argument value
	 */
    public DivOperator(Value value1, Value value2) {
    	super(value1, value2);
    }
    
    /**
     * Evaluates this division operator by dividin the first
     * argument value with the second. The result is converted to an
     * integer.
     * 
     * @return the integer result of this operator
     */
     protected int getIntValue() {
    	return value1.getIntValue() / value2.getIntValue();
    }
    
     /**
      * Evaluates this division operator by dividing the first
      * argument value with the second. The result is converted to a
      * double.
      * 
      * @return the double result of this operator
      */
      protected double getDoubleValue() {
        return value1.getDoubleValue() / value2.getDoubleValue();
    }

      /**
       * Creates bytecode for evaluating this operator. The bytecode is created
       * using the specified method visitor. The bytecode produces a result
       * of the specified type.
       * 
       * @param visitor the method visitor
       * @param type the result type
       */
    void compile(MethodVisitor visitor, Type type) {
    	type = resolveType(this.value1, this.value2);
    	value1.compile(visitor, type);
    	value2.compile(visitor, type);
    	
    	if(type == Type.DOUBLE) {
    		visitor.visitInsn(Opcodes.DDIV);
    	} else {
    		visitor.visitInsn(Opcodes.IDIV);
    	}
    }
}

