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
import java.util.LinkedList;

import com.nginious.http.HttpException;
import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;

/**
 * A controller chain executes a list of controllers in order.
 * 
 * <p>
 * A typical example for chains is to prepend a HTTP authentication
 * controller in front of another service. The prepended authentication controller is used to
 * password protect another controller.
 * </p>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
class ControllerChain extends HttpService {
	
	private LinkedList<HttpService> chain;
	
	/**
	 * Constructs a new controller chain
	 */
	ControllerChain() {
		super();
		this.chain = new LinkedList<HttpService>();
	}
	
	/**
	 * Adds the specified controller HTTP service to the end of this chain.
	 * 
	 * @param service the controllerHTTP service to add
	 */
	void addServiceLast(HttpService service) {
		chain.addLast(service);
	}
	
	/**
	 * Adds the specified controller HTTP service first in this chain.
	 * 
	 * @param service the controller HTTP service to add
	 */
	void addServiceFirst(HttpService service) {
		chain.addFirst(service);
	}
	
	/**
	 * Removes the specified controller HTTP service from this chain.
	 * 
	 * @param service the controller HTTP service to remove
	 */
	void removeService(HttpService service) {
		chain.remove(service);
	}
	
	/**
	 * Returns all controller HTTP services in this chain.
	 * 
	 * @return all controller HTTP services
	 */
	HttpService[] getServices() {
		return chain.toArray(new HttpService[chain.size()]);
	}
	
	/**
	 * Executes HTTP GET for all controller HTTP services in this chain in order until a service returns {@link HttpServiceResult#DONE}
	 * or the end of the chain is reached.
	 * 
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @return a result indicating if execution is done, should continue or is asynchronous
	 * @throws HttpException if the HTTP request is invalid or if unable to process the request
	 * @throws IOException if an I/O error occurs
	 */
	public HttpServiceResult executeGet(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		HttpServiceResult result = null;
		
		for(HttpService service : chain) {
			result = service.executeGet(request, response);
			
			if(result == HttpServiceResult.DONE) {
				return result;
			}
		}
		
		return result;
	}
	
	/**
	 * Executes HTTP POST for all controller HTTP services in this chain in order until a service returns {@link HttpServiceResult#DONE}
	 * or the end of the chain is reached.
	 * 
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @return a result indicating if execution is done, should continue or is asynchronous
	 * @throws HttpException if the HTTP request is invalid or if unable to process the request
	 * @throws IOException if an I/O error occurs
	 */
	public HttpServiceResult executePost(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		HttpServiceResult result = null;
		
		for(HttpService service : chain) {
			result = service.executePost(request, response);
			
			if(result == HttpServiceResult.DONE) {
				return result;
			}
		}
		
		return result;
	}

	/**
	 * Executes HTTP PUT for all controller HTTP services in this chain in order until a service returns {@link HttpServiceResult#DONE}
	 * or the end of the chain is reached.
	 * 
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @return a result indicating if execution is done, should continue or is asynchronous
	 * @throws HttpException if the HTTP request is invalid or if unable to process the request
	 * @throws IOException if an I/O error occurs
	 */
	public HttpServiceResult executePut(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		HttpServiceResult result = null;
		
		for(HttpService service : chain) {
			result = service.executePut(request, response);
			
			if(result == HttpServiceResult.DONE) {
				return result;
			}
		}
		
		return result;
	}

	/**
	 * Executes HTTP DELETE for all controller HTTP services in this chain in order until a service returns {@link HttpServiceResult#DONE}
	 * or the end of the chain is reached.
	 * 
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @return a result indicating if execution is done, should continue or is asynchronous
	 * @throws HttpException if the HTTP request is invalid or if unable to process the request
	 * @throws IOException if an I/O error occurs
	 */
	public HttpServiceResult executeDelete(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		HttpServiceResult result = null;
		
		for(HttpService service : chain) {
			result = service.executeDelete(request, response);
			
			if(result == HttpServiceResult.DONE) {
				return result;
			}
		}
		
		return result;
	}
}
