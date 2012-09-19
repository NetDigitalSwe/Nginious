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
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.nginious.http.server.Connection;
import com.nginious.http.server.Server;
import com.nginious.http.websocket.WebSocketException;

/**
 * Handles a web socket connection over its lifecycle. Uses a web socket parser to parse incoming messages
 * from the client. Parsed messages are put on a queue for processing by a web socket session.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 * @see WebSocketSessionImpl
 * @see WebSocketParser
 * @see WebSocketMessageQueue
 *
 */
class WebSocketConnection extends Connection {
	
	private int BUFFER_SIZE = 8192;
	
	private Server server;
	
	private ByteBuffer buffer;	
	
	private WebSocketParser parser;
	
	private WebSocketSessionImpl session;
	
	private WebSocketMessageQueue queue;
	
	/**
	 * Constructs a new web socket connection.
	 */
	WebSocketConnection() {
		this.buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
		this.parser = new WebSocketParser();		
	}
	
	/**
	 * Constructs a new web socket connection using the specified server, socket channel, key and web socket
	 * session for handling incoming data.
	 * 
	 * @param server the server
	 * @param channel the socket channel.
	 * @param key the selection key
	 * @param session the web socket session
	 * @throws IOException if unable to create web socket connection
	 */
	WebSocketConnection(Server server, SocketChannel channel, SelectionKey key, WebSocketSessionImpl session) throws IOException {
		super(server, channel, key);
		this.server = server;
		this.buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
		this.parser = new WebSocketParser();
		this.session = session;
		this.queue = WebSocketMessageQueue.getInstance();
	}
	
	/**
	 * Sets server to the specified server for this web socket connection.
	 */
	protected void setServer(Server server) {
		this.server = server;
		super.setServer(this.server);
	}
	
	/**
	 * Sets socket channel to the specified channel for this web socket connection.
	 * 
	 * @param channel the socket channel
	 * @throws IOException if unable to set socket channel
	 */
	protected void setChannel(SocketChannel channel) throws IOException {
		super.setChannel(channel);
		channel.socket().setTcpNoDelay(true);
	}
	
	/**
	 * Sets web socket session to the specified session for this web socket connection
	 * 
	 * @param session the web socket session
	 */
	void setSession(WebSocketSessionImpl session) {
		this.session = session;
		this.queue = WebSocketMessageQueue.getInstance();
	}
	
	/**
	 * Reads incoming data from the specified socket channel, parses web socket messages and queues 
	 * them for execution when ready.
	 * 
	 * @param channel the socket channel to read from
	 * @throws IOException if unable to read from socket channel
	 */
	protected void read(SocketChannel channel) throws IOException {
		try {
			int size = channel.read(buffer);
			
			if(size > 0) {
				buffer.rewind();
				boolean more = true;
				
				while(more) {
					WebSocketMessage message = parser.parse(buffer, size);
					
					if(message != null) {
						queue.queue(this.session, message);
						more = buffer.limit() - buffer.remaining() < size;
					} else {
						server.queueRead(this);
						more = false;
					}
				}
			} else if(size == -1) {
				close();
			}
			
			buffer.rewind();
		} catch(WebSocketException e) {
			session.sendClose(e.getStatusCode(), e.getMessage());
		}
	}
	
	/**
	 * Returns whether or not this web socket connection has timed out.
	 * 
	 * @return <code>false</code>
	 */
	protected boolean isTimedOut() {
		return false;
	}
	
	/**
	 * Performs necessary cleanup when a connection had timed out.
	 */
	protected void timedOut() {
		return;
	}
}
