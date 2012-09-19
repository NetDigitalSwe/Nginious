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

package com.nginious.http;

import java.io.IOException;

import com.nginious.http.application.Service;

/**
 * A HTTP service provides functionality for implementing dynamic handling of HTTP requests. A HTTP service 
 * is bound to a URI path. When a HTTP request is made to the URI path the server invokes the HTTP service
 * to execute the HTTP request and return a HTTP response.
 * 
 * <p>
 * Developers must subclass this class and override one or more of the following methods:
 * 
 * <ul>
 * <li>{@link #executeGet(HttpRequest, HttpResponse)} - handles HTTP GET requests</li>
 * <li>{@link #executePost(HttpRequest, HttpResponse)} - handles HTTP POST requests</li>
 * <li>{@link #executePut(HttpRequest, HttpResponse)} - handles HTTP PUT requests</li>
 * <li>{@link #executeDelete(HttpRequest, HttpResponse)} - handled HTTP DELETE requests</li>
 * </ul>
 * 
 * Default functionality for these methods is to throw a {@link HttpException} with a status code
 * {@link HttpStatus#METHOD_NOT_ALLOWED}. This makes the server return a HTTP response with
 * the same status code to the client.
 * </p>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public abstract class HttpService {
	
	/**
	 * Invokes this HTTP service with the specified HTTP request and response. This method calls one of the
	 * {@link #executeGet(HttpRequest, HttpResponse)}, {@link #executePost(HttpRequest, HttpResponse)},
	 * {@link #executePut(HttpRequest, HttpResponse)} or {@link #executeDelete(HttpRequest, HttpResponse)}
	 * methods depending on the HTTP method returned by {@link HttpRequest#getMethod()}.
	 * 
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @return a result indicating if execution is done, should continue or is asynchronous.
	 * @throws HttpException if the HTTP request is invalid or if the service is unable to process the request
	 * @throws IOException if an I/O error occurs
	 */
	public HttpServiceResult invoke(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		HttpServiceResult result = HttpServiceResult.DONE;
		HttpMethod method = request.getMethod();

		if(method.equals(HttpMethod.GET) || method.equals(HttpMethod.HEAD)) {
			result = executeGet(request, response);
		} else if(method.equals(HttpMethod.POST)) {
			result = executePost(request, response);
		} else if(method.equals(HttpMethod.PUT)) {
			result = executePut(request, response);
		} else if(method.equals(HttpMethod.DELETE)) {
			result = executeDelete(request, response);
		} else {
			response.setStatus(HttpStatus.BAD_REQUEST, "Invalid HTTP method");
		}
		
		return result;
	}
	
	/**
	 * Called by the server to execute a HTTP GET for this service using the specified HTTP request and response.
	 * 
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @return a result indicating if execution is done, should continue or is asynchronous
	 * @throws HttpException if the HTTP request is invalid or if the service is unable to process the request
	 * @throws IOException if an I/O error occurs
	 */
	public HttpServiceResult executeGet(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		throw new HttpException(HttpStatus.METHOD_NOT_ALLOWED, "GET method not allowed");
	}
	
	/**
	 * Called by the server to execute a HTTP POST for this service using the specified HTTP request and response.
	 * 
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @return a result indicating if execution is done, should continue or is asynchronous
	 * @throws HttpException if the HTTP request is invalid or if the service is unable to process the request
	 * @throws IOException if an I/O error occurs
	 */
	public HttpServiceResult executePost(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		throw new HttpException(HttpStatus.METHOD_NOT_ALLOWED, "POST method not allowed");
	}
	
	/**
	 * Called by the server to execute a HTTP PUT for this service using the specified HTTP request and response.
	 * 
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @return a result indicating if execution is done, should continue or is asynchronous
	 * @throws HttpException if the HTTP request is invalid or if the service is unable to process the request
	 * @throws IOException if an I/O error occurs
	 */
	public HttpServiceResult executePut(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		throw new HttpException(HttpStatus.METHOD_NOT_ALLOWED, "PUT method not allowed");
	}
	
	/**
	 * Called by the server to execute a HTTP DELETE for this service using the specified HTTP request and response.
	 * 
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @return a result indicating if execution is done, should continue or is asynchronous
	 * @throws HttpException if the HTTP request is invalid or if the service is unable to process the request
	 * @throws IOException if an I/O error occurs
	 */
	public HttpServiceResult executeDelete(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		throw new HttpException(HttpStatus.METHOD_NOT_ALLOWED, "DELETE method not allowed");
	}
	
	/**
	 * Returns metadata for this HTTP service.
	 * 
	 * @return service metadata
	 */
	public Service getService() {
		return getClass().getAnnotation(Service.class);
	}
}
