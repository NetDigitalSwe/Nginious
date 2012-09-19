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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.nginious.http.server.CaseInsensitiveKey;

/**
 * A multipart content part. Multipart content consists of one or more parts separated by
 * boundaries. Each part consists of a set of headers that describe the part conten and
 * the actual content. The boundary is found in the <code>Content-Type</code> header of
 * the HTTP request.
 * 
 * <p>
 * See <a href="">RFC 1867 Form-based File Upload in HTML</a> for detailed information.
 * </p>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
abstract class PartImpl implements Part {
	
	private String name;
	
	private HashMap<CaseInsensitiveKey, List<String>> headers;
	
	/**
	 * Constructs a new part with the specified name.
	 * @param name
	 */
	PartImpl(String name) {
		super();
		this.name = name;
	}
	
	/**
	 * Returns the first header value with the specified name for this part.
	 * 
	 * @param name the header name
	 * @return the first header value
	 */
	public String getHeader(String name) {
		CaseInsensitiveKey key = new CaseInsensitiveKey(name);
		List<String> values = headers.get(key);
		return values != null && values.size() > 0 ? values.get(0) : null;
	}
	
	/**
	 * Returns all header names for this part.
	 * 
	 * @return all header names
	 */
	public Collection<String> getHeaderNames() {
		Collection<CaseInsensitiveKey> keys = headers.keySet();
		ArrayList<String> names = new ArrayList<String>(keys.size());
		
		for(CaseInsensitiveKey key : keys) {
			names.add(key.getKey());
		}
		
		return names;
	}
	
	/**
	 * Returns all header values for the specified header name,
	 * 
	 * @param name the headr name
	 * @return all header values
	 */
	public Collection<String> getHeaders(String name) {
		CaseInsensitiveKey key = new CaseInsensitiveKey(name);
		return headers.get(key);
	}
	
	/**
	 * Sets all headers from the specified headers map to this part.
	 * 
	 * @param headers headers map
	 */
    void setHeaders(HashMap<CaseInsensitiveKey, List<String>> headers) {
    	this.headers = headers;
    }
    
    /**
     * Returns this parts name.
     * 
     * @return the name
     */
    public String getName() {
    	return this.name;
    }
    
    /**
     * Adds content data to this part from the specified content starting at the
     * specified offset and of specified length.
     * 
     * @param content the content to add
     * @param off the offset in the content to start from
     * @param len the number of bytes to add
     * @throws IOException if an I/O exception occurs
     */
	abstract void content(byte[] content, int off, int len) throws IOException ;
	
	/**
	 * Ends adding of content.
	 */
	abstract void end();
}
