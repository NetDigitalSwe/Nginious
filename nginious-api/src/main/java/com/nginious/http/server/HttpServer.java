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

import com.nginious.http.application.ApplicationManager;

/**
 * HTTP server interface.
 * 
 * <p>
 * Example HTTP server setup
 * 
 * <pre>
 * try {
 *   HttpServerConfiguration config = new HttpServerConfiguration();
 *   config.setPort(8080);
 *   config.setWebappsDir("apps");
 *   
 *   HttpServer server = HttpServerFactory.create(config);
 *   server.start();
 * } catch(IOException e) {
 *   e.printStackTrace();
 * }
 * </pre>
 * </p>
 * 
 * @author Bojan Pisler, NetDigital Sweden
 *
 */
public interface HttpServer {
	
	/**
	 * Starts this HTTP server.
	 * 
	 * @return <code>true</code> if server was started, <code>false</code> if already started
	 * @throws IOException if unable to start HTTP server
	 */
	public boolean start() throws IOException;
	
	/** 
	 * Stops this HTTP server.
	 * 
	 * @return <code>true</code> if server was stopped, <code>false</code> if already stopped
	 * @throws IOException if unable to stop HTTP server.
	 */
	public boolean stop() throws IOException;
	
	/**
	 * Returns application manager for this HTTP server.
	 * 
	 * @return the application manager
	 */
	public ApplicationManager getApplicationManager();	
}
