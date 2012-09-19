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

/**
 * Thrown by {@link HttpClient} to indicate a HTTP error.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class HttpClientException extends Exception {
	
	static final long serialVersionUID = 1L;
	
	/**
	 * Constructs a new HTTP client exception with the specified message.
	 * 
	 * @param message the message
	 */
	public HttpClientException(String message) {
		super(message);
	}

	/**
	 * Constructs a new HTTP client exception with the specified message and cause.
	 * 
	 * @param message the message
	 * @param cause the cause
	 */
	public HttpClientException(String message, Throwable cause) {
		super(message, cause);
	}
}
