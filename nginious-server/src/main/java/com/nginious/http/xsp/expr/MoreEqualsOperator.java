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
 * A comparison operator which checks if the left side argument value is greater or equals to
 * the right side argument value.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class MoreEqualsOperator extends ComparisonOperator {
	
	/**
	 * Constructs a new more than or equals operator with the specified two argument values.
	 * 
	 * @param value1 the left side argument value
	 * @param value2 the right side argument value
	 * @throws ExpressionException if any of the two argument values are not numeric
	 */
     public MoreEqualsOperator(Value value1, Value value2) throws ExpressionException {
    	super(value1, value2);
    	
    	if(value1.getType() == Type.STRING || value2.getType() == Type.STRING) {
    		throw new ExpressionException("Can't use more than or equals operator with strings");
    	}
    }

     /**
      * Evaluates this more than or equals operator.
      * 
      * @return <code>true</code> if the left side argument value is greater than or equals
      * to the right side argument value, <code>false</code> otherwise
      */
    @SuppressWarnings("incomplete-switch")
	protected boolean getBooleanValue() {
    	Type type = resolveType(this.value1, this.value2);
    	
    	switch(type) {
    	case INT:
    		return value1.getIntValue() >= value2.getIntValue();
    		
    	case DOUBLE:
    		return value1.getDoubleValue() >= value2.getDoubleValue();
    	}
    	
    	return false;
    }
    
    /**
     * Creates bytecode for evaluating this operator. The bytecode is generated using
     * the specified method visitor and for the specified type.
     * 
     * @param visitor the methos visitor
     * @param type the type
     */
    @SuppressWarnings("incomplete-switch")
	void compile(MethodVisitor visitor, Type type) {
    	Type subType = resolveType(this.value1, this.value2);
    	
    	switch(subType) {
    	case INT:
    		compileInt(visitor);
    		break;
    		
    	case DOUBLE:
    		compileDouble(visitor);
    		break;
    	}    	
    }
    
    /**
     * Creates bytecode for evaluating the argument values as integers. The bytecode
     * is generated using the specified method visitor.
     * 
     * @param visitor the method visitor
     */
    private void compileInt(MethodVisitor visitor) {
		Label labelFalse = new Label();
		Label labelEnd = new Label();
		
 		value1.compile(visitor, Type.INT);
 		value2.compile(visitor, Type.INT);
    	
 		visitor.visitJumpInsn(Opcodes.IF_ICMPLT, labelFalse);

		// True
		visitor.visitLdcInsn(true);
		visitor.visitJumpInsn(Opcodes.GOTO, labelEnd);
		
		// False
		visitor.visitLabel(labelFalse);
		visitor.visitLdcInsn(false);
		
		visitor.visitLabel(labelEnd);   	
    }
    
    /**
     * Creates bytecode for evaluating the argument values as doubles. The bytecode
     * is generated using the specified method visitor.
     * 
     * @param visitor the method visitor
     */
    private void compileDouble(MethodVisitor visitor) {
		Label labelFalse = new Label();
		Label labelEnd = new Label();
		
 		value1.compile(visitor, Type.DOUBLE);
 		value2.compile(visitor, Type.DOUBLE);
 		
 		visitor.visitInsn(Opcodes.DCMPL);
 		visitor.visitJumpInsn(Opcodes.IFLT, labelFalse);
 		
		// True
		visitor.visitLdcInsn(true);
		visitor.visitJumpInsn(Opcodes.GOTO, labelEnd);
		
		// False
		visitor.visitLabel(labelFalse);
		visitor.visitLdcInsn(false);
		
		visitor.visitLabel(labelEnd);   	
    }
}
