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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.nginious.http.xsp.expr.ExpressionException;
import com.nginious.http.xsp.expr.ExpressionParser;
import com.nginious.http.xsp.expr.TreeExpression;
import com.nginious.http.xsp.expr.Type;

/**
 * A XSP tag part which formats a parameterized and localized message.
 * 
 * Attributes
 * 
 * <ul>
 * 	<li>key - property key for the message value to format.</li>
 * 	<li>bundle - name of property file where key/value pairs are stored.</li>
 * 	<li>args - message arguments.</li>
 * 	<li>var - name of variable to store formated message in. If thid attribute is not present then the
 * 	result is printed to the output.</li>
 * </ul>
 * 
 * The following example prints 'Hello world' if the <code>value</code> variable contains the value
 * 'world'. The message text is taken from a file names <code>test_en_US.properties</code>.
 * 
 * <pre>
 * &lt;xsp:message key="test" bundle="test" args="${value}" />
 * </pre>
 * 
 * Example properties file with name 'test_en_US.properties'.
 *  
 * <pre>
 * test = Hello {0}
 * </pre>
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
class MessageTagPart extends TagPart {
	
	private enum Attribute {
		KEY("key"),
		BUNDLE("bundle"),
		ARGS("args"),
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
	
	private StaticPart key;
	
	private StaticPart bundle;
	
	private StaticPart args;
	
	private StaticPart var;
	
	private String methodName;
	
	/**
	 * Constructs a new message tag part with the specified name, XSP page source file path
	 * and line number location.
	 * 
	 * @param name the XSP tag name
	 * @param srcFilePath the XSP page source file path
	 * @param lineNo the line number in the XSP page source file
	 */
    MessageTagPart(String name, String srcFilePath, int lineNo) {
        super(name, srcFilePath, lineNo);
    }
    
    /**
     * Adds the attribute with the specified name and value to this message tag part. See class description
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
    	case KEY:
    		this.key = value;
    		break;
    		
    	case BUNDLE:
    		this.bundle = value;
    		break;
    		
    	case ARGS:
    		this.args = value;
    		break;
    		
    	case VAR:
    		this.var = value;
    		break;
    		
    	default:
    		throw new XspException("Unknown attribute " + name + " for tag " + getName() + " at " + getLocationDescriptor());
    	}
    }
    
    /**
     * Creates bytecode for this message tag part using the specified class writer and method visitor.
     * 
     * @param intClassName the binary class name of the class being created
     * @param writer the class writer
     * @param visitor the method visitor
     * @throws XspException if unable to create bytecode
     */
    void compile(String intClassName, ClassWriter writer, MethodVisitor visitor) throws XspException {
    	if(this.key == null) {
       		throw new XspException("Attribute key is missing for tag + " + getName() + " at " + getLocationDescriptor());
    	}
    	
    	if(this.bundle == null) {
       		throw new XspException("Attribute bundle is missing for tag + " + getName() + " at " + getLocationDescriptor());
    	}
    	
    	if(this.var != null && var.isExpression()) {
       		throw new XspException("Expressions are not allowed in attribute var for tag + " + getName() + " at " + getLocationDescriptor());
    	}

    	this.methodName = "message" + methodNameCounter.getAndIncrement();
    	visitor.visitVarInsn(Opcodes.ALOAD, 0);
    	visitor.visitVarInsn(Opcodes.ALOAD, 1);
    	visitor.visitVarInsn(Opcodes.ALOAD, 2);
    	visitor.visitVarInsn(Opcodes.ALOAD, 3);
    	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, intClassName, this.methodName, "(Lcom/nginious/http/HttpRequest;Lcom/nginious/http/HttpResponse;Ljava/lang/StringBuffer;)V");
    }
    
    /**
     * Creates bytecode in a separate method for evaluating this message tag part.
     * 
     * @param intClassName the binary class name of the class being created
     * @param writer the class writer
     * @throws XspException if unable to create bytecode
     */
    void compileMethod(String intClassName, ClassWriter writer) throws XspException {
    	MethodVisitor visitor = writer.visitMethod(Opcodes.ACC_PRIVATE, this.methodName, "(Lcom/nginious/http/HttpRequest;Lcom/nginious/http/HttpResponse;Ljava/lang/StringBuffer;)V", null, null);
    	visitor.visitCode();
    	
    	if(this.var != null) {
    		visitor.visitVarInsn(Opcodes.ALOAD, 1);
    		var.compile(visitor, Type.STRING);
    	} else {
    		visitor.visitVarInsn(Opcodes.ALOAD, 3);
    	}
    	
    	bundle.compile(visitor, Type.STRING);
    	visitor.visitVarInsn(Opcodes.ALOAD, 2);
    	visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "com/nginious/http/HttpResponse", "getLocale", "()Ljava/util/Locale;");
    	visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/ResourceBundle", "getBundle", "(Ljava/lang/String;Ljava/util/Locale;)Ljava/util/ResourceBundle;");
    	
    	key.compile(visitor, Type.STRING);
    	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/ResourceBundle", "getString", "(Ljava/lang/String;)Ljava/lang/String;");
    	
    	if(this.args != null) {    		
        	try {
        		String expression = args.getExpressionContent();
        		ExpressionParser parser = new ExpressionParser();
        		TreeExpression expr = parser.parse(expression);
        		
        		if(expr.getType() != Type.ANY) {
            		throw new XspException("Expression in attribute value in args " + 
            				getName() + " is not an attribute or bean property " +
            				" at line " + getLocationDescriptor());
        		}
        		
        		expr.compile(visitor, Type.ANY);
        		visitor.visitTypeInsn(Opcodes.CHECKCAST, "[Ljava/lang/Object;");
        		visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/text/MessageFormat", "format", "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;");
        	} catch(ExpressionException e) {
        		throw new XspException("Invalid expression in attribute value in tag " +
        				getName() + " at line " + getLocationDescriptor(), e);    		
        	}
    	}
    	
    	if(this.var != null) {
    		visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "com/nginious/http/HttpRequest", "setAttribute", "(Ljava/lang/String;Ljava/lang/Object;)V");
    	} else {
    		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
    		visitor.visitInsn(Opcodes.POP);    		
    	}
    	
		visitor.visitInsn(Opcodes.RETURN);
		visitor.visitMaxs(5, 5);
    	visitor.visitEnd();
    	
    	super.compileMethod(intClassName, writer);
    }
}
