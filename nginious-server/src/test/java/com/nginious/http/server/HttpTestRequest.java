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

package com.nginious.http.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.nginious.http.HttpCookie;
import com.nginious.http.HttpMethod;
import com.nginious.http.HttpRequest;
import com.nginious.http.HttpServiceResult;
import com.nginious.http.HttpSession;
import com.nginious.http.upload.FilePart;
import com.nginious.http.upload.UploadTracker;


public class HttpTestRequest implements HttpRequest {
	
	private HttpMethod method;
	
	private String version;
	
	private String path;
	
	private String queryString;
	
	private String charEncoding;
	
	private HashMap<String, HttpCookie> cookies;
	
	private HashMap<String, List<String>> headers;
	
	private HashMap<String, List<String>> params;
	
	private HashMap<String, Object> attributes;
	
	private byte[] body;
	
	private InputStream stream;
	
	private BufferedReader reader;
	
	public HttpTestRequest() {
		super();
		this.cookies = new HashMap<String, HttpCookie>();
		this.headers = new HashMap<String, List<String>>();
		this.params = new HashMap<String, List<String>>();
		this.attributes = new HashMap<String, Object>();
	}
	
	public String getHeader(String name) {
		List<String> values = headers.get(name);
		return values != null ? values.get(0) : null;
	}

	public String[] getHeaderNames() {
		return headers.keySet().toArray(new String[headers.size()]);
	}
	
	public void addHeader(String name, String value) {
		List<String> values = null;
		
		if(!headers.containsKey(name)) {
			values = new ArrayList<String>();
			headers.put(name, values);
		} else {
			values = headers.get(name);
		}
		
		values.add(value);
	}
	
	public HttpCookie getCookie(String name) {
		return cookies.get(name);
	}

	public HttpCookie[] getCookies() {
		return cookies.values().toArray(new HttpCookie[cookies.size()]);
	}
	
	public void addCookie(HttpCookie cookie) {
		cookies.put(cookie.getName(), cookie);
	}
	
	public void prepareUploadTracker() {
		return;
		
	}

	public UploadTracker getUploadTracker() {
		return null;
	}

	public HttpMethod getMethod() {
		return this.method;
	}
	
	public void setMethod(HttpMethod method) {
		this.method = method;
	}
	
	public String getVersion() {
		return this.version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getPath() {
		return this.path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getQueryString() {
		return this.queryString;
	}
	
	public HttpSession getSession() {
		return null;
	}
	
	public HttpSession getSession(boolean create) {
		return null;
	}
	
	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public String getCharacterEncoding() {
		return this.charEncoding;
	}
	
	public void setCharacterEncoding(String charEncoding) {
		this.charEncoding = charEncoding;
	}

	public int getContentLength() {
		return this.body == null ? 0 : body.length;
	}

	public String getContentType() {
		return getHeader("Content-Type");
	}
	
	public InputStream getInputStream() throws IOException {
		if(this.reader != null) {
			throw new IOException("Reader created");
		}
				
		if(this.stream == null) {
			byte[] content = this.body == null ? new byte[0] : this.body;
			this.stream = new ByteArrayInputStream(content);
		}
		
		return this.stream;
	}

	public BufferedReader getReader() throws IOException {
		if(this.stream != null) {
			throw new IOException("Stream created");
		}
		
		if(this.reader == null) {
			byte[] content = this.body == null ? new byte[0] : this.body;
			this.reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content)));
		}
		
		return this.reader;
	}
	
	public void setContent(byte[] body) {
		this.body = body;
		this.stream = null;
		this.reader = null;
	}
	
	public Locale getLocale() {
		return Locale.getDefault();
	}

	public String getParameter(String name) {
		List<String> list = params.get(name);
		return list != null ? list.get(0) : null;
	}

	public String[] getParameterValues(String name) {
		List<String> list = params.get(name);
		return list != null ? list.toArray(new String[list.size()]) : null;
	}

	public String[] getParameterNames() {
		return params.keySet().toArray(new String[params.size()]);
	}
	
	public void setAttribute(String name, Object value) {
		attributes.put(name, value);
	}
	
	public Object getAttribute(String name) {
		return attributes.get(name);
	}
	
	public Object removeAttribute(String name) {
		return attributes.remove(name);
	}
	
	public FilePart getFile(String name) {
		return null;
	}
	
	public Collection<FilePart> getFiles() {
		return null;
	}
	
	public void setParameter(String name, String value) {
		params.remove(name);
		addParameter(name, value);
	}
	
	public void addParameter(String name, String value) {
		List<String> values = null;
		
		if(!params.containsKey(name)) {
			values = new ArrayList<String>();
			params.put(name, values);
		} else {
			values = params.get(name);
		}
		
		values.add(value);
	}

	public String getProtocol() {
		return "http";
	}

	public String getScheme() {
		return "http";
	}

	public String getRemoteAddress() {
		return "localhost";
	}

	public String getRemoteHost() {
		return "127.0.0.1";
	}

	public int getRemotePort() {
		return 80;
	}
	
	public HttpServiceResult dispatch(String path) {
		return HttpServiceResult.DONE;
	}
}
