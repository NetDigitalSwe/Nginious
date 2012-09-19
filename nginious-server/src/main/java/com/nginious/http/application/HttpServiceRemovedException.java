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

import com.nginious.http.HttpException;
import com.nginious.http.HttpStatus;

/**
 * A special HTTP exception used by {@link ReloadableHttpService} to indicate a removed
 * service class.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
class HttpServiceRemovedException extends HttpException {
	
	static final long serialVersionUID = 1L;
	
	/**
	 * Constructs a new HTTP service removed exception with the specified HTTP 
	 * response status.
	 * 
	 * @param status the HTTP response status
	 */
	public HttpServiceRemovedException(HttpStatus status) {
		super(status);
	}
	
	/**
	 * Constructs a new HTTP service removed exception with the specified HTTP response 
	 * status and message.
	 * 
	 * @param status the HTTP response status
	 * @param message the HTTP response message
	 */
	public HttpServiceRemovedException(HttpStatus status, String message) {
		super(status, message);
	}
	
	/**
	 * Constructs a new HTTP service removed exception with the specified HTTP response status, 
	 * message and cause.
	 * 
	 * @param status the HTTP response status
	 * @param message the HTTP response message
	 * @param cause the cause
	 */
	public HttpServiceRemovedException(HttpStatus status, String message, Throwable cause) {
		super(status, message, cause);
	}	
}
