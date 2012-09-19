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

package com.nginious.http;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Enumeration of HTTP methods.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public enum HttpMethod {
	
	/**
	 * A request with method TRACE is used to invoke a remote loopback of the request message.
	 */
	TRACE,
	
	/**
	 * A request with method OPTIONS is a request for information about the HTTP methods
	 * supported by the resource identified by the request URI.
	 */
	OPTIONS,
	
	/**
	 * A request with method HEAD is identical to a GET method request but with no message body.
	 */
	HEAD,
	
	/**
	 * A request with method GET retrieves information identified by the request URI.
	 */
	GET,
	
	/**
	 * A request with method POST requests the server to accept and process the data in the message
	 * body.
	 */
	POST,
	
	/**
	 * A request with method PUT requests the server to accept and process the data in the message
	 * body. If an entity already exists with the same URI it should be replaced.
	 */
	PUT,
	
	/**
	 * A request with method DELETE requests the server to delete the resource identified by the request
	 * URI.
	 */
	DELETE;
	
	private static HashMap<String, HashSet<HttpMethod>> versionMethodSupport = new HashMap<String, HashSet<HttpMethod>>();

	static {
		HashSet<HttpMethod> methods = new HashSet<HttpMethod>();
		methods.add(HEAD);
		methods.add(GET);
		methods.add(POST);
		methods.add(PUT);
		methods.add(DELETE);
		methods.add(OPTIONS);
		methods.add(TRACE);
		versionMethodSupport.put("HTTP/1.1", methods);
		
		methods = new HashSet<HttpMethod>();
		methods.add(HEAD);
		methods.add(GET);
		methods.add(POST);
		versionMethodSupport.put("HTTP/1.0", methods);
		
		methods = new HashSet<HttpMethod>();
		methods.add(GET);
		versionMethodSupport.put("HTTP/0.9", methods);

	}
	
	/**
	 * Returns whether or not this HTTP method is supported in the specified HTTP version.
	 * 
	 * @param version the HTTP version. One of <code>HTTP/0.9</code>, <code>HTTP/1.0</code> or <code>HTTP/1.1</code>
	 * @return <code>true</code> if the method is supported, <code>false</code> otherwise
	 */
	public boolean isSupportedInHttpVersion(String version) {
		HashSet<HttpMethod> methods = versionMethodSupport.get(version);
		
		if(methods != null) {
			return methods.contains(this);
		}
		
		return false;
	}
	
	/**
	 * Returns whether or not this HTTP method is supported for serving static content.
	 * 
	 * @return <code>true</code> if this method is supported, <code>false</code> otherwise
	 */
	public boolean isSupportedByStaticContent() {
		return equals(HttpMethod.HEAD) || equals(HttpMethod.GET) || equals(HttpMethod.OPTIONS);
	}
	
	/**
	 * Returns whether or not if content is expected in this HTTP method.
	 * 
	 * @return whether or not content is expected
	 */
	public boolean isContentMethod() {
		return equals(HttpMethod.POST) || equals(HttpMethod.PUT);
	}
}
