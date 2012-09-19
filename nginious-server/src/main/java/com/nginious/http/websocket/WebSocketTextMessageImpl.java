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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * A text web socket message has an opcode {@link Opcode#TEXT} and carries a text
 * payload.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
class WebSocketTextMessageImpl extends WebSocketMessageImpl implements WebSocketTextMessage {
	
	private String textMessage;
	
	/**
	 * Constructs a new web socket text message.
	 */
	WebSocketTextMessageImpl() {
		super(Opcode.TEXT);
	}
	
	/**
	 * Verifies that this web socket text messages payload is utf-8 encoded text.
	 * 
	 * @throws WebSocketException if the payload is not utf-8 encoded text
	 */
	void verify() throws WebSocketException {
		try {
			ByteBuffer buff = message.toByteBuffer();
			Charset charset = Charset.forName("utf-8");
			CharsetDecoder decoder = charset.newDecoder();
			CharBuffer chBuff = decoder.decode(buff);
			this.textMessage = chBuff.toString();
		} catch(CharacterCodingException e) {
			throw new WebSocketException(StatusCode.INVALID_FRAME_PAYLOAD_DATA, "Invalid utf-8 text in message");
		}
	}
	
	/**
	 * Returns the text payload in this web socket text message.
	 * 
	 * @return the text payload
	 */
	public String getMessage() {
		return this.textMessage;
	}
	
	/**
	 * Returns the payload length of this web socket text message.
	 * 
	 * @return the payload length
	 */
	public long length() {
		return textMessage.length();
	}
}
