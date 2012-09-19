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

package com.nginious.http.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashSet;

import com.nginious.http.HttpCookie;
import com.nginious.http.HttpException;
import com.nginious.http.HttpStatus;
import com.nginious.http.session.HttpSessionManager;
import com.nginious.http.stats.HttpRequestStatistics;
import com.nginious.http.stats.WebSocketSessionStatistics;
import com.nginious.http.upload.UploadTracker;

/**
 * Handles a HTTP connection. Uses a HTTP parser to parse incoming data from the client and a HTTP context
 * to handle the request / response. If the connection is kept alive over several request / responses new
 * parsers and contexts are used.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 * @see HttpParser
 * @see HttpContext
 *
 */
class HttpConnection extends Connection {
	
	private int BUFFER_SIZE = 8192;
	
	private HttpServerImpl server;
	
	private HttpContextManager contextManager;
	
	private ByteBuffer buffer;
	
	private HttpParser parser;
	
	private HashSet<String> hostnames;
	
	private long lastAccessTimeMillis;
	
	private boolean pendingRead;
	
	private boolean trackerSet;
	
	/**
	 * Constructs a new HTTP connection with the specified HTTP server, socket channel, key and set of hostnames.
	 * 
	 * @param server the HTTP server that accepted the incoming client connection
	 * @param channel the socket channel for the client connection
	 * @param key the selection key
	 * @param hostnames list of server hostnames
	 * @throws IOException if unable to create HTTP connection
	 */
	HttpConnection(HttpServerImpl server, SocketChannel channel, SelectionKey key, HashSet<String> hostnames) throws IOException {
		super(server, channel, key);
		this.server = server;
		this.hostnames = hostnames;
		this.contextManager = server.getContextManager();
		this.buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
		this.parser = new HttpParser(server.getApplicationManagerImpl(), this, hostnames);
		this.lastAccessTimeMillis = System.currentTimeMillis();
		this.pendingRead = true;
	}
	
	/**
	 * Returns HTTP access log for this HTTP connection
	 * 
	 * @return the HTTP access log
	 */
	AccessLog getAccessLog() {
		return server.getAccessLog();
	}
	
	/**
	 * Returns HTTP server for this HTTP connection
	 * 
	 * @return the HTTP server
	 */
	HttpServerImpl getHttpServer() {
		return this.server;
	}
	
	/**
	 * Returns HTTP context for this HTTP connection
	 * 
	 * @return the HTTP context
	 */
	HttpContext getContext() {
		return parser.getContext();
	}
	
	/**
	 * Returns HTTP session manager for this HTTP connections server.
	 * 
	 * @return the HTTP session manager
	 */
	HttpSessionManager getSessionManager() {
		return server.getSessionManager();
	}
	
	/**
	 * Returns this HTTP connections monitor.
	 * 
	 * @return the HTTP request statistics
	 */
	HttpRequestStatistics getHttpRequestStatistics() {
		return server.getHttpRequestStatistics();
	}
	
	/**
	 * Returns web socket session statistics.
	 * 
	 * @return the web socket session statistics
	 */
	WebSocketSessionStatistics getWebSocketSessionStatistics() {
		return server.getWebSocketSessionStatistics();
	}
	
	/**
	 * Resets HTTP parser for this HTTP connection. A parser handles one request. If the connection
	 * is kept alive over several request / responses the parser must be reset to handle the next
	 * request.
	 */
	void resetParser() {
		clearUploadTracker();
		this.parser = new HttpParser(server.getApplicationManagerImpl(), this, this.hostnames);
		this.buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
		this.lastAccessTimeMillis = System.currentTimeMillis();
		this.pendingRead = true;
	}
	
	/**
	 * Clears HTTP parser.
	 */
	void clearParser() {
		clearUploadTracker();
		this.parser = null;
	}
	
	/*
	 * Sets upload tracker if tracker cookie exists
	 */
	void setUploadTracker(UploadTracker tracker) {
		HttpContext context = parser.getContext();
		HttpCookie trackerCookie = context.getCookie(HttpConstants.HTTP_UPLOAD_ID);
		
		if(trackerCookie != null) {
			getHttpServer().setUploadTracker(trackerCookie.getValue(), tracker);
			this.trackerSet = true;
		}
	}
	
	/*
	 * Removes any upload trackers associated with HTTP context for this HTTP connection.
	 */
	void clearUploadTracker() {
		if(!this.trackerSet) {
			return;
		}
		
		HttpContext context = parser.getContext();
		
		if(context == null) {
			return;
		}
		
		HttpCookie cookie = context.getCookie(HttpConstants.HTTP_UPLOAD_ID);
		
		if(cookie != null) {
			server.removeUploadTracker(cookie.getValue());
		}
	}
	
	/**
	 * Called by the HTTP server when data from the client is available for reading on the
	 * specified channel.
	 * 
	 * @param channel the socket channel where data is available for reading
	 * @throws IOException if unable to read data from channel
	 */
	protected void read(SocketChannel channel) throws IOException {
		try {
			int size = channel.read(this.buffer);
			
			if(size > 0) {
				this.pendingRead = false;
				
				if(parser.parse(buffer)) {
					// Should we block other requests while this one is processing?
					this.pendingRead = false;
			 		HttpContext context = parser.getContext();
					contextManager.manage(context);
				} else if(parser.sendExpect100Continue()) {
			 		HttpContext context = parser.getContext();
			 		context.sendExpect100Continue();
				} else if(parser.sendExpectationFailed()) {
			 		HttpContext context = parser.getContext();
                	String value = context.getHeader("Expect");
        			throw new HttpException(HttpStatus.EXPECTATION_FAILED, "unknown expect value " + value);					
				} else {
					server.queueRead(this);
				}
			} else if(size == -1) {
				close();
			}
		} catch(HttpException e) {
			e.printStackTrace();
	 		HttpContext context = parser.getContext();
			context.handleException(e);
			close();
			context.updateMonitorEntry();
		} catch(ClosedChannelException e) {
			close();
		} catch(Exception e) {
			e.printStackTrace();
	 		HttpContext context = parser.getContext();
			context.handleError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
		}
	}
	
	/**
	 * Returns whether or not this HTTP connection has timed out. A connection has timed out while waiting
	 * for the first data to read and the timeout has passed.
	 * 
	 * @return <code>true</code> if this connection has timed out, <code>false</code> otherwise
	 */
	protected boolean isTimedOut() {
		long connectionTimeoutMillis = server.getConnectionTimeoutMillis();
		return this.pendingRead && System.currentTimeMillis() - this.lastAccessTimeMillis > connectionTimeoutMillis;
	}
	
	/**
	 * Performs necessary cleanup on a timed out connection.
	 */
	protected void timedOut() {
		clearParser();
	}
}
