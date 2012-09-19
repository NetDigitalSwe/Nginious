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
 * A web socket session handles the receiving and sending of messages between a web socket client and server
 * from the time the client connects until one of the endpoints closes the connection.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public interface WebSocketSession {
	
	/**
	 * Sends a close message to the client with the specified status code and message.
	 * 
	 * @param statusCode the status code
	 * @param msg the message
	 * @return <code>true</code> if close message was sent, <code>false</code> if connection is already closing
	 * @throws IOException if an I/O error occurs which the underlying connection
	 */
	public boolean sendClose(StatusCode statusCode, String msg) throws IOException;
	
	/**
	 * Sends a ping message to the client with the specified data as payload.
	 * 
	 * @param data the data to use as payload
	 * @return <code>true</code> if ping message sent, <code>false</code> if session is not open
	 * @throws IOException if an I/O error occurs which the underlying connection
	 */
	public boolean sendPing(byte[] data) throws IOException;
	
	/**
	 * Sends the specified data as a binary message to the client.
	 * 
	 * @param data the data to send
	 * @return <code>true</code> if message sent, <code>false</code> if session is not open
	 * @throws IOException if an I/O error occurs which the underlying connection
	 */
	public boolean sendBinaryData(byte[] data) throws IOException;

	/**
	 * Sends the specified text as a text message to the client.
	 * 
	 * @param data the data to send
	 * @return <code>true</code> if message sent, <code>false</code> if session is not open
	 * @throws IOException if an I/O error occurs which the underlying connection
	 */
	public boolean sendTextData(String data) throws IOException;
}
