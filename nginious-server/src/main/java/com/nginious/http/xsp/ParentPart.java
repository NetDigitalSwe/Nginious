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

import java.util.ArrayList;
import java.util.List;

/**
 * A XSP part that can contain other parts as children.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
abstract class ParentPart extends XspPart {
	
	protected List<XspPart> contentParts;
	
	/**
	 * Constructs a new parent part.
	 * 
	 * @param srcFilePath the source file path to the XSP page where the tag was found
	 * @param lineNo the line number in the XSP page where the tag was found
	 */
	ParentPart(String srcFilePath, int lineNo) {
		super(srcFilePath, lineNo);
		this.contentParts = new ArrayList<XspPart>();
	}
	
	/**
	 * Returns a list of child parts for this parent part.
	 * 
	 * @return the child parts
	 */
	List<XspPart> getContentParts() {
		return this.contentParts;
	}
	
	/**
	 * Adds the specified XSP part as child to this parent part.
	 * 
	 * @param part the XSP part
	 */
	void addPart(XspPart part) {
		contentParts.add(part);
	}
	
	/**
	 * Adds the specified tag part as child to this parent part.
	 * 
	 * @param part the tag part
	 */
	void addTagPart(TagPart part) {
		contentParts.add(part);
	}
	
	/**
	 * Returns whether or not the last child part for this parent part is a tag part.
	 * 
	 * @return <code>true</code> if the last child part is a tag part, <code>false</code> otherwise
	 */
	boolean isLastTagPart() {
		return contentParts.size() > 0 && contentParts.get(contentParts.size() - 1) instanceof TagPart;
	}
	
	void addMetaTagPart(MetaTagPart part) {
		throw new RuntimeException("Method ParentPart.addMetaTagPart(MetaTagPart part) not implemented at" + getLocationDescriptor());
	}
}
