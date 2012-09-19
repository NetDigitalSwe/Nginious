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

import java.util.HashMap;

/**
 * An enumeration over frame opcodes for the websocket protocol. An opcode defines the
 * frame type in the websocket protocol.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public enum Opcode {
	
	/**
	 * A continuation frame opcode indicating that the frame is a part of message
	 * split into several frames where the first frame has opcode text or opcode
	 * binary. 
	 */
	CONTINUATION(0x00),
	
	/**
	 * A text frame opcode is set on the first frame in a text message that might
	 * be fragmented into several frames. Any frames following the first frame must
	 * have the continuation opcode set.
	 */
	TEXT(0x01),
	
	/**
	 * A binary frame opcode is set on the first frame in a binary message that might
	 * be fragmented into several frames. Any frames following the first frame must
	 * have the continuation opcode set.
	 */
	BINARY(0x02),
	
	/**
	 * A close frame opcode indicates to the receiveing endpoint that the sending
	 * endpoint wants to close the connection.
	 */
	CLOSE(0x08),
	
	/**
	 * A ping frame opcode indicates a ping message where the receiveing endpoint
	 * has to send back a pong message in a single frame.
	 */
	PING(0x09),
	
	/**
	 * A pong frame opcode indicates a pong message where an endpoint sends a
	 * pong message in response to a received ping message.
	 */
	PONG(0x0A);
	
	public int value;
	
	private static HashMap<Integer, Opcode> lookup = new HashMap<Integer, Opcode>();
	
	static {
		lookup.put(CONTINUATION.value, CONTINUATION);
		lookup.put(TEXT.value, TEXT);
		lookup.put(BINARY.value, BINARY);
		lookup.put(CLOSE.value, CLOSE);
		lookup.put(PING.value, PING);
		lookup.put(PONG.value, PONG);
	}
	
	/**
	 * Constructs a new opcode with the specified value.
	 * 
	 * @param value the opcode value
	 */
	private Opcode(int value) {
		this.value = value;
	}
	
	/**
	 * Returns opcode for the specified opcode value.
	 * 
	 * @param value the opcode value
	 * @return the opcode or <code>null</code> if no opcode exists for the given opcode value
	 */
	public static Opcode get(int value) {
		return lookup.get(value);
	}
	
	/**
	 * Returns whether or not this opcode is a control opcode. Control opcodes are ping, pong
	 * and close which are used for controling the websocket connection.
	 * 
	 * @return <code>true</code> if this is a control opcode, <code>false</code> otherwise
	 */
	public boolean isControl() {
		return this.value == 0x08 || this.value == 0x09 || this.value == 0x0A;
	}
	
	/**
	 * Returns whether or not this opcode is a data opcode. Data opcodes are binary and text
	 * which are used to indicate the payload type of a message.
	 * 
	 * @return <code>true</code> if this is a data opcode, <code>false</code> otherwise
	 */
	public boolean isData() {
		return this.value == 0x01 || this.value == 0x02;
	}
	
	/**
	 * Returns whether or not this opcode is a continuation opcode.
	 * 
	 * @return <code>true</code> if this is a continuation opcode, <code>false</code> otherwise
	 */
	public boolean isContinuation() {
		return this.value == 0x00;
	}
}
