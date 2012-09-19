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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

import com.nginious.http.HttpCookie;
import com.nginious.http.HttpResponse;
import com.nginious.http.HttpStatus;

/**
 * Extends a HTTP response with methods to add bean for serialization in HTTP response.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 * @param <E> type of bean to serialize
 */
public class RestResponse<E> implements HttpResponse {
	
	@SuppressWarnings("unused")
	private RestService<?, E> service;
	
	private RestResponseAdapter adapter;
	
	private Class<E> clazz;
	
	private String acceptHeader;
	
	private HttpResponse response;
	
	private E bean;
	
	RestResponse(HttpResponse response, RestResponseAdapter adapter, RestService<?, E> service, Class<E> clazz, String acceptHeader) {
		this.response = response;
		this.adapter = adapter;
		this.service = service;
		this.clazz = clazz;
		this.acceptHeader = acceptHeader;
	}
	
	public void setBean(E bean) {
		this.bean = bean;
	}
	
	E getBean() {
		return this.bean;
	}
	
	public String getCharacterEncoding() {
		return response.getCharacterEncoding();
	}

	public String getContentType() {
		return response.getContentType();
	}

	public int getContentLength() {
		return response.getContentLength();
	}

	public OutputStream getOutputStream() {
		return response.getOutputStream();
	}

	public PrintWriter getWriter() {
		return response.getWriter();
	}

	public void setCharacterEncoding(String charset) {
		response.setCharacterEncoding(charset);
	}

	public void setContentLength(int len) {
		response.setContentLength(len);
	}

	public void setContentType(String type) {
		response.setContentType(type);
	}

	public void setLocale(Locale locale) {
		response.setLocale(locale);
	}

	public Locale getLocale() {
		return response.getLocale();
	}

	public void addCookie(HttpCookie cookie) {
		response.addCookie(cookie);
	}

	public HttpCookie[] getCookies() {
		return response.getCookies();
	}

	public void addHeader(String name, String value) {
		response.addHeader(name, value);
	}

	public String getHeader(String name) {
		return response.getHeader(name);
	}

	public String[] getHeaderNames() {
		return response.getHeaderNames();
	}

	public String[] getHeaders(String name) {
		return response.getHeaders(name);
	}

	public HttpStatus getStatus() {
		return response.getStatus();
	}

	public void setStatus(HttpStatus status) {
		response.setStatus(status);
	}

	public void setStatus(HttpStatus status, String message) {
		response.setStatus(status, message);
	}

	public void completed() {
		adapter.complete(this, this.clazz, this.acceptHeader);
		response.completed();
	}
}
