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

package com.nginious.http.xsp;

import java.io.IOException;

import com.nginious.http.HttpException;
import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.application.HttpService;
import com.nginious.http.application.HttpServiceResult;


/**
 * Handles execution of a XSP page as a HTTP service.
 * 
 * @author Bojan Pisler, NetDigital Sweden aB
 *
 */
public abstract class XspService extends HttpService {
	
	/**
	 * Called by the server to execute a HTTP GET for this XSP service using the specified HTTP request and response.
	 *
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @return a result indicating if execution is done or is asynchronous
	 * @throws HttpException if the HTTP request is invalid or if the service is unable to process the request
	 * @throws IOException if an I/O error occurs
	 */
    public final HttpServiceResult executeGet(HttpRequest request, HttpResponse response) throws IOException, HttpException {
        return executeXsp(request, response) ? HttpServiceResult.DONE : HttpServiceResult.ASYNC;
    }

	/**
	 * Called by the server to execute a HTTP POST for this XSP service using the specified HTTP request and response.
	 *
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @return a result indicating if execution is done or is asynchronous
	 * @throws HttpException if the HTTP request is invalid or if the service is unable to process the request
	 * @throws IOException if an I/O error occurs
	 */
    public final HttpServiceResult executePost(HttpRequest request, HttpResponse response) throws IOException, HttpException {
        return executeXsp(request, response) ? HttpServiceResult.DONE : HttpServiceResult.ASYNC;
    }

	/**
	 * Called by the server to execute a HTTP PUT for this XSP service using the specified HTTP request and response.
	 *
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @return a result indicating if execution is done or is asynchronous
	 * @throws HttpException if the HTTP request is invalid or if the service is unable to process the request
	 * @throws IOException if an I/O error occurs
	 */
    public final HttpServiceResult executePut(HttpRequest request, HttpResponse response) throws IOException, HttpException {
        return executeXsp(request, response) ? HttpServiceResult.DONE : HttpServiceResult.ASYNC;
    }

	/**
	 * Called by the server to execute a HTTP DELETE for this XSP service using the specified HTTP request and response.
	 *
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @return a result indicating if execution is done or is asynchronous
	 * @throws HttpException if the HTTP request is invalid or if the service is unable to process the request
	 * @throws IOException if an I/O error occurs
	 */
    public final HttpServiceResult executeDelete(HttpRequest request, HttpResponse response) throws IOException, HttpException {
        return executeXsp(request, response) ? HttpServiceResult.DONE : HttpServiceResult.ASYNC;
    }
    
    /**
     * Called by the server to execute this XSP service using the specified HTTP request and response.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @return <code>true</code> if execution is done, <code>false</code> if asynchronous
     * @throws HttpException if the HTTP request is invalid or if the service is unable to process the request
     * @throws IOException if an I/O error occurs
     */
    protected abstract boolean executeXsp(HttpRequest request, HttpResponse response) throws IOException, HttpException;
}
