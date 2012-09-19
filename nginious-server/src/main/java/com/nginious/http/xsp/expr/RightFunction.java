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
 * A right string function which takes a string and an integer as arguments producing a
 * substring starting from the rightmost position in the string. Below is an example which evaluates
 * to 'est'.
 * 
 * <pre>
 * right('Test', 3)
 * </pre>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class RightFunction extends StringFunction {

    private Value value2;
    
    /**
     * Constructs a new right function with the specified argument values.
     * 
     * @param value1 the string.
     * @param value2 value producing integer for number of characters to extract from string
     */    
    public RightFunction(Value value1, Value value2) {
        super("right", value1);
        this.value2 = value2;
    }
    
    /**
     * Evaluates this right function producing a substring starting at the rightmost position in
     * the string
     * 
     * @return the substring
     */
    protected String getStringValue() {
        String value1 = value.getStringValue();
        
        if(value1 == null) {
        	return null;
        }
        
        int len = value2.getIntValue();
        return value1.substring(value1.length() - len);
    }
    
    /**
     * Creates bytecode for evaluating this right function. The specified method visitor
     * and type are used for generating bytecode.
     * 
     * @param visitor the method visitor
     * @param type the type
     */
    void compile(MethodVisitor visitor, Type type) {
    	value.compile(visitor, Type.STRING);
    	visitor.visitVarInsn(Opcodes.ASTORE, 1);
    	
    	Label nullLabel = new Label();
    	Label notNullLabel = new Label();
    	
    	visitor.visitVarInsn(Opcodes.ALOAD, 1);
    	visitor.visitJumpInsn(Opcodes.IFNULL, nullLabel);
    	
    	visitor.visitVarInsn(Opcodes.ALOAD, 1);
    	visitor.visitVarInsn(Opcodes.ALOAD, 1);
    	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I");
    	value2.compile(visitor, Type.INT);
    	visitor.visitInsn(Opcodes.ISUB);
    	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "substring", "(I)Ljava/lang/String;");
    	visitor.visitJumpInsn(Opcodes.GOTO, notNullLabel);
    	
    	visitor.visitLabel(nullLabel);
    	visitor.visitInsn(Opcodes.ACONST_NULL);
    	
    	visitor.visitLabel(notNullLabel);
    }
}
