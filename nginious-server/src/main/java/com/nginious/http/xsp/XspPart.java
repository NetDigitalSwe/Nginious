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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

/**
 * Represents a part of a XSP page. A XSP page is split into parts when parsed by a
 * {@link XspParser}. 
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
abstract class XspPart {
	
	private String srcFilePath;
	
	private int lineNo;
	
	/**
	 * Constructs a new XSP part found at the specified line no in the XSP page at
	 * the specified source file path.
	 * 
	 * @param srcFilePath the source file path
	 * @param lineNo the line number
	 */
	XspPart(String srcFilePath, int lineNo) {
		this.srcFilePath = srcFilePath;
		this.lineNo = lineNo;
	}
	
	/**
	 * Returns location descriptor describing where in the source XSP file this XSP part
	 * can be found.
	 * 
	 * @return the location descriptor
	 */
	String getLocationDescriptor() {
		return this.srcFilePath + ":" + this.lineNo;
	}
	
	/**
	 * Creates bytecode for this XSP part using the specified class writer and method visitor.
	 * 
	 * @param intClassName the binary class name of the class being created
	 * @param writer the class writer
	 * @param visitor the method visitor
	 * @throws XspException if unable to create bytecode
	 */
    abstract void compile(String intClassName, ClassWriter writer, MethodVisitor visitor) throws XspException;
    
    /**
     * Creates bytecode for this XSP part using the specified class writer. A separate method is created
     * for this XSP part.
     * 
     * @param intClassName the binary class name of the class being created
     * @param writer the class writer
     * @throws XspException if unable to create bytecode
     */
    abstract void compileMethod(String intClassName, ClassWriter writer) throws XspException;
}
