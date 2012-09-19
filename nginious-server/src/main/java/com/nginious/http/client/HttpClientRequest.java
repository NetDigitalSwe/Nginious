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

package com.nginious.http.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.nginious.http.HttpMethod;

/**
 * A HTTP client request contains all information necessary to send a HTTP request to a HTTP server. This includes
 * HTTP version, method, path and headers.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class HttpClientRequest {
	
	private HttpMethod method;
	
	private String path;
	
	private int contentLength;
	
	private HashMap<String, List<String>> headers;
	
	/**
	 * Constructs a new HTTP client request.
	 */
	public HttpClientRequest() {
		super();
		this.headers = new HashMap<String, List<String>>();
	}
	
	/**
	 * Returns HTTP method for this HTTP client request.
	 * 
	 * @return the HTTP method
	 */
	public HttpMethod getMethod() {
		return this.method;
	}
	
	/**
	 * Sets the HTTP method to the specified method for this HTTP client request.
	 *  
	 * @param method the HTTP method
	 */
	public void setMethod(HttpMethod method) {
		this.method = method;
	}
	
	/**
	 * Returns URI path for this HTTP client request.
	 * 
	 * @return the URI path
	 */
	public String getPath() {
		return this.path;
	}
	
	/**
	 * Sets URI path to the specified path for this HTTP client request.
	 * 
	 * @param path the URI path
	 */
	public void setPath(String path) {
		this.path = path;
	}
	
	/**
	 * Returns value of content type header for this HTTP client request.
	 * 
	 * @return the content type header value or <code>null</code> if not set
	 */
	public String getContentType() {
		return getHeader("Content-Type");
	}
	
	/**
	 * Sets content length header value to the specified content length.
	 * 
	 * @param contentLength the content length
	 */
	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
		setHeader("Content-Length", Integer.toString(contentLength));
	}
	
	/**
	 * Returns value of content length header for this HTTP client request.
	 * 
	 * @return the content length header value or <code>0</code> if not set
	 */
	public int getContentLength() {
		return this.contentLength;
	}
	
	/**
	 * Sets value of content type header to the specified type for this HTTP client request.
	 * 
	 * @param type the content type
	 */
	public void setContentType(String type) {
		setHeader("Content-Type", type);
	}
	
	/**
	 * Adds a header with the specified name and value to this HTTP client request.
	 * 
	 * @param name the HTTP header name
	 * @param value the HTTP header value
	 */
	public void addHeader(String name, String value) {
		List<String> headerList = headers.get(name);
		
		if(headerList == null) {
			headerList = new ArrayList<String>();
			headers.put(name, headerList);
		}
		
		headerList.add(value);
	}
	
	/**
	 * Sets a header with the specified name and value to this HTTP client request. Any
	 * previously added or set headers with the same name are removed.
	 * 
	 * @param name the HTTP header name
	 * @param value the HTTP header value
	 */
	public void setHeader(String name, String value) {
		List<String> headerList = headers.get(name);
		
		if(headerList == null) {
			headerList = new ArrayList<String>();
			headers.put(name, headerList);
		} else {
			headerList.clear();
		}
		
		headerList.add(value);
	}
	
	/**
	 * Returns first value for HTTP header with the specified name.
	 * 
	 * @param name the HTTP header name
	 * @return HTTP header value or <code>null</code> if not set
	 */
	public String getHeader(String name) {
		List<String> headerList = headers.get(name);
		
		if(headerList != null && headerList.size() > 0) {
			return headerList.get(0);
		}
		
		return null;		
	}
	
	/**
	 * Returns all set or added header names for this HTTP client request.
	 * 
	 * @return all HTTP header names
	 */
	public String[] getHeaderNames() {
		return headers.keySet().toArray(new String[headers.size()]);
	}
	
	/**
	 * Returns all values for HTTP header with specified name.
	 * 
	 * @param name the HTTP header name
	 * @return all value or <code>null</code> if none set or added
	 */
	public String[] getHeaders(String name) {
		List<String> headerList = headers.get(name);
		
		if(headerList != null) {
			return headerList.toArray(new String[headerList.size()]);
		}
		
		return null;				
	}
}
