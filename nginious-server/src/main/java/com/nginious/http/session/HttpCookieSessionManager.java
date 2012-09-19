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
import com.nginious.http.common.PathParameters;

/**
 * A HTTP session manager which creates, serializes HTTP sessions into cookies and
 * deserializes HTTP sessions from cookies.
 *
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class HttpCookieSessionManager implements HttpSessionManager {
	
	/**
	 * Starts this HTTP cookie session manager.
	 */
	public void start() {
		return;
	}

	/**
	 * Stops this HTTP cookie session manager.
	 */
	public void stop() {
		return;
	}
	
	/**
	 * Deserializes HTTP session from cookies in specified HTTP request or creates a new HTTP session
	 * if no session cookies exist.
	 * 
	 * @param request the HTTP request to deserialize HTTP session from
	 * @param create whether or not to create a new session if no cookies exists in HTTP request
	 * @throws IOException if unable to deserialize HTTP session
	 */
	public HttpSession getSession(HttpRequest request, boolean create) throws IOException {
		HttpSessionImpl session = HttpCookieSessionDeserializer.deserialize(request);
		
		if(session == null && create) {
			session = new HttpSessionImpl();
		} else if(session != null) {
			session.setLastAccessedTime();
		}
		
		return session;
	}
	
	/**
	 * Serializes the specified HTTP session into cookies and stores them in the specified HTTP response. The
	 * cookie path is retrieved from the specified HTTP request.
	 * 
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @param session the HTTP session to serialize
	 * @throws IOException if unable to serialize HTTP session
	 */
	public void storeSession(HttpRequest request, HttpResponse response, HttpSession session) throws IOException {
		PathParameters params = new PathParameters(request);
		String path = params.get(0);
		
		if(path == null) {
			path = "/";
		} else {
			path = "/" + path;
		}
		
		if(session.isInvalidated()) {
			HttpCookieSessionSerializer.invalidate(session, request, response, path);			
		} else if(session.isNew()) {
			HttpCookieSessionSerializer.serialize(session, response, path);			
		}
	}
}
