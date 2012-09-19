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

package com.nginious.http.xsp;

import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.nginious.http.xsp.expr.ExpressionException;
import com.nginious.http.xsp.expr.ExpressionParser;
import com.nginious.http.xsp.expr.TreeExpression;
import com.nginious.http.xsp.expr.Type;

/**
 * A part of a XSP page that contains static content.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
class StaticPart extends ChildPart {

	private static AtomicInteger methodNameCounter = new AtomicInteger(1);
	
	private String methodName;
	
	private byte[] content;

    private int start;

    private int len;
    
    /**
     * Constructs a new static part from the specified content.
     * 
     * @param content the content
     * @param start the start position in the content
     * @param len the length of the content
     * @param srcFilePath the XSP page source file path
     * @param lineNo the start line number of the static part in the source file
     */
    StaticPart(byte[] content, int start, int len, String srcFilePath, int lineNo) {
    	super(srcFilePath, lineNo);
        this.content = content;
        this.start = start;
        this.len = len;
    }
    
    /**
     * Returns this static part as a string.
     * 
     * @return this statis part as a string
     */
    String getStringContent() {
    	return new String(content, start, len);
    }
    
    /**
     * Returns whether or not this static part is an expression. A XSP expression starts with
     * ${ characters and ends with a } character.
     * 
     * @return <code>true</code> if this static part is an expression, <code>false</code> otherwise
     */
    boolean isExpression() {
    	String expr = getStringContent();
    	return expr.startsWith("${") && expr.endsWith("}");
    }
    
    /**
     * Returns content of expression if this static part is a XSP expression delimited by
     * ${ and } characters.
     * 
     * @return the expression or <code>null</code> if this static part is not an expression
     */
    String getExpressionContent() {
    	String expr = getStringContent();
    	
    	if(expr.startsWith("${") && expr.endsWith("}")) {
    		return expr.substring(2, expr.length() - 1);
    	}
    	
    	return null;
    }
    
    /**
     * Returns this static part as an integer.
     * 
     * @return this static part converted to an integer
     */
    int getIntContent() {
    	return Integer.parseInt(getStringContent());
    }
    
    /**
     * Returns this static part as a double.
     * 
     * @return this static part converted to a double
     */
    double getDoubleContent() {
    	return Double.parseDouble(getStringContent());
    }
    
    /**
     * Creates bytecode for this static part using the specified  class writer and method visitor. If this static
     * part is an expression a separate method is created with bytecode to evaluate the expression.
     * 
     * @param intClassName the binary class name of the class being created
     * @param writer the class writer
     * @param visitor the method visitor
     * @throws XspException if unable to create bytecode
     */
    void compile(String intClassName, ClassWriter writer, MethodVisitor visitor) throws XspException {
    	if(isExpression()) {
    		this.methodName = "staticExpr" + methodNameCounter.getAndIncrement();
        	visitor.visitVarInsn(Opcodes.ALOAD, 0);
        	visitor.visitVarInsn(Opcodes.ALOAD, 3);
        	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, intClassName, this.methodName, "(Ljava/lang/StringBuffer;)V");
        	compileMethod(intClassName, writer);
    	} else {
    		String contentStr = new String(content, start, len);
    		visitor.visitVarInsn(Opcodes.ALOAD, 3);
    		visitor.visitLdcInsn(contentStr);
    		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
    		visitor.visitInsn(Opcodes.POP);
    	}
    }
    
    /**
     * Creates bytecode in a separate method for evaluating an expression.
     * 
     * @param intClassName the binary class name of the class being created
     * @param writer the class writer
     * @throws XspException if unable to create bytecode
     */
    void compileMethod(String intClassName, ClassWriter writer) throws XspException {
    	if(!isExpression()) {
    		return;
    	}
    	
    	try {
    		ExpressionParser parser = new ExpressionParser();
    		TreeExpression expr = parser.parse(getExpressionContent());

    		MethodVisitor visitor = writer.visitMethod(Opcodes.ACC_PRIVATE, this.methodName, "(Ljava/lang/StringBuffer;)V", null, null);
    		visitor.visitCode();
    		visitor.visitVarInsn(Opcodes.ALOAD, 1);
    		
    		if(expr.getType() == Type.ANY){ 
    			expr.compile(visitor, Type.STRING);
    		} else {
    			expr.compile(visitor);
    		}
    		
    		if(expr.getType() == Type.BOOLEAN) {
        		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Z)Ljava/lang/StringBuffer;");
    		} else if(expr.getType() == Type.DOUBLE) {
        		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(D)Ljava/lang/StringBuffer;");
    		} else if(expr.getType() == Type.INT) {
        		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(I)Ljava/lang/StringBuffer;");
    		} else if(expr.getType() == Type.STRING || expr.getType() == Type.ANY) {
        		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
    		}
    		
    		visitor.visitInsn(Opcodes.POP);
    		visitor.visitInsn(Opcodes.RETURN);
    		visitor.visitMaxs(5, 5);
    		visitor.visitEnd();
    	} catch(ExpressionException e) {
    		throw new XspException("Invalid expression at " + getLocationDescriptor(), e);
    	}
    }
    
    /**
     * Creates bytecode for handling this static part as the specified type.
     * 
     * @param visitor the method visitor
     * @param type the type
     * @throws XspException if unable to create bytecode
     */
    void compile(MethodVisitor visitor, Type type) throws XspException {
    	switch(type) {
    	case BOOLEAN:
    		boolean booleanValue = Boolean.parseBoolean(getStringContent());
    		visitor.visitLdcInsn(booleanValue);
    		break;
    		
    	case DOUBLE:
    		try {
    			double intValue = getDoubleContent();
    			visitor.visitLdcInsn(intValue);
    		} catch(NumberFormatException e) {
    			throw new XspException("Unable to convert to double at " + getLocationDescriptor(), e);
    		}
    		break;
    		
    	case INT:
    		try {
    			int intValue = getIntContent();
    			visitor.visitLdcInsn(intValue);
    		} catch(NumberFormatException e) {
    			throw new XspException("Unable to convert to int at " + getLocationDescriptor(), e);
    		}
    		break;
    		
    	case STRING:
    		String value = getStringContent();
    		visitor.visitLdcInsn(value);
    		break;
    	}
    }
    
    /**
     * Returns whether or not this static part contains only whitespace.
     * 
     * @return <code>true</code> if this static part contains only whitespace, <code>false</code> otherwise
     */
    boolean isWhitespace() {
    	return getStringContent().matches("\\s+");
    }
    
    /**
     * Returns this static part as a string.
     * 
     * @return this static part as a string
     */
    public String toString() {
    	return getStringContent();
    }
}
