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

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * A boolean operator which evaluates two other values where this operator
 * evaluates to true if at least one of the values evaluate to true.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class OrOperator extends BooleanOperator {
	
	/**
	 * Constructs a new or operator with the specified two values.
	 * 
	 * @param value1 the left side value
	 * @param value2 the right side value
	 */
    public OrOperator(Value value1, Value value2) {
    	super(value1, value2);
    }
    
    /**
     * Evaluates this or operator which returns true if either the left side
     * or the right side value evaluates to <code>true</code>.
     * 
     * @return <code>true</code> if either of the values evaluate to </code>true</code>,
     * 	otherwise <code>false</code>
     */
    protected boolean getBooleanValue() {
    	return value1.getBooleanValue() || value2.getBooleanValue();
    }
    
    /**
     * Creates bytecode for evaluating this or operator. The bytecode is generated using
     * the specified method visitor. The result of the evaluation produces the specified
     * type.
     * 
     * @param visitor the method visitor
     * @param type the type
     */
     void compile(MethodVisitor visitor, Type type) {
		Label labelTrue = new Label();
		Label labelEnd = new Label();
		
		// Test first value
 		value1.compile(visitor, type);
 		visitor.visitInsn(Opcodes.ICONST_1);
		visitor.visitJumpInsn(Opcodes.IF_ICMPEQ, labelTrue);

		// Test second value
 		value2.compile(visitor, type);
 		visitor.visitInsn(Opcodes.ICONST_1);
		visitor.visitJumpInsn(Opcodes.IF_ICMPEQ, labelTrue);
		
		// False
		visitor.visitLdcInsn(false);
		visitor.visitJumpInsn(Opcodes.GOTO, labelEnd);
		
		// True
		visitor.visitLabel(labelTrue);
		visitor.visitLdcInsn(true);
		
		visitor.visitLabel(labelEnd);    	
    }
    
     /**
      * Returns a description of this or operator.
      * 
      * @return description of this or operator
      */
    public String toString() {
    	return "(" + this.value1 + " || " + this.value2 + ")";
    }
}
