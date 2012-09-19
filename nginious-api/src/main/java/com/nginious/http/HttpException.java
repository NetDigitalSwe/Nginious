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

import java.io.IOException;

/**
 * Thrown by classes that handle a HTTP request / response to indicate a HTTP error.
 * 
 * @author Bojan Pisler, NetDigital Sweden aB
 *
 */
public class HttpException extends IOException {

	static final long serialVersionUID = 1L;
	
	private HttpStatus status;
	
	/**
	 * Constructs a new HTTP exception with the specified HTTP response status.
	 * 
	 * @param status the HTTP response status
	 */
	public HttpException(HttpStatus status) {
		super();
		this.status = status;
	}
	
	/**
	 * Constructs a new HTTP exception with the specified HTTP response status and message.
	 * 
	 * @param status the HTTP response status
	 * @param message the HTTP response message
	 */
	public HttpException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}
	
	/**
	 * Constructs a new HTTP exception with the specified HTTP response status, message and cause.
	 * 
	 * @param status the HTTP response status
	 * @param message the HTTP response message
	 * @param cause the cause
	 */
	public HttpException(HttpStatus status, String message, Throwable cause) {
		super(message, cause);
		this.status = status;
	}
	
	/**
	 * Returns HTTP response status for this HTTP exception
	 * 
	 * @return the HTTP response status
	 */
	public HttpStatus getStatus() {
		return this.status;
	}
}
