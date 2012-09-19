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
 * A left string function which takes a string and an integer as arguments producing a
 * substring starting from position 0 in the string. Below is an example which evaluates
 * to 'Tes'.
 * 
 * <pre>
 * left('Test', 3)
 * </pre>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class LeftFunction extends StringFunction {

    private Value value2;
    
    /**
     * Constructs a new left function with the specified argument values.
     * 
     * @param value1 the string.
     * @param value2 value producing integer for number of characters to extract from string
     */
    public LeftFunction(Value value1, Value value2) {
        super("left", value1);
        this.value2 = value2;
    }
    
    /**
     * Evaluates this left function producing a substring starting at position 0 in the
     * string.
     * 
     * @return the substring
     */
    protected String getStringValue() {
    	String expr = value.getStringValue();
    	
    	if(expr != null) {
    		return expr.substring(0, value2.getIntValue());
    	}
    	
    	return null;
    }
    
    /**
     * Creates bytecode for evaluating this left function. The specified method visitor
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
    	visitor.visitLdcInsn(0);
    	value2.compile(visitor, Type.INT);
    	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "substring", "(II)Ljava/lang/String;");
    	visitor.visitJumpInsn(Opcodes.GOTO, notNullLabel);
    	
    	visitor.visitLabel(nullLabel);
    	visitor.visitInsn(Opcodes.ACONST_NULL);
    	
    	visitor.visitLabel(notNullLabel);
    }
}
