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

package com.nginious.http.server;

/**
 * Thrown to indicate a problem with parsing a URI.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class URIException extends Exception {

	static final long serialVersionUID = 1L;

	/**
	 * Constructs a new URI exception.
	 */
	public URIException() {
		super();
	}
	
	/**
	 * Constructs a new URI exception with the specified message and cause.
	 * 
	 * @param message the message
	 * @param cause the cause
	 */
	public URIException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new URI exception with the specified message.
	 * 
	 * @param message the message
	 */
	public URIException(String message) {
		super(message);
	}
	
	/**
	 * Constructs a new URI exception with the specified cause.
	 * 
	 * @param cause the cause
	 */
	public URIException(Throwable cause) {
		super(cause);
	}
}
