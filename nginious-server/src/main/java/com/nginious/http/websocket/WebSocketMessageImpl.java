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

import com.nginious.http.common.Buffer;
import com.nginious.http.common.ExpandableBuffer;

abstract class WebSocketMessageImpl implements WebSocketMessage {
	
	protected Opcode opcode;
	
	protected Buffer message;
	
	/**
	 * Constructs a new web socket message with the specified opcode.
	 * 
	 * @param opcode the message opcode
	 */
	public WebSocketMessageImpl(Opcode opcode) {
		this.opcode = opcode;
		this.message = new ExpandableBuffer(2097152);
	}
	
	/**
	 * Returns the opcode for this web socket message.
	 * 
	 * @return the opcode
	 */
	public Opcode getOpcode() {
		return this.opcode;
	}
	
	/**
	 * Returns payload length of this web socket message.
	 * 
	 * @return the payload length
	 */
	public abstract long length();
	
	/**
	 * Appends the specified byte array as payload to this web socket message.
	 * 
	 * @param payload the payload to append
	 * @return whether or not all payload data was appended
	 */
	boolean appendPayload(byte[] payload) {
		int added = message.put(payload);
		return added == payload.length;
	}
	
	/**
	 * Compacts message payload.
	 */
	void compact() {
		message.compact();
	}
	
	/**
	 * Verifies the payload of this web socket message.
	 * 
	 * @throws WebSocketException if payload is invalid
	 */
	abstract void verify() throws WebSocketException;
}
