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

package com.nginious.http.xsp.expr;

import com.nginious.http.HttpRequest;

/**
 * A variable context which reads its variable values from HTTP request attributes.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class HttpRequestVariables implements Variables {
	
	private HttpRequest request;
	
	/**
	 * Constructs a new HTTP request variables from the specified HTTP request.
	 * 
	 * @param request the HTTP request
	 */
	public HttpRequestVariables(HttpRequest request) {
		this.request = request;
	}
	
	/**
	 * Returns an attribute with the specified name from the underlying HTTP request.
	 * 
	 * @return the value or <code>null</code> if no attribute with the given name exists
	 */
	public Object getVariable(String name) {
		return request.getAttribute(name);
	}
}
