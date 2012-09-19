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

/**
 * Parses web socket frames as specified in <a href="http://tools.ietf.org/html/rfc6455">RFC 6455 The WebSocket Protocol</a>.
 * The format below describes the format for a single frame carrying information for a complete or partial web socket message.
 * A frame consists of a header and payload. The header carries information about the payload including opcode, payload length 
 * and masking.
 * 
 * <pre>
 * 0                   1                   2                   3
 * * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-------+-+-------------+-------------------------------+
 * |F|R|R|R| opcode|M| Payload len |    Extended payload length    |
 * |I|S|S|S|  (4)  |A|     (7)     |             (16/64)           |
 * |N|V|V|V|       |S|             |   (if payload len==126/127)   |
 * | |1|2|3|       |K|             |                               |
 * +-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - +
 * |     Extended payload length continued, if payload len == 127  |
 * + - - - - - - - - - - - - - - - +-------------------------------+
 * |                               |Masking-key, if MASK set to 1  |
 * +-------------------------------+-------------------------------+
 * | Masking-key (continued)       |          Payload Data         |
 * +-------------------------------- - - - - - - - - - - - - - - - +
 * :                     Payload Data continued ...                :
 * + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
 * |                     Payload Data continued ...                |
 * +---------------------------------------------------------------+
 * </pre>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class WebSocketParser {
	
	private enum State {
		FLAGS, PAYLOAD_LEN_1, PAYLOAD_LEN_2, MASKING_KEY, PAYLOAD
	}
	
	private WebSocketMessageImpl dataMessage;
	
	private WebSocketMessageImpl controlMessage;
	
	private boolean finalFrame;
	
	private Opcode opcode;
	
	private byte[] mask;
	
	private int maskPos;
	
	private int tmpPayloadPos;
	
	private long tmpPayloadLength;
	
	private int payloadLength;
	
	private int payloadPosition;
	
	private State state;
	
	/**
	 * Constructs a new web socket parser.
	 */
	WebSocketParser() {
		super();
		this.state = State.FLAGS;
	}
	
	/**
	 * Parses the specified data.
	 * 
	 * @param data the data to parse
	 * @return a web socket message or <code>null</code> if more data is needed to reconstruct a message
	 * @throws WebSocketException if incoming data ins invalid
	 */
	WebSocketMessage parse(ByteBuffer data, int length) throws WebSocketException {
		int pos = data.position();
		
		while(pos < length) {
			byte ch = 0;
			
			switch(this.state) {
			case FLAGS:
				ch = data.get();
				
				// Check if any of the RSV1, RSV2 or RSV3 bits are set
				if((ch & 0x70) > 0) {
					throw new WebSocketException(StatusCode.PROTOCOL_ERROR, "RSV1-3 bits must be unset");
				}
				
				this.finalFrame = (ch & 0x80) > 0;
				int opcodeValue = ch & 0x0F;
				this.opcode = Opcode.get(opcodeValue);
				
				if(this.opcode == null) {
					throw new WebSocketException(StatusCode.PROTOCOL_ERROR, "Invalid opcode " + opcodeValue);
				}
				
				if(opcode.isData()) {
					if(this.dataMessage != null) {
						throw new WebSocketException(StatusCode.PROTOCOL_ERROR, "Final frame for previous data message not received");
					}
					
					if(opcode == Opcode.BINARY) {
						this.dataMessage = new WebSocketBinaryMessageImpl();
					} else if(opcode == Opcode.TEXT) {
						this.dataMessage = new WebSocketTextMessageImpl();
					}
				} else if(opcode.isControl()) {
					 if(!this.finalFrame) {					
						 throw new WebSocketException(StatusCode.PROTOCOL_ERROR, "Control frames must not be fragmented");
					 }
					 
					 this.controlMessage = new WebSocketBinaryMessageImpl(this.opcode);
				} else if(opcode.isContinuation()) {
					if(this.dataMessage == null) {
						throw new WebSocketException(StatusCode.PROTOCOL_ERROR, "No previous data frame received for continuation frame");
					}
				}
				
				this.state = State.PAYLOAD_LEN_1;
				break;
			
			case PAYLOAD_LEN_1:
				ch = data.get();
				boolean masked = (ch & 0x80) > 0;
				
				if(!masked) {
					throw new WebSocketException(StatusCode.PROTOCOL_ERROR, "Frames must be masked");
				}
				
				this.payloadLength = ch & 0x7F;
				
				if(opcode.isControl() && this.payloadLength > 125) {
					throw new WebSocketException(StatusCode.PROTOCOL_ERROR, "Maximum control frame size is 125 bytes");
				}
				
				if(this.payloadLength == 126) {
					this.state = State.PAYLOAD_LEN_2;
					this.tmpPayloadPos = 16;
				} else if(this.payloadLength == 127) {
					this.state = State.PAYLOAD_LEN_2;
					this.tmpPayloadPos = 64;
				} else {
					this.mask = new byte[4];
					this.state = State.MASKING_KEY;
				}
				break;
			
			case PAYLOAD_LEN_2:
				ch = data.get();
				this.tmpPayloadPos -= 8;
				this.tmpPayloadLength += ((ch & 0xFF) << this.tmpPayloadPos);
				
				if(this.tmpPayloadPos == 0) {
					if(this.tmpPayloadLength > 2097152) {
						throw new WebSocketException(StatusCode.MESSAGE_TOO_BIG, "Maximum frame size is 2097152 bytes");
					}
					
					this.payloadLength = (int)this.tmpPayloadLength;
					this.mask = new byte[4];
					this.state = State.MASKING_KEY;
				}
				break;
			
			case MASKING_KEY:
				ch = data.get();
				mask[this.maskPos++] = ch;
				
				if(this.maskPos == 4) {
					this.state = State.PAYLOAD;
				}
				break;
				
			case PAYLOAD:
				int payloadRemain = this.payloadLength - this.payloadPosition;
				
				if(payloadRemain == 0) {
					this.state = State.FLAGS;
					
					if(this.finalFrame) {
						WebSocketMessageImpl outMessage = null;
						
						if(opcode.isControl()) {
							outMessage = this.controlMessage;
							this.controlMessage = null;
						} else {
							outMessage = this.dataMessage;
							this.dataMessage = null;
						}
						
						outMessage.compact();
						outMessage.verify();
						reset();
						return outMessage;
					}
				} else {
					if(payloadRemain > length - data.position() + 1) {
						payloadRemain = length - data.position();
					}
					
					byte[] payload = new byte[(int)payloadRemain];
					data.get(payload);
					unmask(payload);
					
					if(opcode.isControl()) {
						controlMessage.appendPayload(payload);
					} else {
						if(!dataMessage.appendPayload(payload)) {
							throw new WebSocketException(StatusCode.MESSAGE_TOO_BIG, "Maximum message size is 2097152 bytes");
						}
					}
					
					this.payloadPosition += payloadRemain;
					
					if(this.payloadPosition == this.payloadLength) {
						WebSocketMessageImpl outMessage = null;
						
						if(opcode.isControl()) {
							outMessage = this.controlMessage;
							this.controlMessage = null;
						} else {
							outMessage = this.dataMessage;
							this.dataMessage = null;
						}
						
						outMessage.compact();
						outMessage.verify();
						reset();
						return outMessage;
					}
					
					pos += payloadRemain - 1; // One added below
				}
			}
			
			pos++;
		}
		
		return null;
	}
	
	/**
	 * Unmasks the specified data.
	 * 
	 * @param data the data to unmask
	 */
	private void unmask(byte[] data) {
		for(int i = 0; i < data.length; i++) {
			int maskPos = ((int)(this.payloadPosition + i) % 4);
			data[i] = (byte)(data[i] ^ mask[maskPos]);
		}
	}
	
	/**
	 * Resets this web socket parser which makes it ready to parse a new frame.
	 */
	private void reset() {
		this.finalFrame = false;;
		this.opcode = null;
		this.mask = null;
		this.maskPos = 0;
		this.payloadLength = 0;
		this.payloadPosition = 0;
		this.tmpPayloadLength = 0;
		this.state = State.FLAGS;
	}
}
