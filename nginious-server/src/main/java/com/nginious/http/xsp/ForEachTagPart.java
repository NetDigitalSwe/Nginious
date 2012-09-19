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
 * A XSP tag part which iterates over a collection. All child tags are executed for each iteration.
 * 
 * Attributes
 * 
 * <ul>
 * 	<li>set - name of variable containing the collection to iterate over</li> 
 * 	<li>start - start position in the collection to start iterating from. Iteration starts from the first item
 * 	in the collection if this attribute is not present.</li>
 * 	<li>end - end position in the collection to stop iterating at. Iteration ends at the last item in the
 * 	collection if this attribute is not present.</li>
 * 	<li>step - number to increment position with for each iteration. This value is set to 1 if this attribute
 * 	is not present.</li>
 * 	<li>var - the variable name to place the item from the current position.</li>
 * </ul>
 * 
 * The following example iterates over the first 5 items in a collection named test. For each iteration
 * the current item is placed in a variable named <code>item</code> and the text 'Hello world!' is printed
 * together with the item.
 * 
 * <pre>
 * &lt;xsp:forEach set="test" start="0" end="4" step="1" var="item"&gt;
 *   Hello world ${item}!
 * &lt;/xsp:forEach&gt;
 * </pre>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
class ForEachTagPart extends TagPart {
	
	private enum Attribute {
		SET("set"),
		START("start"),
		END("end"),
		STEP("step"),
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
	
	private String methodName;
	
	private StaticPart setValue;
	
	private StaticPart start;
	
	private StaticPart end;
	
	private StaticPart step;
	
	private StaticPart varName;
	
	private static AtomicInteger methodNameCounter = new AtomicInteger(1);
	
	/**
	 * Constructs a new for each tag part with the specified name, XSP page source file path
	 * and line number location.
	 * 
	 * @param name the XSP tag name
	 * @param srcFilePath the XSP page source file path
	 * @param lineNo the line number in the XSP page source file
	 */
    ForEachTagPart(String name, String srcFilePath, int lineNo) {
        super(name, srcFilePath, lineNo);
    }
    
    /**
     * Adds the attribute with the specified name and value to this for each tag part. See class description
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
    	case SET:
    		this.setValue = value;
    		break;
    		
    	case START:
    		this.start = value;
    		break;
    		
    	case END:
    		this.end = value;
    		break;
    		
    	case STEP:
    		this.step = value;
    		break;
    		
    	case VAR:
    		this.varName = value;
    		break;
    		
    	default:
    		throw new XspException("Unknown attribute " + name + " for tag " + getName() + " at " + getLocationDescriptor());
    	}
    }
       
    /**
     * Creates bytecode for this for each tag part using the specified class writer and method visitor.
     * 
     * @param intClassName the binary class name of the class being created
     * @param writer the class writer
     * @param visitor the method visitor
     * @throws XspException if unable to create bytecode
     */
    void compile(String intClassName, ClassWriter writer, MethodVisitor visitor) throws XspException {
    	if(this.setValue== null) {
    		throw new XspException("Attribute set is missing for tag + " + getName() + " at " + getLocationDescriptor());
    	}
    	
    	if(this.varName == null) {
    		throw new XspException("Attribute var is missing for tag + " + getName() + " at " + getLocationDescriptor());
    	}
    	
    	this.methodName = "forEach" + methodNameCounter.getAndIncrement();
    	visitor.visitVarInsn(Opcodes.ALOAD, 0);
    	visitor.visitVarInsn(Opcodes.ALOAD, 1);
    	visitor.visitVarInsn(Opcodes.ALOAD, 2);
    	visitor.visitVarInsn(Opcodes.ALOAD, 3);
    	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, intClassName, this.methodName, "(Lcom/nginious/http/HttpRequest;Lcom/nginious/http/HttpResponse;Ljava/lang/StringBuffer;)V");
    }

