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
 * Compares two other values for equality producing a boolean as result.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class NotEqualsOperator extends ComparisonOperator {
	
	/**
	 * Constructs a new not equals operator comparing the two specified
	 * values.
	 * 
	 * @param value1 the left side value
	 * @param value2 the right side value
	 */
    public NotEqualsOperator(Value value1, Value value2) {
    	super(value1, value2);
    }
    
    /**
     * Compares two values for equality.
     * 
     * @return <code>true</code> if values are not equal, <code>false</code> if equal
     */
    protected boolean getBooleanValue() {
    	Type type = resolveType(this.value1, this.value2);
    	
    	switch(type) {
    	case STRING:
    		return !value1.getStringValue().equals(value2.getStringValue());
    		
    	case INT:
    		return value1.getIntValue() != value2.getIntValue();
    		
    	case DOUBLE:
    		return value1.getDoubleValue() != value2.getDoubleValue();
    	}
    	
    	return false;
    }
    
    /**
     * Create bytecode for evaluating this not equals operator. The
     * specified method visitor is used for generating bytecode. Produces a
     * result of the specified type.
     * 
     * @param visitor method visitor
     * @param type the type
     */
    void compile(MethodVisitor visitor, Type type) {
    	Type subType = resolveType(this.value1, this.value2);
    	
    	switch(subType) {
    	case STRING:
    		compileString(visitor);
    		break;
    		
    	case INT:
    		compileInt(visitor);
    		break;
    		
    	case DOUBLE:
    		compileDouble(visitor);
    		break;
    	}    	
    }
    
    /**
     * Compiles bytecode for evaluating this not equals operator producing
     * a string as result. The specified method visitor is used for generating
     * bytecode.
     * 
     * @param visitor the method visitor
     */
    private void compileString(MethodVisitor visitor) {
		Label trueLabel = new Label();
		Label falseLabel = new Label();
		Label nullLabel = new Label();
		
		value1.compile(visitor, Type.STRING);
		visitor.visitVarInsn(Opcodes.ASTORE, 1);
		value2.compile(visitor, Type.STRING);
		visitor.visitVarInsn(Opcodes.ASTORE, 2);
		
		visitor.visitVarInsn(Opcodes.ALOAD, 1);
		visitor.visitJumpInsn(Opcodes.IFNULL, nullLabel);
		visitor.visitVarInsn(Opcodes.ALOAD, 2);
		visitor.visitJumpInsn(Opcodes.IFNULL, nullLabel);
		
		visitor.visitVarInsn(Opcodes.ALOAD, 1);
		visitor.visitVarInsn(Opcodes.ALOAD, 2);
		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z");
		visitor.visitJumpInsn(Opcodes.IFNE, falseLabel);
		
		visitor.visitLabel(nullLabel);
		visitor.visitLdcInsn(true);
		visitor.visitJumpInsn(Opcodes.GOTO, trueLabel);
		
		visitor.visitLabel(falseLabel);
		visitor.visitLdcInsn(false);
		
		visitor.visitLabel(trueLabel);   	
    }
    
    /**
     * Compiles bytecode for evaluating this not equals operator producing
     * an integer as result. The specified method visitor is used for generating
     * bytecode.
     * 
     * @param visitor the method visitor
     */
    private void compileInt(MethodVisitor visitor) {
		Label trueLabel = new Label();
		Label falseLabel = new Label();
		
		value1.compile(visitor, Type.INT);
		value2.compile(visitor, Type.INT);
		visitor.visitJumpInsn(Opcodes.IF_ICMPEQ, trueLabel);
		
		visitor.visitLdcInsn(true);
		visitor.visitJumpInsn(Opcodes.GOTO, falseLabel);
		
		visitor.visitLabel(trueLabel);
		visitor.visitLdcInsn(false);
		
		visitor.visitLabel(falseLabel);    	
    }
    
    /**
     * Compiles bytecode for evaluating this not equals operator producing
     * a double as result. The specified method visitor is used for generating
     * bytecode.
     * 
     * @param visitor the method visitor
     */
    private void compileDouble(MethodVisitor visitor) {
    	Label trueLabel = new Label();
    	Label falseLabel = new Label();
    	
    	value1.compile(visitor, Type.DOUBLE);
    	value2.compile(visitor, Type.DOUBLE);
    	visitor.visitInsn(Opcodes.DCMPL);
    	visitor.visitJumpInsn(Opcodes.IFEQ, trueLabel);
    	
    	visitor.visitLdcInsn(true);
		visitor.visitJumpInsn(Opcodes.GOTO, falseLabel);
		
		visitor.visitLabel(trueLabel);
		visitor.visitLdcInsn(false);
		
		visitor.visitLabel(falseLabel);    	
    }
}
