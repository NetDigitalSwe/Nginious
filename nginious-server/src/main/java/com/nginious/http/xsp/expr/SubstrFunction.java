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
 * A substring function which takes a string and two integers as arguments producing a
 * substring starting from the lefttmost position in the string. Below is an example which evaluates
 * to 'Tes'.
 * 
 * <pre>
 * substr('Test', 0, 3)
 * </pre>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class SubstrFunction extends StringFunction {

    private Value value2;

    private Value value3;
    
    /**
     * Constructs a new substring function with the specified argument values.
     * 
     * @param value1 the string
     * @param value2 the start position in the string
     * @param value3 the end position in the string
     */
    public SubstrFunction(Value value1, Value value2, Value value3) {
        super("substr", value1);
        this.value2 = value2;
        this.value3 = value3;
    }
    
    /**
     * Evaluates this substring function producing a substring starting at the start position and ending
     * ath the end position in the string.
     * 
     * @return the substring
     */
    protected String getStringValue() {
        String value1 = value.getStringValue();
        
        if(value1 == null) {
        	return null;
        }
        
        int start = value2.getIntValue();
        int end = value3.getIntValue();
        
        if(start < 0) {
        	start = 0;
        }
        
        if(start > value1.length()) {
          start = value1.length();
        }
        
        if(end < start) {
        	end = start;
        }
        
        if(end > value1.length()) {
        	end = value1.length();
        }
        
        return value1.substring(start, end);
    }
    
    /**
     * Creates bytecode for evaluating this substring function. The specified method visitor
     * and type are used for generating bytecode.
     * 
     * @param visitor the method visitor
     * @param type the type
     */
    void compile(MethodVisitor visitor, Type type) {
    	value.compile(visitor, Type.STRING);
    	visitor.visitVarInsn(Opcodes.ASTORE, 1);
    	value2.compile(visitor, Type.INT);
    	visitor.visitVarInsn(Opcodes.ISTORE, 2);
    	value3.compile(visitor, Type.INT);
    	visitor.visitVarInsn(Opcodes.ISTORE, 3);
    	
    	Label nullLabel = new Label();
    	Label notNullLabel = new Label();
    	
    	// check for null string
    	visitor.visitVarInsn(Opcodes.ALOAD, 1);
    	visitor.visitJumpInsn(Opcodes.IFNULL, nullLabel);
    	
    	// start < 0
    	Label label1 = new Label();
    	visitor.visitLdcInsn(0);
    	visitor.visitVarInsn(Opcodes.ILOAD, 2);
    	visitor.visitJumpInsn(Opcodes.IF_ICMPLT, label1);
    	visitor.visitLdcInsn(0);
    	visitor.visitVarInsn(Opcodes.ISTORE, 2);
    	
    	// start > value.length
    	Label label2 = new Label();
    	visitor.visitLabel(label1);
    	visitor.visitVarInsn(Opcodes.ALOAD, 1);
    	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I");
    	visitor.visitVarInsn(Opcodes.ILOAD, 2);
    	visitor.visitJumpInsn(Opcodes.IF_ICMPGT, label2);
    	visitor.visitVarInsn(Opcodes.ALOAD, 1);
    	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I");
    	visitor.visitVarInsn(Opcodes.ISTORE, 2);
    	    	
    	// end < start
    	Label label3 = new Label();
    	visitor.visitLabel(label2);
    	visitor.visitVarInsn(Opcodes.ILOAD, 2);
    	visitor.visitVarInsn(Opcodes.ILOAD, 3);
    	visitor.visitJumpInsn(Opcodes.IF_ICMPLT, label3);
    	visitor.visitVarInsn(Opcodes.ILOAD, 2);
    	visitor.visitVarInsn(Opcodes.ISTORE, 3);
    	
    	// end > value1.length
    	Label label4 = new Label();
    	visitor.visitLabel(label3);
    	visitor.visitVarInsn(Opcodes.ALOAD, 1);
    	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I");
    	visitor.visitVarInsn(Opcodes.ILOAD, 3);
    	visitor.visitJumpInsn(Opcodes.IF_ICMPGT, label4);
    	visitor.visitVarInsn(Opcodes.ALOAD, 1);
    	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I");
    	visitor.visitVarInsn(Opcodes.ISTORE, 3);
    	
    	// substr
    	visitor.visitLabel(label4);
    	visitor.visitVarInsn(Opcodes.ALOAD, 1);
    	visitor.visitVarInsn(Opcodes.ILOAD, 2);
    	visitor.visitVarInsn(Opcodes.ILOAD, 3);
    	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "substring", "(II)Ljava/lang/String;");
    	visitor.visitJumpInsn(Opcodes.GOTO, notNullLabel);
    	
    	visitor.visitLabel(nullLabel);
    	visitor.visitInsn(Opcodes.ACONST_NULL);
    	
    	visitor.visitLabel(notNullLabel);
    	
    }
}
