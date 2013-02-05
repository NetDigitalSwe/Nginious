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

package com.nginious.http.application;

import java.io.IOException;

import com.nginious.http.HttpException;
import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.HttpStatus;
import com.nginious.http.annotation.Controller;
import com.nginious.http.server.DigestAuthentication;
import com.nginious.http.server.Header;
import com.nginious.http.server.HeaderException;
import com.nginious.http.server.HeaderParameter;
import com.nginious.http.server.HeaderParameters;

/**
 * An authentication filter to protect access to the REST base web application management services
 * provided by {@link Controller} and {@link ApplicationsController}. This filter uses digest authentication
 * as defined in <a href="http://www.ietf.org/rfc/rfc2617.txt">RFC 2617</a>.
 * 
 * <p>
 * The username is <code>admin</code> and the password is set during construction of this class.
 * See {@link com.nginious.http.server.Main} for how to set password on startup.
 * </p>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 * @see com.nginious.http.server.DigestAuthentication
 */
class ApplicationAuthenticationFilter extends HttpService {
	
	private String password;
	
	private DigestAuthentication auth;
	
	/**
	 * Constructs a new application authentication filter with the specified password.
	 * 
	 * @param password the password to use for
	 */
	ApplicationAuthenticationFilter(String password) {
		super();
		this.auth = new DigestAuthentication();
		this.password = password;
	}
	
	/**
	 * Authenticates the specified HTTP GET request.
	 * 
	 * @param request the HTTP request to authenticate
	 * @param response the HTTP response for sending authentication headers if needed
	 * @return {@link com.nginious.http.application.HttpServiceResult#CONTINUE} if authenticated, 
	 *  {@link com.nginious.http.application.HttpServiceResult#DONE} otherwise
	 * @throws HttpException if a HTTP error occurs while authenticating
	 * @throws IOException if an I/O error occurs
	 */
	public HttpServiceResult executeGet(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		if(authenticate(request, response)) {
			return HttpServiceResult.CONTINUE;
		}
		
		return HttpServiceResult.DONE;
	}

	/**
	 * Authenticates the specified HTTP POST request.
	 * 
	 * @param request the HTTP request to authenticate
	 * @param response the HTTP response for sending authentication headers if needed
	 * @return {@link com.nginious.http.application.HttpServiceResult#CONTINUE} if authenticated, 
	 *  {@link com.nginious.http.application.HttpServiceResult#DONE} otherwise
	 * @throws HttpException if a HTTP error occurs while authenticating
	 * @throws IOException if an I/O error occurs
	 */
	public HttpServiceResult executePost(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		if(authenticate(request, response)) {
			return HttpServiceResult.CONTINUE;
		}
		
		return HttpServiceResult.DONE;
	}

	/**
	 * Authenticates the specified HTTP PUT request.
	 * 
	 * @param request the HTTP request to authenticate
	 * @param response the HTTP response for sending authentication headers if needed
	 * @return {@link com.nginious.http.application.HttpServiceResult#CONTINUE} if authenticated, 
	 *  {@link com.nginious.http.application.HttpServiceResult#DONE} otherwise
	 * @throws HttpException if a HTTP error occurs while authenticating
	 * @throws IOException if an I/O error occurs
	 */
	public HttpServiceResult executePut(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		if(authenticate(request, response)) {
			return HttpServiceResult.CONTINUE;
		}
		
		return HttpServiceResult.DONE;
	}

	/**
	 * Authenticates the specified HTTP DELETE request.
	 * 
	 * @param request the HTTP request to authenticate
	 * @param response the HTTP response for sending authentication headers if needed
	 * @return {@link com.nginious.http.application.HttpServiceResult#CONTINUE} if authenticated, 
	 *  {@link com.nginious.http.application.HttpServiceResult#DONE} otherwise
	 * @throws HttpException if a HTTP error occurs while authenticating
	 * @throws IOException if an I/O error occurs
	 */
	public HttpServiceResult executeDelete(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		if(authenticate(request, response)) {
			return HttpServiceResult.CONTINUE;
		}
		
		return HttpServiceResult.DONE;
	}
	
	/**
	 * Authenticates the specified HTTP request.
	 * 
	 * @param request the HTTP request to authenticate
	 * @param response the HTTP response for sending authentication headers if needed
	 * @return <code>true</code> if authentication succeeded, <code>false</code> otherwise 
	 * @throws HttpException if a HTTP error occurs while authenticating
	 * @throws IOException if an I/O error occurs
	 */
	private boolean authenticate(HttpRequest request, HttpResponse response) throws HttpException {
		boolean result = false;
		
		try {
			String credentials = request.getHeader("Authorization");
			
			if(credentials != null) {
				Header header = new Header("Authorization", credentials);
				HeaderParameters params = header.getParameters();
				HeaderParameter usernameParam = params.getParameter("username");
				
				if(usernameParam != null) {
					String username = usernameParam.getValue();
					
					if(username.equals("admin")) {
						result = auth.response(credentials, request.getMethod(), this.password);
					}
				}
			}
		} catch(HeaderException e) {}
		
		if(!result) {
			String challenge = auth.challenge("admin", "admin");
			response.setStatus(HttpStatus.UNAUTHORIZED);
			response.addHeader("WWW-Authenticate", challenge);
		}
		
		return result;
	}	
}
