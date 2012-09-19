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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.ParameterizedType;

import com.nginious.http.HttpException;
import com.nginious.http.HttpMethod;
import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.HttpService;
import com.nginious.http.HttpServiceResult;
import com.nginious.http.HttpStatus;
import com.nginious.http.application.Service;

public class InvokeRestService extends HttpService implements RestResponseAdapter {
	
	private SerializerFactory serializerFactory;
	
	private DeserializerFactory deserializerFactory;
	
	private RestService<?, ?> service;
	
	/**
	 * Constructs a new REST service.
	 */
	public InvokeRestService(RestService<?, ?> service) {
		super();
		this.service = service;
		this.serializerFactory = SerializerFactory.getInstance();
		this.deserializerFactory = DeserializerFactory.getInstance();
	}
	
	/**
	 * Invokes this REST service with the specified HTTP request and response.
	 */
	public final HttpServiceResult invoke(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		HttpServiceResult result = HttpServiceResult.DONE;
		
		try {
			ParameterizedType type = (ParameterizedType)service.getClass().getGenericSuperclass();
			Class<?> inClazz = (Class<?>)type.getActualTypeArguments()[0];
			Class<?> outClazz = (Class<?>)type.getActualTypeArguments()[1];
			Object inBean = null;
			
			HttpMethod method = request.getMethod();
			
			if(!inClazz.equals(Void.class)) {
				String contentType = request.getContentType();
				
				if(contentType == null && (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT))) {
					throw new HttpException(HttpStatus.BAD_REQUEST, "Content type is missing");
				}
				
				if(contentType != null) {
					Deserializer<?> deserializer = deserializerFactory.createDeserializer(inClazz, request.getContentType());
					
					if(deserializer == null) {
						throw new HttpException(HttpStatus.BAD_REQUEST, "Invalid content type '" + request.getContentType() + "'");
					}
					
					inBean = deserializer.deserialize(request);
				}
			}
			
			RestRequest<?> restRequest = service.createRestRequest(request, inBean);
			RestResponse<?> restResponse = service.createRestResponse(restRequest, response, this, outClazz);
			
			result = service.invoke(method, restRequest, restResponse);
			
			if(result == HttpServiceResult.DONE) {
				response(restResponse, outClazz, request.getHeader("Accept"));
			}
		} catch(SerializerException e) {
			response.setStatus(HttpStatus.BAD_REQUEST, "Serialization failed: " + e.getMessage());
		} catch(SerializerFactoryException e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR, "Serialization failed");
		}
		
		return result;
	}
	
	/**
	 * 
	 * @param restResponse
	 * @param outClazz
	 * @param acceptHeader
	 */
	void response(RestResponse<?> restResponse, Class<?> outClazz, String acceptHeader) {
		Object outBean = restResponse.getBean();
		
		try {
			if(outBean != null) {
				Serializer<?> serializer = serializerFactory.createSerializer(outClazz, acceptHeader);
				
				if(serializer == null) {
					restResponse.setStatus(HttpStatus.BAD_REQUEST, "No acceptable content type in '" + acceptHeader + "'");
					return;
				}
				
				restResponse.setContentType(serializer.getMimeType());
				restResponse.setCharacterEncoding("utf-8");
				
				ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				OutputStreamWriter outWriter = new OutputStreamWriter(byteOut, "utf-8");
				PrintWriter writer = new PrintWriter(outWriter);
				service.serialize(serializer, writer, outBean);
				writer.flush();
				
				byte[] data = byteOut.toByteArray();
				restResponse.setContentLength(data.length);
				OutputStream out = restResponse.getOutputStream();
				out.write(data);
			} else if(restResponse.getStatus() == HttpStatus.OK) {
				restResponse.setStatus(HttpStatus.NO_CONTENT);
			}
		} catch(UnsupportedEncodingException e) {
			restResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR, "Serialization failed");
		} catch(IOException e) {
			restResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR, "Write output failed");
		} catch(SerializerException e) {
			restResponse.setStatus(HttpStatus.BAD_REQUEST, "Serialization failed: " + e.getMessage());
		} catch(SerializerFactoryException e) {
			restResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR, "Serialization failed: " + e.getMessage());
		}		
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

	public void complete(RestResponse<?> service, Class<?> clazz, String acceptHeader) {
		response(service, clazz, acceptHeader);
	}
	
	public Service getService() {
		return service.getService();
	}
}
