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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.nginious.http.xsp.expr.ExpressionException;
import com.nginious.http.xsp.expr.ExpressionParser;
import com.nginious.http.xsp.expr.TreeExpression;
import com.nginious.http.xsp.expr.Type;

/**
 * A XSP tag part which formats a number according to formating rules provided in a pattern attribute.
 * 
 * Attributes
 * 
 * <ul>
 * 	<li>value - the number to format.</li>
 * 	<li>pattern - the number formating pattern. Accept the same formating rules as documented in the
 * 	{@link java.text.NumberFormat} class.
 * 	<li>var - name of variable to place formated number in. If this attribute is not present then the
 * 	result is printed to the output.</li>
 * </ul>
 * 
 * The following example formats a number found in variable value with a pattern '#,###'. The result is 
 * stored in a variable names 'result'.
 * 
 * <pre>
 * &lt;xsp:formatNumber value="${value}" pattern="#,###" var="result" /&gt;
 * </pre>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
class FormatNumberTagPart extends TagPart {
	
	private enum Attribute {
		VALUE("value"),
		PATTERN("pattern"),
		VAR("var");
		
		private static final HashMap<String, Attribute> lookup = new HashMap<String, Attribute>();
		
		static {
			for(Attribute attr : EnumSet.allOf(Attribute.class))
				lookup.put(attr.getName(), attr);
		}
		
		private String name;
		
		private Attribute(String name) {
			this.name = name;
		}	
		
		public String getName() {
			return this.name;
		}
		
		public static Attribute get(String name) {
			return lookup.get(name);
		}
	}
	
	private static AtomicInteger methodNameCounter = new AtomicInteger(1);
	
	private StaticPart value;
	
	private StaticPart pattern;
	
	private StaticPart var;
	
	private String methodName;
	
	/**
	 * Constructs a new format number tag part with the specified name, XSP page source file path
	 * and line number location.
	 * 
	 * @param name the XSP tag name
	 * @param srcFilePath the XSP page source file path
	 * @param lineNo the line number in the XSP page source file
	 */
    FormatNumberTagPart(String name, String srcFilePath, int lineNo) {
        super(name, srcFilePath, lineNo);
    }
    
    /**
     * Adds the attribute with the specified name and value to this format number tag part. See class description
     * for supported attributes.
     * 
     * @param name the attribute name
     * @param value the attribute value
     * @throws XspException if the attribute is not known by this tag
     */
    void addAttribute(String name, StaticPart value) throws XspException {
    	Attribute attr = Attribute.get(name);
    	
    	if(attr == null) {
    		throw new XspException("Unknown attribute " + name + " for tag " + getName() + " at " + getLocationDescriptor());
    	}
    	
    	switch(attr) {
    	case VALUE:
    		this.value = value;
    		break;
    		
    	case PATTERN:
    		this.pattern = value;
    		break;
    		
    	case VAR:
    		this.var = value;
    		break;
    		
    	default:
    		throw new XspException("Unknown attribute " + name + " for tag " + getName() + " at " + getLocationDescriptor());
    	}
    }
    
    /**
     * Creates bytecode for this format number tag part using the specified class writer and method visitor. 
     * 
     * @param intClassName the binary class name of the class being created
     * @param writer the class writer
     * @param visitor the method visitor
     * @throws XspException if unable to create bytecode
     */
    void compile(String intClassName, ClassWriter writer, MethodVisitor visitor) throws XspException {
    	if(this.value == null) {
       		throw new XspException("Attribute value is missing for tag + " + getName() + " at " + getLocationDescriptor());
    	}
    	
    	if(this.pattern == null) {
       		throw new XspException("Attribute pattern is missing for tag + " + getName() + " at " + getLocationDescriptor());
    	}
    	
    	if(pattern.isExpression()) {
       		throw new XspException("Expressions are not allowed in attribute pattern for tag + " + getName() + " at " + getLocationDescriptor());
    	}
    	
    	if(this.var != null && var.isExpression()) {
       		throw new XspException("Expressions are not allowed in attribute var for tag + " + getName() + " at " + getLocationDescriptor());
    	}

    	this.methodName = "formatNumber" + methodNameCounter.getAndIncrement();
    	visitor.visitVarInsn(Opcodes.ALOAD, 0);
    	visitor.visitVarInsn(Opcodes.ALOAD, 1);
    	visitor.visitVarInsn(Opcodes.ALOAD, 2);
    	visitor.visitVarInsn(Opcodes.ALOAD, 3);
    	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, intClassName, this.methodName, "(Lcom/nginious/http/HttpRequest;Lcom/nginious/http/HttpResponse;Ljava/lang/StringBuffer;)V");
    }
    
    /**
     * Creates bytecode in a separate method for evaluating this format number tag part.
     * 
     * @param intClassName the binary class name of the class being created
     * @param writer the class writer
     * @throws XspException if unable to create bytecode
     */
    void compileMethod(String intClassName, ClassWriter writer) throws XspException {
    	String[] exceptions = { "com/nginious/http/xsp/XspException" };			
    	MethodVisitor visitor = writer.visitMethod(Opcodes.ACC_PRIVATE, this.methodName, "(Lcom/nginious/http/HttpRequest;Lcom/nginious/http/HttpResponse;Ljava/lang/StringBuffer;)V", null, exceptions);
    	visitor.visitCode();
    	
		Label tryLabel = new Label();
		Label startCatchLabel = new Label();
		
		// Start try block
		visitor.visitTryCatchBlock(tryLabel, startCatchLabel, startCatchLabel, "java/lang/Exception");
		
		visitor.visitLabel(tryLabel);
		
		visitor.visitTypeInsn(Opcodes.NEW, "java/text/DecimalFormat");
    	visitor.visitInsn(Opcodes.DUP);
    	pattern.compile(visitor, Type.STRING);
    	visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/text/DecimalFormat", "<init>", "(Ljava/lang/String;)V");
    	visitor.visitVarInsn(Opcodes.ASTORE, 4);
    	
    	ExpressionParser parser = null;
    	TreeExpression expr = null;
    	
    	try {
    		String expression = value.getExpressionContent();
    		parser = new ExpressionParser();
    		expr = parser.parse(expression);
    		
    		if(expr.getType() != Type.ANY) {
        		throw new XspException("Expression in attribute value in tag " + 
        				getName() + " is not an attribute or bean property " +
        				" at line " + getLocationDescriptor());
    		}    		
    	} catch(ExpressionException e) {
    		throw new XspException("Invalid expression in attribute value in tag " +
    				getName() + " at line " + getLocationDescriptor(), e);    		
    	}
    	
    	visitor.visitVarInsn(Opcodes.ALOAD, 4);
		expr.compile(visitor, Type.DOUBLE);
    	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/text/DecimalFormat", "format", "(D)Ljava/lang/String;");
    	visitor.visitVarInsn(Opcodes.ASTORE, 5);

    	if(this.var != null) {
    		visitor.visitVarInsn(Opcodes.ALOAD, 1);
    		var.compile(visitor, Type.STRING);
    		visitor.visitVarInsn(Opcodes.ALOAD, 5);
    		visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "com/nginious/http/HttpRequest", "setAttribute", "(Ljava/lang/String;Ljava/lang/Object;)V");
    	} else {
    		visitor.visitVarInsn(Opcodes.ALOAD, 3);
    		visitor.visitVarInsn(Opcodes.ALOAD, 5);
    		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
    		visitor.visitInsn(Opcodes.POP);    		
    	}    	
     	
		visitor.visitInsn(Opcodes.RETURN);
		
		visitor.visitLabel(startCatchLabel);
		
		visitor.visitVarInsn(Opcodes.ASTORE, 4);
		visitor.visitTypeInsn(Opcodes.NEW, "com/nginious/http/xsp/XspException");
    	visitor.visitInsn(Opcodes.DUP);
    	visitor.visitLdcInsn("Attribute value contains an invalid number for tag " + getName() + " at " + getLocationDescriptor());
    	visitor.visitVarInsn(Opcodes.ALOAD, 4);
    	visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "com/nginious/http/xsp/XspException", "<init>", "(Ljava/lang/String;Ljava/lang/Throwable;)V");
    	visitor.visitInsn(Opcodes.ATHROW);
    	
		visitor.visitMaxs(6, 6);
    	visitor.visitEnd();
    	
    	super.compileMethod(intClassName, writer);
    }
}
