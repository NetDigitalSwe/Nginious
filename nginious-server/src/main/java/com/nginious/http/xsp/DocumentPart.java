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

import java.util.HashMap;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * A document part represents a complete XSP page. All other parts are sub parts to an instance
 * of this part.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class DocumentPart extends ParentPart {
	
	private HashMap<String, MetaTagPart> metaTags;
	
	/**
	 * Constructs a new document part with the specified XSP page source file path and
	 * line number.
	 * 
	 * @param srcFilePath the XSP page source file path
	 * @param lineNo the line number
	 */
	DocumentPart(String srcFilePath, int lineNo) {
		super(srcFilePath, lineNo);
		this.metaTags = new HashMap<String, MetaTagPart>();
	}
	
	/**
	 * Adds the specified sub tag part to this document tag part.
	 * 
	 * @param part the sub tag part to add
	 */
	void addTagPart(TagPart part) {
		contentParts.add(part);
	}
	
	/**
	 * Adds the specified meta tag part to this document tag part.
	 * 
	 * @param part the meta tag part to add
	 */
	void addMetaTagPart(MetaTagPart part) {
		metaTags.put(part.getName(), part);
	}
	
	/**
	 * Returns the meta tag with the specified name.
	 * 
	 * @param name the meta name
	 * @return the meta value
	 */
	String getMetaContent(String name) {
		MetaTagPart part = metaTags.get(name);
		
		if(part != null) {
			return part.getContent();
		}
		
		return null;
	}
	
    /**
     * Creates bytecode for this document tag part using the specified class writer and method visitor.
     * 
     * @param intClassName the binary class name of the class being created
     * @param writer the class writer
     * @param visitor the method visitor
     * @throws XspException if unable to create bytecode
     */
	void compile(String intClassName, ClassWriter writer, MethodVisitor visitor) throws XspException {
    	// 0 = this, 1 = HttpRequest, 2 = HttpResponse, 3 = StringBuffer
		
		// StringBuffer buffer = new StringBuffer();
		visitor.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuffer");
		visitor.visitInsn(Opcodes.DUP);
		visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuffer", "<init>", "()V");
		visitor.visitVarInsn(Opcodes.ASTORE, 3);
        
		for(XspPart part : contentParts) {
			part.compile(intClassName, writer, visitor);
		}
		
		// response.setContentLength(buffer.length());
		visitor.visitVarInsn(Opcodes.ALOAD, 2);
		visitor.visitVarInsn(Opcodes.ALOAD, 3);
		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuffer", "length", "()I");
		visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "com/nginious/http/HttpResponse", "setContentLength", "(I)V");
		
		// response.addHeader(contentType);
		String contentType = getMetaContent("Content-Type");
		
		if(contentType == null) {
			contentType = "application/unknown";
		}
		
		visitor.visitVarInsn(Opcodes.ALOAD, 2);
		visitor.visitLdcInsn("Content-Type");
		visitor.visitLdcInsn(contentType);
		visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "com/nginious/http/HttpResponse", "addHeader", "(Ljava/lang/String;Ljava/lang/String;)V");
		
		// PrintWriter writer = response.getWriter();
		// writer.print(buffer.toString());
		visitor.visitVarInsn(Opcodes.ALOAD, 2);
		visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "com/nginious/http/HttpResponse", "getWriter", "()Ljava/io/PrintWriter;");
		visitor.visitVarInsn(Opcodes.ALOAD, 3);
		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuffer", "toString", "()Ljava/lang/String;");
		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintWriter", "print", "(Ljava/lang/String;)V");
	}
	
    /**
     * Creates bytecode in a separate method for evaluating this document tag part.
     * 
     * @param intClassName the binary class name of the class being created
     * @param writer the class writer
     * @throws XspException if unable to create bytecode
     */
	void compileMethod(String intClassName, ClassWriter writer) throws XspException {
		for(XspPart part : this.contentParts) {
			part.compileMethod(intClassName, writer);
		}		
	}
}
