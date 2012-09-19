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
 * A boolean operator which evaluates two other values. This operator evaluates to
 * true if both of the values evaluate to true.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class AndOperator extends BooleanOperator {
	
	/**
	 * Constructs a new and operator with the two specified values.
	 * 
	 * @param value1 the left side value
	 * @param value2 the right side value
	 */
    public AndOperator(Value value1, Value value2) {
    	super(value1, value2);
    }
    
    /**
     * Evaluates this and operator which returns true if both the left side
     * and the right side value evaluates to <code>true</code>.
     * 
     * @return <code>true</code> if both values evaluate to </code>true</code>,
     * 	otherwise <code>false</code>
     */
    protected boolean getBooleanValue() {
    	return value1.getBooleanValue() && value2.getBooleanValue();
    }
    
    /**
     * Creates bytecode for evaluating this and operator. The bytecode is generated using
     * the specified method visitor. The result of the evaluation produces the specified
     * type.
     * 
     * @param visitor the method visitor
     * @param type the type
     */
    void compile(MethodVisitor visitor, Type type) {
		Label labelFalse = new Label();
		Label labelEnd = new Label();
		
		// Test first value
 		value1.compile(visitor, type);
 		visitor.visitInsn(Opcodes.ICONST_1);
		visitor.visitJumpInsn(Opcodes.IF_ICMPNE, labelFalse);
		
		// Test second value
    	value2.compile(visitor, type);
 		visitor.visitInsn(Opcodes.ICONST_1);
		visitor.visitJumpInsn(Opcodes.IF_ICMPNE, labelFalse);
		
		// True
		visitor.visitLdcInsn(true);
		visitor.visitJumpInsn(Opcodes.GOTO, labelEnd);
		
		// False
		visitor.visitLabel(labelFalse);
		visitor.visitLdcInsn(false);
		
		visitor.visitLabel(labelEnd);
    }
    
    /**
     * Returns a description of this and operator.
     * 
     * @return description of this and operator
     */
    public String toString() {
    	return "(" + this.value1 + " && " + this.value2 + ")";
    }
}