    /**
     * Creates bytecode in a separate method for evaluating this for each tag part.
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
		
    	try {
    		String expression = setValue.getExpressionContent();
    		ExpressionParser parser = new ExpressionParser();
    		TreeExpression expr = parser.parse(expression);
    		
    		if(expr.getType() != Type.ANY) {
    			throw new XspException("Expression in attribute set in tag " + 
    					getName() + " is not an attribute or bean property " +
        				" at line " + getLocationDescriptor());
    		}
    		
    		expr.compile(visitor, Type.ANY);
    		visitor.visitTypeInsn(Opcodes.CHECKCAST, "java/util/Collection");
    		visitor.visitVarInsn(Opcodes.ASTORE, 4);
    	} catch(ExpressionException e) {
    		throw new XspException("Invalid expression in attribute set in tag " +
    				getName() + " at line " + getLocationDescriptor(), e);    		
    	}
    	
		Label labelOut = new Label();    	
    	visitor.visitVarInsn(Opcodes.ALOAD, 4);
    	visitor.visitJumpInsn(Opcodes.IFNULL, labelOut);
    	
    	// Start
    	if(this.start != null) {
    		start.compile(visitor, Type.INT);
    	} else {
    		visitor.visitLdcInsn((int)0);
    	}
    	
    	visitor.visitVarInsn(Opcodes.ISTORE, 5);
    	
    	// End
    	if(this.end != null) {
    		end.compile(visitor, Type.INT);
    	} else {
    		visitor.visitVarInsn(Opcodes.ALOAD, 4);
    		visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Collection", "size", "()I");
    	}
    	
    	visitor.visitVarInsn(Opcodes.ISTORE, 6);
    	
    	// Step
    	if(this.step != null) {
    		step.compile(visitor, Type.INT);
    	} else {
    		visitor.visitLdcInsn((int)1);
    	}
    	
    	visitor.visitVarInsn(Opcodes.ISTORE, 7);
    	
    	// Current pos
    	visitor.visitLdcInsn(0);
    	visitor.visitVarInsn(Opcodes.ISTORE, 8);
    	
    	visitor.visitVarInsn(Opcodes.ALOAD, 4);
    	visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Collection", "iterator", "()Ljava/util/Iterator;");
    	visitor.visitVarInsn(Opcodes.ASTORE, 9);
    	
    	Label labelStart = new Label();
    	
    	// Start of loop
    	visitor.visitLabel(labelStart);
    	
    	// iterator.hasNext();
    	visitor.visitVarInsn(Opcodes.ALOAD, 9);
    	visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z");
    	visitor.visitJumpInsn(Opcodes.IFEQ, labelOut);
    	
    	// iterator.next();
    	visitor.visitVarInsn(Opcodes.ALOAD, 9);
    	visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
    	visitor.visitVarInsn(Opcodes.ASTORE, 10);
    	
    	// pos >= start && pos <= end && (pos - start) % step == 0
    	Label labelIncr = new Label();
    	
    	visitor.visitVarInsn(Opcodes.ILOAD, 8);
    	visitor.visitVarInsn(Opcodes.ILOAD, 5);
    	visitor.visitJumpInsn(Opcodes.IF_ICMPLT, labelIncr);
    	
    	visitor.visitVarInsn(Opcodes.ILOAD, 8);
    	visitor.visitVarInsn(Opcodes.ILOAD, 6);
    	visitor.visitJumpInsn(Opcodes.IF_ICMPGT, labelIncr);
    	
    	visitor.visitVarInsn(Opcodes.ILOAD, 8);
    	visitor.visitVarInsn(Opcodes.ILOAD, 5);
    	visitor.visitInsn(Opcodes.ISUB);
    	visitor.visitVarInsn(Opcodes.ILOAD, 7);
    	visitor.visitInsn(Opcodes.IREM);    	
    	visitor.visitJumpInsn(Opcodes.IFNE, labelIncr);
    	
    	visitor.visitVarInsn(Opcodes.ALOAD, 1);
    	varName.compile(visitor, Type.STRING);
    	visitor.visitVarInsn(Opcodes.ALOAD, 10);
    	visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "com/nginious/http/HttpRequest", "setAttribute", "(Ljava/lang/String;Ljava/lang/Object;)V");
    	
    	// Call sub parts
    	for(XspPart part : this.contentParts) {
    		part.compile(intClassName, writer, visitor);
    	}
    	
    	// pos++
    	visitor.visitLabel(labelIncr);
    	visitor.visitIincInsn(8, 1);
    	visitor.visitJumpInsn(Opcodes.GOTO, labelStart);
    	
    	visitor.visitLabel(labelOut);
		visitor.visitInsn(Opcodes.RETURN);
		
		visitor.visitLabel(startCatchLabel);
		
		visitor.visitVarInsn(Opcodes.ASTORE, 3);
		visitor.visitTypeInsn(Opcodes.NEW, "com/nginious/http/xsp/XspException");
    	visitor.visitInsn(Opcodes.DUP);
    	visitor.visitLdcInsn("Attribute set contains an invalid collection for tag " + getName() + " at " + getLocationDescriptor());
    	visitor.visitVarInsn(Opcodes.ALOAD, 3);
    	visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "com/nginious/http/xsp/XspException", "<init>", "(Ljava/lang/String;Ljava/lang/Throwable;)V");
    	visitor.visitInsn(Opcodes.ATHROW);

    	visitor.visitMaxs(11, 11);
    	visitor.visitEnd();
    }
}
