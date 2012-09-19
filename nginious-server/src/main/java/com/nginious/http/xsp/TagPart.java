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

/**
 * Base class for all XSP tag parts.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
abstract class TagPart extends ParentPart {

    private String name;
    
    /**
     * Constructs a new tag part with the specified name, created from the XSP page
     * at the specified source file path.
     * 
     * @param name the tag name
     * @param srcFilePath the XSP page source file path
     * @param lineNo the line number for the tag
     */
    TagPart(String name, String srcFilePath, int lineNo) {
    	super(srcFilePath, lineNo);
    	this.name = name;
    }
    
    /**
     * Returns this XSP tags name.
     * 
     * @return the name
     */
    String getName() {
        return this.name;
    }
    
    /**
     * Adds an attribute with the specified name and part value.
     * 
     * @param name the attribute name
     * @param part the value which is static content or a XSP expression
     * @throws XspException if attribute is not supported by this tag part
     */
    abstract void addAttribute(String name, StaticPart part) throws XspException;
    
    /**
     * Creates bytecode for this tag part and all child parts to this tag part.
     * 
     * @param intClassName the binary class name of the XSP service being compiled.
     * @param writer the class writer
     */
	void compileMethod(String intClassName, ClassWriter writer) throws XspException {
		for(XspPart part : this.contentParts) {
			part.compileMethod(intClassName, writer);
		}		
	}
}
