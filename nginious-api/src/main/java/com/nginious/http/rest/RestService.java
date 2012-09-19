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

package com.nginious.http.rest;

import java.io.IOException;
import java.io.PrintWriter;

import com.nginious.http.HttpException;
import com.nginious.http.HttpMethod;
import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.HttpService;
import com.nginious.http.HttpServiceResult;
import com.nginious.http.HttpStatus;

/**
 * Extends {@link com.nginious.http.HttpService} with REST service semantics where data in HTTP requests is
 * automatically handled and deserialized into a bean of type K. The response bean of type T is automatically
 * serialized.
 * 
 * <p>
 * The deserialization mechanism uses the Content-Type HTTP header to determine format of the data in the
 * HTTP request. The following formats are supported.
 * 
 * <ul>
 * <li>JSON (application/json)</li>
 * <li>XML (text/xml)</li>
 * <li>Query (application/x-www-form-urlencoded)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * The serialization mechanism uses the Accept HTTP header to determine format of the data to serialize
 * the bean to. The following formats are supported.
 * 
 * <ul>
 * <li>JSON (application/json)</li>
 * <li>XML (text/xml)</li>
 * </ul>
 * </p>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 * @param <K> the type of bean deserialized from the HTTP request
 * @param <T> the type of bean serialized and returned in the HTTP response
 */
public abstract class RestService<K, T> extends HttpService {
	
	/**
	 * Constructs a new REST service.
	 */
	protected RestService() {
		super();
	}
	
	public final HttpServiceResult executeGet(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		return invoke(request, response);
	}

	public final HttpServiceResult executePost(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		return invoke(request, response);
	}

	public final HttpServiceResult executePut(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		return invoke(request, response);
	}

	public final HttpServiceResult executeDelete(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		return invoke(request, response);
	}

	/**
	 * Called by the server to execute a REST GET for this service using the specified HTTP request and response.
	 * 
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @return a result indicating if execution is done, should continue or is asynchronous
	 * @throws HttpException if the HTTP request is invalid or if the service is unable to process the request
	 * @throws IOException if an I/O error occurs
	 */
	public HttpServiceResult executeGet(RestRequest<K> request, RestResponse<T> response) throws HttpException, IOException {
		throw new HttpException(HttpStatus.METHOD_NOT_ALLOWED, "GET method not allowed");
	}
	
	public HttpServiceResult executePost(RestRequest<K> request, RestResponse<T> response) throws HttpException, IOException {
		throw new HttpException(HttpStatus.METHOD_NOT_ALLOWED, "POST method not allowed");
	}
	
	public HttpServiceResult executePut(RestRequest<K> request, RestResponse<T> response) throws HttpException, IOException {
		throw new HttpException(HttpStatus.METHOD_NOT_ALLOWED, "PUT method not allowed");
	}
	
	public HttpServiceResult executeDelete(RestRequest<K> request, RestResponse<T> response) throws HttpException, IOException {
		throw new HttpException(HttpStatus.METHOD_NOT_ALLOWED, "DELETE method not allowed");
	}
	
	@SuppressWarnings("unchecked")
	HttpServiceResult invoke(HttpMethod method, RestRequest<?> request, RestResponse<?> response) throws HttpException, IOException{
		RestRequest<K> restRequest = (RestRequest<K>)request;
		RestResponse<T> restResponse = (RestResponse<T>)response;
		HttpServiceResult result = null;
		
		if(method.equals(HttpMethod.GET) || method.equals(HttpMethod.HEAD)) {
			result = executeGet(restRequest, restResponse);
		} else if(method.equals(HttpMethod.POST)) {
			result = executePost(restRequest, restResponse);
		} else if(method.equals(HttpMethod.PUT)) {
			result = executePut(restRequest, restResponse);
		} else if(method.equals(HttpMethod.DELETE)) {
			result = executeDelete(restRequest, restResponse);
		} else {
			response.setStatus(HttpStatus.BAD_REQUEST, "Invalid HTTP method");
		}
		
		return result;
	}
	
	RestRequest<K> createRestRequest(HttpRequest request, Object inBean) {
		@SuppressWarnings("unchecked")
		RestRequest<K> restRequest = new RestRequest<K>(request, (K)inBean);
		return restRequest;
	}
	
	RestResponse<T> createRestResponse(HttpRequest request, HttpResponse response, RestResponseAdapter adapter, Class<?> outClazz) {
		@SuppressWarnings("unchecked")
		RestResponse<T> restResponse = new RestResponse<T>(response, adapter, this, (Class<T>)outClazz, request.getHeader("Accept"));
		return restResponse;
	}
	
	@SuppressWarnings("unchecked")
	void serialize(Serializer<?> serializer, PrintWriter writer, Object item) throws SerializerException {
		Serializer<T> typedSerializer = (Serializer<T>)serializer;
		T typedItem = (T)item;
		typedSerializer.serialize(writer, typedItem);
	}
}
