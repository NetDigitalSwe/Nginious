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

import java.util.Collection;

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
public interface Part {
	
	/**
	 * Returns the first header value with the specified name for this part.
	 * 
	 * @param name the header name
	 * @return the first header value
	 */
	public String getHeader(String name);
	
	/**
	 * Returns all header names for this part.
	 * 
	 * @return all header names
	 */
	public Collection<String> getHeaderNames();
	
	/**
	 * Returns all header values for the specified header name,
	 * 
	 * @param name the headr name
	 * @return all header values
	 */
	public Collection<String> getHeaders(String name);
	
    /**
     * Returns this parts name.
     * 
     * @return the name
     */
    public String getName();
}
