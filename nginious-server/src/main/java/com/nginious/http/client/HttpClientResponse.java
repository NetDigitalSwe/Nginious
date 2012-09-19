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

import com.nginious.http.HttpStatus;

/**
 * A HTTP client response contains all information returned in a HTTP response sent in response to a HTTP request 
 * including HTTP response line, headers and content.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class HttpClientResponse {
	
	private HttpStatus status;
	
	private String statusMsg;
	
	private int contentLength;
	
	private byte[] content;
	
	private HashMap<String, List<String>> headers;
	
	/**
	 * Constructs a new HTTP client response.
	 */
	public HttpClientResponse() {
		super();
		this.headers = new HashMap<String, List<String>>();
	}
	
	/**
	 * Returns HTTP status for this HTTP client response.
	 * 
	 * @return the HTTP status
	 */
	public HttpStatus getStatus() {
		return this.status;
	}
	
	/**
	 * Sets HTTP status code to the specified status for this HTTP client response.
	 * 
	 * @param status the HTTP status
	 */
	void setStatus(HttpStatus status) {
		this.status = status;
	}
	
	/**
	 * Returns HTTP status message for this HTTP client response.
	 * 
	 * @return the HTTP status message or <code>null</code> if not set
	 */
	public String getStatusMessage() {
		return this.statusMsg;
	}
	
	/**
	 * Sets HTTP status message to the specified status message for this HTTP client response.
	 * 
	 * @param statusMsg the HTTP status message
	 */
	void setStatusMessage(String statusMsg) {
		this.statusMsg = statusMsg;
	}
	
	/**
	 * Sets body content to the specified content for this HTTP client response
	 *  
	 * @param content the body content
	 */
	void setContent(byte[] content) {
		this.content = content;
	}
	
	/**
	 * Returns body content for this HTTP client response.
	 * 
	 * @return the body content
	 */
	public byte[] getContent() {
		return this.content;
	}
	
	/**
	 * Returns value of content type header for this HTTP client response.
	 * 
	 * @return value of content type header or <code>null</code> if not set
	 */
	public String getContentType() {
		return getHeader("Content-Type");
	}
	
	/**
	 * Sets content length to the specified content length for this HTTP client response.
	 * 
	 * @param contentLength the content length
	 */
	void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}
	
	/**
	 * Returns content length for this HTTP client response.
	 * 
	 * @return the content length
	 */
	public int getContentLength() {
		return this.contentLength;
	}
	
	/**
	 * Adds a header with the specified name and value to this HTTP client response.
	 * 
	 * @param name the HTTP header name
	 * @param value the HTTP header value
	 */
	void addHeader(String name, String value) {
		List<String> headerList = headers.get(name);
		
		if(headerList == null) {
			headerList = new ArrayList<String>();
			headers.put(name, headerList);
		}
		
		headerList.add(value);
	}
	
	/**
	 * Sets a header with the specified name and value to this HTTP client response. Any
	 * previously added or set headers with the same name are removed.
	 * 
	 * @param name the HTTP header name
	 * @param value the HTTP header value
	 */
	void setHeader(String name, String value) {
		List<String> headerList = headers.get(name);
		
		if(headerList == null) {
			headerList = new ArrayList<String>();
			headers.put(name, headerList);
		} else {
			headerList.clear();
		}
		
		headerList.set(0, value);
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
	 * Returns all set or added header names for this HTTP client response.
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
