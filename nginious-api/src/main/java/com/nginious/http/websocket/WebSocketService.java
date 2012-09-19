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

import com.nginious.http.HttpException;
import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.HttpService;

/**
 * A web socket service provides functionality for implementing dynamic handling of web socket messages. A web
 * socket service is bound to a URI path. When a web socket handshake is made to the URI path the server binds the
 * web socket service to the web socket connection and session.
 * 
 * <p>
 * Developers must subclass this class and override one or more of the following methods:
 * 
 * <ul>
 * <li>{@link #executeOpen(com.nginious.http.HttpRequest, com.nginious.http.HttpResponse, WebSocketSession)} - handles web socket handshake.</li>
 * <li>{@link #executeBinaryMessage(WebSocketBinaryMessage, WebSocketSession)} - handles binary messages.</li>
 * <li>{@link #executeTextMessage(WebSocketTextMessage, WebSocketSession)} - handles text messages.</li>
 * <li>{@link #executeClose(WebSocketSession)} - called when the web socket session is about to close.</li>
 * </ul>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public abstract class WebSocketService extends HttpService {
	
	/**
	 * Called by the server to notify this service that the specified web socket session has been opened.
	 * 
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @param session the opened web socket session
	 * @throws HttpException if a HTTP error occurs while executing
	 * @throws IOException if an I/O error occurs while executing
	 */
	public abstract void executeOpen(HttpRequest request, HttpResponse response, WebSocketSession session) throws HttpException, IOException;
	
	/**
	 * Called by the server to let this service handle the specified message received by the specified web socket
	 * session.
	 * 
	 * @param message the binary message
	 * @param session the web socket session
	 * @throws WebSocketException if unable to handle message
	 * @throws IOException if an I/O error occurs while handling message
	 */
	public abstract void executeBinaryMessage(WebSocketBinaryMessage message, WebSocketSession session) throws WebSocketException, IOException;
	
	/**
	 * Called by the server to let this service handle the specified message received by the specified web socket
	 * session.
	 * 
	 * @param message the text message
	 * @param session the web socket session
	 * @throws WebSocketException if unable to handle message
	 * @throws IOException if an I/O error occurs while handling message
	 */
	public abstract void executeTextMessage(WebSocketTextMessage message, WebSocketSession session) throws WebSocketException, IOException;
	
	/**
	 * Called by the server to notify this service that the specified web socket session is about to close.
	 * 
	 * @param session the web socket session
	 * @throws WebSocketException if unable to handle close
	 */
	public abstract void executeClose(WebSocketSession session) throws WebSocketException;
}
