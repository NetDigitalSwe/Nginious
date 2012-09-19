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

package com.nginious.http.application;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import com.nginious.http.HttpException;
import com.nginious.http.HttpMethod;
import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.HttpStatus;

/**
 * Handles serving of static content for a web application.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
class AppStaticContent {
	
	private static HashSet<HttpMethod> allowedMethods = new HashSet<HttpMethod>();
	
	static {
		allowedMethods.add(HttpMethod.HEAD);
		allowedMethods.add(HttpMethod.GET);
		allowedMethods.add(HttpMethod.OPTIONS);
	}
	
	private File baseDir;
	
	/**
	 * Constructs a new app static content.
	 */
	AppStaticContent() {
		super();
	}
	
	/**
	 * Sets web application base directory to the specified directory.
	 * 
	 * @param baseDir the web application base directory.
	 */
	void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}
	
	/**
	 * Handles serving of static content located in at the specified local path for specified HTTP request.
	 * The content is sent in specified HTTP response. The following HTTP methods are supported.
	 * 
	 * <ul>
	 * <li>HTTP GET - static content is served to the client.</li>
	 * <li>HTTP HEAD - only headers for static contentare sent to client.</li>
	 * <li>HTTP OPTIONS - send Allow header with possible HTTP methods to client.</li>
	 * </ul>
	 * 
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @param path the relative local path from the web applications base directory
	 * @throws IOException if unable to read static content
	 * @throws HttpException if a HTTP error occurs for example if content does not exist
	 */
	void execute(HttpRequest request, HttpResponse response, String path) throws IOException, HttpException {
		File contentFile = new File(this.baseDir, path);
		
		if(!contentFile.exists()) {
			throw new HttpException(HttpStatus.NOT_FOUND, path);			
		}
		
		HttpMethod method = request.getMethod();
		
		if(!allowedMethods.contains(method)) {
			throw new HttpException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed " + method);
		}
		
		if(method.equals(HttpMethod.OPTIONS)) {
			executeOptions(response);
			return;
		}
		
		StaticContent content = new StaticContent(this.baseDir, path);
		content.execute(request, response);
	}
	
	private void executeOptions(HttpResponse response) {
		response.setStatus(HttpStatus.OK);
		response.setContentLength(0);
		response.addHeader("Allow", "GET, HEAD, OPTIONS");
	}
}
