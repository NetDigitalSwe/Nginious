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

package com.nginious.http.common;

import com.nginious.http.HttpRequest;

/**
 * Handles a URI path as a list of parameters.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class PathParameters {
	
	private String path;
	
	private String[] parameters;
	
	/**
	 * Constructs an new path parameters with the URI path from the specified HTTP request.
	 * 
	 * @param req the HTTPrequest
	 */
	public PathParameters(HttpRequest req) {
		super();
		this.path = req.getPath();
		
		String localPath = path;
		
		if(localPath.startsWith("/")) {
			localPath = localPath.substring(1);
		}
		
		this.parameters = localPath.split("/");
	}
	
	/**
	 * Returns URI path for this path parameters.
	 * 
	 * @return the URI path
	 */
	public String getPath() {
		return this.path;
	}
	
	/**
	 * Returns number of parameters in this path parameters.
	 * 
	 * @return number of parameters
	 */
	public int length() {
		return parameters != null ? parameters.length : 0;
	}
	
	/**
	 * Returns parameter at specified index.
	 * 
	 * @param index zero based parameter index
	 * @return the parameter or <code>null</code> if index is out of range
	 */
	public String get(int index) {
		return parameters == null || index >= parameters.length ? null : parameters[index];
	}
}
