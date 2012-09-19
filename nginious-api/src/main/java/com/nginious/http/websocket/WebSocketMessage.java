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

/**
 * A web socket message is sent from one endpoint to another and consists of an
 * opcode and payload. The action that needs to be taken by the receiving endpoint
 * depends on the opcode.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 * @see Opcode
 */
public interface WebSocketMessage {
	
	/**
	 * Returns the opcode for this web socket message.
	 * 
	 * @return the opcode
	 */
	public Opcode getOpcode();
	
	/**
	 * Returns payload length of this web socket message.
	 * 
	 * @return the payload length
	 */
	public long length();
}
