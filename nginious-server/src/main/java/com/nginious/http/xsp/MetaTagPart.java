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
 * A meta tag part contains information used by the XSP compiler when creating a subclass of {@link XspService}
 * based on the XSP page where the meta tag part was found.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
class MetaTagPart extends TagPart {
	
	private String name;
	
	private String content;
	
	/**
	 * Constructs a new meta tag part with the specified name.
	 * 
	 * @param name the tag name
	 * @param srcFilePath the source file path to the XSP page where the meta tag was found
	 * @param lineNo the line number in the XSP page file where the meta tag was found
	 */
    MetaTagPart(String name, String srcFilePath, int lineNo) {
        super(name, srcFilePath, lineNo);
    }
    
    /**
     * Does nothing
     */
	void addPart(XspPart part) {
		throw new RuntimeException("Method MetaTagPart.addPart(XspPart part) not implemented at " + getLocationDescriptor());
	}
	
	/**
	 * Does nothing
	 */
	void addTagPart(TagPart part) {
		throw new RuntimeException("Method MetaTagPart.addPart(TagPart part) not implemented at " + getLocationDescriptor());
	}
	
	/**
	 * Adds an attribute with the specified name and value to this meta tag part. The meta tag part supports
	 * attributes with name 'name' or 'content'.
	 * 
	 * @param name the attribute name
	 * @param value the attribute static value part
	 * @throws XspException if attribute name is unknown
	 */
	void addAttribute(String name, StaticPart value) throws XspException {
    	if(name.equals("name")) {
    		this.name = value.getStringContent();
    	} else if(name.equals("content")) {
    		this.content = value.getStringContent();
    	} else {
    		throw new XspException("Unknown attribute " + name + " for tag " + getName() + " at " + getLocationDescriptor());
    	}
    }
    
	/**
	 * Returns the value of the name attribute for this meta tag part.
	 * 
	 * @return the name attribute value
	 */
	String getName() {
		return this.name;
	}
	
	/**
	 * Returns the value of the content attribute for this meta tag part.
	 * 
	 * @return the content attribute value
	 */
	String getContent() {
		return this.content;
	}
	
	/**
	 * Does nothing
	 */
    void compile(String intClassName, ClassWriter writer, MethodVisitor visitor) throws XspException {
		return;
    }
    
    /**
     * Does nothing
     */
    void compileMethod(String intClassName, ClassWriter writer) throws XspException {
		return;
    }
}
