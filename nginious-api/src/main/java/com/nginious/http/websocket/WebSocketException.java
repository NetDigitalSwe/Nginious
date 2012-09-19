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

package com.nginious.http.websocket;

import java.io.IOException;

import com.nginious.http.websocket.StatusCode;

/**
 * Thrown to indicate a problem with a web socket connection or a web socket message.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class WebSocketException extends IOException {

	static final long serialVersionUID = 1L;
	
	private StatusCode status;
	
	/**
	 * Constructs a new web socket exception with the specified status code.
	 * 
	 * @param status the status code
	 */
	public WebSocketException(StatusCode status) {
		super();
		this.status = status;
	}
	
	/**
	 * Constructs a new web socket exception with the specified status code and message.
	 * 
	 * @param status the status code
	 * @param message the message
	 */
	public WebSocketException(StatusCode status, String message) {
		super(message);
		this.status = status;
	}
	
	/**
	 * Constructs a new web socket exception with the specified status code, message and cause.
	 * 
	 * @param status the status code
	 * @param message the message
	 * @param cause the cause
	 */
	public WebSocketException(StatusCode status, String message, Throwable cause) {
		super(message, cause);
		this.status = status;
	}
	
	/**
	 * Returns the status code for this web socket exception.
	 * 
	 * @return the status code
	 */
	public StatusCode getStatusCode() {
		return this.status;
	}
}
