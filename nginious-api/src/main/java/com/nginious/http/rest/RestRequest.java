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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Locale;

import com.nginious.http.HttpCookie;
import com.nginious.http.HttpException;
import com.nginious.http.HttpMethod;
import com.nginious.http.HttpRequest;
import com.nginious.http.HttpServiceResult;
import com.nginious.http.HttpSession;
import com.nginious.http.upload.FilePart;
import com.nginious.http.upload.UploadTracker;

/**
 * Extends a HTTP request with methods to access deserialized bean from HTTP request.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 * @param <E> type of deserialized bean
 */
public class RestRequest<E> implements HttpRequest {
	
	private HttpRequest request;
	
	private E bean;
	
	RestRequest(HttpRequest request, E bean) {
		this.request = request;
		this.bean = bean;
	}
	
	public String getHeader(String name) {
		return request.getHeader(name);
	}
	
	public String[] getHeaderNames() {
		return request.getHeaderNames();
	}
	
	public HttpCookie getCookie(String name) {
		return request.getCookie(name);
	}
	
	public HttpCookie[] getCookies() {
		return request.getCookies();
	}
	
	public String getVersion() {
		return request.getVersion();
	}
	
	public HttpMethod getMethod() {
		return request.getMethod();
	}
	
	public String getPath() {
		return request.getPath();
	}
	
	public String getQueryString() {
		return request.getQueryString();
	}
	
	public String getCharacterEncoding() {
		return request.getCharacterEncoding();
	}
	
	public int getContentLength() {
		return request.getContentLength();
	}
	
	public String getContentType() {
		return request.getContentType();
	}
	
	public InputStream getInputStream() throws IOException {
		return request.getInputStream();
	}
	
	public BufferedReader getReader() throws IOException {
		return request.getReader();
	}
	
	public Locale getLocale() {
		return request.getLocale();
	}
	
	public String getParameter(String name) {
		return request.getParameter(name);
	}
	
	public String[] getParameterValues(String name) {
		return request.getParameterValues(name);
	}
	
	public String[] getParameterNames() {
		return request.getParameterNames();
	}
	
	public void setAttribute(String name, Object value) {
		request.setAttribute(name, value);
	}
	
	public Object getAttribute(String name) {
		return request.getAttribute(name);
	}
	
	public Object removeAttribute(String name) {
		return request.removeAttribute(name);
	}
	
	public void prepareUploadTracker() {
		request.prepareUploadTracker();
	}

	public UploadTracker getUploadTracker() {
		return request.getUploadTracker();
	}

	public FilePart getFile(String name) {
		return request.getFile(name);
	}
	
	public Collection<FilePart> getFiles() {
		return request.getFiles();
	}
	
	public String getProtocol() {
		return request.getProtocol();
	}
	
	public String getScheme() {
		return request.getScheme();
	}
	
	public String getRemoteAddress() {
		return request.getRemoteAddress();
	}
	
	public String getRemoteHost() {
		return request.getRemoteHost();
	}
	
	public int getRemotePort() {
		return request.getRemotePort();
	}
	
	public HttpSession getSession() {
		return request.getSession();
	}
	
	public HttpSession getSession(boolean create) {
		return request.getSession(create);
	}
	
	public HttpServiceResult dispatch(String path) throws HttpException, IOException {
		return request.dispatch(path);
	}

	public E getBean() {
		return this.bean;
	}
}
