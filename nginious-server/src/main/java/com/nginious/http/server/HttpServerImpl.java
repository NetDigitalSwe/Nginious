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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import com.nginious.http.application.ApplicationManager;
import com.nginious.http.application.ApplicationManagerImpl;
import com.nginious.http.session.HttpCookieSessionManager;
import com.nginious.http.session.HttpInMemorySessionManager;
import com.nginious.http.session.HttpSessionManager;
import com.nginious.http.stats.HttpRequestStatistics;
import com.nginious.http.stats.WebSocketSessionStatistics;
import com.nginious.http.upload.UploadTracker;

/**
 * An event based HTTP server which creates connections capable of handling the HTTP protocol.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class HttpServerImpl extends Server implements HttpServer {
	
	private static final long DEFAULT_CONNECTION_TIMEOUT_MILLIS = 30000L;
	
	private boolean started;
	
	private HttpContextManager contextManager;
	
	private HttpSessionManager sessionManager;
	
	private AccessLog accessLog;
	
	private ApplicationManagerImpl manager;
	
	private HashSet<String> hostnames;
	
	private HttpRequestStatistics httpRequestStatistics;
	
	private WebSocketSessionStatistics webSocketSessionStatistics;
	
	private ConcurrentHashMap<String, UploadTracker> trackers;
	
	private long connectionTimeoutMillis;
	
	/**
	 * Constructs a new HTTP server with default configuration.
	 * 
	 * @see HttpServerConfiguration
	 */
	HttpServerImpl() {
		this(new HttpServerConfiguration());
	}
	
	/**
	 * Constructs a new HTTP server with the specified configuration.
	 * 
	 * @param config the HTTP server configuration to use
	 */
	HttpServerImpl(HttpServerConfiguration config) {
		super("Http", config.getServerLogPath());
		this.httpRequestStatistics = new HttpRequestStatistics();
		this.webSocketSessionStatistics = new WebSocketSessionStatistics();
		this.trackers = new ConcurrentHashMap<String, UploadTracker>();
		
		this.contextManager = new HttpContextManager();
		
		if(config.getSession().equals("cookie")) {
			this.sessionManager = new HttpCookieSessionManager();
		} else {
			this.sessionManager = new HttpInMemorySessionManager();
		}
		
		this.started = false;
		this.accessLog = new AccessLog(config.getAccessLogPath());
		this.connectionTimeoutMillis = DEFAULT_CONNECTION_TIMEOUT_MILLIS;
		setPort(config.getPort());
	}
	
	UploadTracker getUploadTracker(String trackerId) {
		return trackers.get(trackerId);
	}
	
	void setUploadTracker(String trackerId, UploadTracker tracker) {
		trackers.put(trackerId, tracker);
	}
	
	void removeUploadTracker(String trackerId) {
		trackers.remove(trackerId);
	}
	
	void setApplicationManager(ApplicationManagerImpl manager) {
		this.manager = manager;
		manager.setHttpRequestStatistics(this.httpRequestStatistics);
		manager.setWebSocketSessionStatistics(this.webSocketSessionStatistics);
	}
	
	/**
	 * Returns access log for this HTTP server
	 * 
	 * @return the access log
	 */
	AccessLog getAccessLog() {
		return this.accessLog;
	}
	
	/**
	 * Returns application manager for this HTTP server.
	 * 
	 * @return the application context manager
	 */
	public ApplicationManager getApplicationManager() {
		return this.manager;
	}
	
	/**
	 * Returns application manager for this HTTP server.
	 * 
	 * @return the application context manager
	 */
	ApplicationManagerImpl getApplicationManagerImpl() {
		return this.manager;
	}
	
	/**
	 * Returns HTTP context manager for this HTTP server.
	 * 
	 * @return the HTTP context manager
	 */
	HttpContextManager getContextManager() {
		return this.contextManager;
	}
	
	/**
	 * Returns HTTP session manager for this HTTP server.
	 * 
	 * @return the HTTP session manager
	 */
	HttpSessionManager getSessionManager() {
		return this.sessionManager;
	}
	
	/**
	 * Returns HTTP request statistics for this HTTP server.
	 * 
	 * @return the HTTP request statistics
	 */
	public HttpRequestStatistics getHttpRequestStatistics() {
		return this.httpRequestStatistics;
	}
	
	/**
	 * Returns web socket session statistics for this HTTP server.
	 * 
	 * @return the web socket session statistics
	 */
	public WebSocketSessionStatistics getWebSocketSessionStatistics() {
		return this.webSocketSessionStatistics;
	}
	
	/**
	 * Returns connection timeout in milliseconds for this HTTP server. The connection timeout
	 * defines the approximate amount of time the server will wait before closing
	 *  
	 * @return connection timeout in milliseconds
	 */
	public long getConnectionTimeoutMillis() {
		return this.connectionTimeoutMillis;
	}
	
	public void setConnectionTimeoutMillis(long connectionTimeoutMillis) {
		this.connectionTimeoutMillis = connectionTimeoutMillis;
	}
	
	/**
	 * Starts this HTTP server including the app context manager, access log and network services.
	 * 
	 * @return <code>true</code> if server was started, <code>false</code> if server was already started
	 * @see com.nginious.http.server.Server#start()
	 */
	public boolean start() throws IOException {
		if(this.started) {
			return false;
		}
		
		boolean done = false;
		
		try {
			this.hostnames = getHostnames();
			sessionManager.start();
			manager.start();
			
			accessLog.open();
			done = super.start();
			return done;
		} finally {
			if(!done) {
				sessionManager.stop();
				manager.stop();
				accessLog.close();
			}
		}
	}
	
	/**
	 * Stops this HTTP server including the app context manager, access log and network services.
	 * 
	 * @see com.nginious.http.server.Server#stop()
	 */
	public boolean stop() throws IOException {
		if(super.stop()) {
			sessionManager.stop();
			manager.stop();
			accessLog.close();
			return true;
		}
		
		return false;
	}
	
	/**
	 * Creates a HTTP connection capable of handling the HTTP protocol for the specified socket channel and key.
	 * 
	 * @param channel the socket channel
	 * @param key the selection key
	 * @throws IOException if an I/O error occurs with the channel
	 */
	protected Connection createConnection(SocketChannel channel, SelectionKey key) throws IOException {
		return new HttpConnection(this, channel, key, this.hostnames);
	}
	
	/**
	 * Returns all hostnames associated with all network interfaces available on the server where this
	 * HTTP server is running.
	 * 
	 * @return the set of hostnames
	 * @throws IOException if unable to query network interfaces for hostnames
	 */
	private HashSet<String> getHostnames() throws IOException {
		Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
		HashSet<String> hostnames = new HashSet<String>();

		while(ifaces.hasMoreElements()) {
			NetworkInterface iface = ifaces.nextElement();
			
			if(!iface.isLoopback()) {
				Enumeration<InetAddress> inets = iface.getInetAddresses();
				
				while(inets.hasMoreElements()) {
					InetAddress inet = inets.nextElement();
					
					if(!inet.getCanonicalHostName().equalsIgnoreCase(inet.getHostAddress())) {
						String name = inet.getCanonicalHostName();
						hostnames.add(name);
					}
				}
			}
		}
		
		return hostnames;
	}	
}
