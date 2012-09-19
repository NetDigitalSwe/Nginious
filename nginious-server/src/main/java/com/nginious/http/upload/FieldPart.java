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

package com.nginious.http.upload;

import com.nginious.http.common.FixedBuffer;

/**
 * A field part from multipart content has a name and a value just like query parameters in
 * a URI.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class FieldPart extends PartImpl {
	
	private FixedBuffer buffer;
	
	private String value;
	
	/**
	 * Constructs a new field part with the specified name.
	 * 
	 * @param name the name
	 */
	FieldPart(String name) {
		super(name);
		this.buffer = new FixedBuffer(1024);
	}
	
	/**
	 * Returns the value for this field part.
	 * 
	 * @return the value
	 */
	public String getValue() {
		return this.value;
	}
	
	/**
	 * Adds the specified content to this field part. Content is added starting from the
	 * specified offset and length.
	 * 
	 * @param content the content to add
	 * @param off start offset in content
	 * @param len length to add
	 */
	void content(byte[] content, int off, int len) {
		buffer.put(content, off, len);
	}
	
	/**
	 * Ends the adding of content.
	 */
	void end() {
		this.value = new String(buffer.toByteArray());
		this.buffer = null;
	}
}
