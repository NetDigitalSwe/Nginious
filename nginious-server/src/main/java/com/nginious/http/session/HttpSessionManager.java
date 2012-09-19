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

package com.nginious.http.session;

import java.io.IOException;

import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.HttpSession;

/**
 * A HTTP session manager manages the creation, storing and retrieving of HTTP sessions.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public interface HttpSessionManager {
	
	/**
	 * Starts the HTTP session manager.
	 */
	public void start();
	
	/**
	 * Stops the HTTP session manager.
	 */
	public void stop();
	
	/**
	 * Gets HTTP session for the specified HTTP request. If no HTTP session exists one is created if
	 * the specified create flags is set to <code>true</code>.
	 * 
	 * @param request the HTTP request
	 * @param create whether or not to create a new HTTP session if none exists
	 * @return the retrieved or created HTTP session
	 * @throws IOException if unable to get or create HTTP session
	 */
	public HttpSession getSession(HttpRequest request, boolean create) throws IOException;
	
	/**
	 * Stores the specified HTTP session for later retrieval in another HTTP request.
	 * 
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @param session the HTTP session to store
	 * @throws IOException if unable to store HTTP session
	 */
	public void storeSession(HttpRequest request, HttpResponse response, HttpSession session) throws IOException;
}
