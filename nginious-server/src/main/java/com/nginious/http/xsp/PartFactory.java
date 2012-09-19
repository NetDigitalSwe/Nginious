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

/**
 * A factory for creating tag parts.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
class PartFactory {
	
	/**
	 * Constructs a new part factory
	 */
    PartFactory() {
        super();
    }
    
    /**
     * Creates a new meta tag part with the specified name.
     * 
     * @param name the tag name
     * @param srcFilePath the source file path to the XSP page where the tag was found
     * @param lineNo the line number in the XSP page where the tag was found
     * @return the create meta tag part
     */
    MetaTagPart createMetaTagPart(String name, String srcFilePath, int lineNo) {
    	return new MetaTagPart(name, srcFilePath, lineNo);
    }
    
    /**
     * Constructs a new tag part with the specified name.
     * 
     * @param name the tag name
     * @param srcFilePath the source file path to the XSP page where the tag was found
     * @param lineNo the line number in the XSP page where the tag was found
     * @return the created tag
     * @throws XspException if the provided tag name is unknown
     */
    TagPart createTagPart(String name, String srcFilePath, int lineNo) throws XspException {
    	if(name.equals("if")) {
            return new IfTagPart(name, srcFilePath, lineNo);
        }

        if(name.equals("forEach")) {
            return new ForEachTagPart(name, srcFilePath, lineNo);
        }
        
        if(name.equals("formatDate")) {
        	return new FormatDateTagPart(name, srcFilePath, lineNo);
        }
        
        if(name.equals("formatNumber")) {
        	return new FormatNumberTagPart(name, srcFilePath, lineNo);
        }
        
        if(name.equals("message")) {
        	return new MessageTagPart(name, srcFilePath, lineNo);
        }

        throw new XspException("Unknown xsp tag " + name);
    }
}
