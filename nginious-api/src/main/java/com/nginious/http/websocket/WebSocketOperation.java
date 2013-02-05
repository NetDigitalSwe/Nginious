/**
 * Copyright 2012, 2013 NetDigital Sweden AB
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

/**
 * Enumeration of web socket operations.
 * 
 * @author Bojan Pisler, Connode AB
 *
 */
public enum WebSocketOperation {
	
	/**
	 * An open operation signaling that a new websocket session has been opened.
	 */
	OPEN,
	
	/**
	 * A close operation which marks the closing of a websocket session.
	 */
	CLOSE,
	
	/**
	 * A text operation signaling that a text message has been received.
	 */
	TEXT,
	
	/**
	 * A binary operation signaling that a binary message has been received.
	 */
	BINARY;
}
