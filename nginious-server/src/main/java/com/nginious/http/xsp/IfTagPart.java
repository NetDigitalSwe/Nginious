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
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.nginious.http.xsp.expr.ExpressionException;
import com.nginious.http.xsp.expr.ExpressionParser;
import com.nginious.http.xsp.expr.TreeExpression;
import com.nginious.http.xsp.expr.Type;

/**
 * A XSP tag which evaluates an expression. If the expression evaluates to true all child tags
 * are executed.
 * 
 * Attributes
 * 
 * <ul>
 * 	<li>test - provides an expression which must evaluate to <code>true</code> for the child tags
 * 	of this tag to be executed.</li>
 * </ul>
 * 
 * Example: if the test variable contains the value 2 the text 'Hello world!' is printed
 * 
 * <pre>
 * &lt;xsp:if test="${test} == 2"&gt;
 *   Hello world!
 * &lt;/xsp:if&gt;
 * </pre>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
class IfTagPart extends TagPart {
	
	private static AtomicInteger methodNameCounter = new AtomicInteger(1);
	
	private StaticPart part;
	
	private String methodName;
	
	/**
	 * Constructs a new if tag part with the specified name located in the XSP page file
	 * at the specified source file path and line in the file.
	 * 
	 * @param name the tag name
	 * @param srcFilePath the XSP page file path
	 * @param lineNo the line number in the XSP page
	 */
    IfTagPart(String name, String srcFilePath, int lineNo) {
        super(name, srcFilePath, lineNo);
    }
    
    /**
     * Returns the test attribute expression value
     * 
     * @return the test attribute expression value
     */
    StaticPart getTestAttribute() {
    	return part;
    }
    
    /**
     * Adds the attribute with the specified name and value to this if tag part.
     * 
     * @param name the attribute name
     * @param value the attribute value
     * @throws XspException if attribute is not supported by this tag part
     */
    void addAttribute(String name, StaticPart value) throws XspException {
    	if(!name.equals("test")) {
    		throw new XspException("Unknown attribute + " + name + " for tag " + getName() + " at " + getLocationDescriptor());
    	}
    	
    	this.part = value;
    }
    
    /**
     * Creates bytecode for this if tag part using the specified  class writer and method visitor. A separate
     * method is created within the class to evaluate the expression in the test attribute.
     * 
     * @param intClassName the binary class name of the class being created
     * @param writer the class writer
     * @param visitor the method visitor
     * @throws XspException if unable to create bytecode
     */
    void compile(String intClassName, ClassWriter writer, MethodVisitor visitor) throws XspException {
    	if(this.part == null) {
    		throw new XspException("Attribute test is missing for tag " + getName() + " at " + getLocationDescriptor());
    	}
    	
    	this.methodName = "ifTest" + methodNameCounter.getAndIncrement();
    	visitor.visitVarInsn(Opcodes.ALOAD, 0);
    	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, intClassName, this.methodName, "()Z");
    	Label falseLabel = new Label();
    	visitor.visitJumpInsn(Opcodes.IFEQ, falseLabel);
    	
    	for(XspPart part : this.contentParts) {
    		part.compile(intClassName, writer, visitor);
        }
    	
    	visitor.visitLabel(falseLabel);
    }
    
    /**
     * Creates bytecode in a separate method for evaluating this if tag part.
     * 
     * @param intClassName the binary class name of the class being created
     * @param writer the class writer
     * @throws XspException if unable to create bytecode
     */
    void compileMethod(String intClassName, ClassWriter writer) throws XspException {
    	if(this.part == null) {
    		throw new XspException("Attribute test is missing for tag " + getName() + " at " + getLocationDescriptor());
    	}
    	
    	if(!part.isExpression()) {
    		throw new XspException("Attribute test is not an expression for tag " + getName() + " at " + getLocationDescriptor());    		
    	}
    	
    	String expression = part.getExpressionContent();
    	
    	try {
    		ExpressionParser parser = new ExpressionParser();
    		TreeExpression expr = parser.parse(expression);

    		if(expr.getType() != Type.BOOLEAN) {
        		throw new XspException("Expression in attribute test in tag " + 
        				getName() + " does not produce a boolean " +
        				" at line " + getLocationDescriptor());
    		}
    		
    		MethodVisitor visitor = writer.visitMethod(Opcodes.ACC_PRIVATE, this.methodName, "()Z", null, null);
    		visitor.visitCode();
    		expr.compile(visitor);
    		visitor.visitInsn(Opcodes.IRETURN);
    		visitor.visitMaxs(5, 5);
    		visitor.visitEnd();
    		
    		super.compileMethod(intClassName, writer);
    	} catch(ExpressionException e) {
    		throw new XspException("Invalid expression in attribute test in tag " +
    				getName() + " at line " + getLocationDescriptor(), e);
    	}    	
    }
}
