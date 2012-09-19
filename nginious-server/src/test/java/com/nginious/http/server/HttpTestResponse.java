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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.nginious.http.HttpCookie;
import com.nginious.http.HttpResponse;
import com.nginious.http.HttpStatus;


public class HttpTestResponse implements HttpResponse {
	
	private HashMap<String, List<String>> headers;
	
	private ArrayList<HttpCookie> cookies;
	
	private int contentLength;
	
	private ByteArrayOutputStream stream;
	
	private PrintWriter writer;
	
	private Locale locale;
	
	private String charset;
	
	private HttpStatus status;
	
	private String statusMsg;
	
	public HttpTestResponse() {
		super();
		this.headers = new HashMap<String, List<String>>();
		this.cookies = new ArrayList<HttpCookie>();
		this.status = HttpStatus.OK;
	}

	public String getCharacterEncoding() {
		return this.charset;
	}

	public String getContentType() {
		return getHeader("Content-Type");
	}

	public int getContentLength() {
		return this.contentLength;
	}

	public OutputStream getOutputStream() {
		if(this.stream == null) {
			this.stream = new ByteArrayOutputStream();
		}
		
		return this.stream;
	}

	public PrintWriter getWriter() {
		if(this.writer == null) {
			this.writer = new PrintWriter(getOutputStream());
		}
		
		return this.writer;
	}
	
	public byte[] getContent() throws IOException {
		if(this.writer != null) {
			writer.flush();
		}
		
		if(this.stream != null) {
			stream.flush();
			return stream.toByteArray();
		}
		
		return null;
	}
	
	public void setCharacterEncoding(String charset) {
		this.charset = charset;
	}

	public void setContentLength(int len) {
		this.contentLength = len;
		addHeader("Content-Length", Integer.toString(len));
	}

	public void setContentType(String type) {
		addHeader("Content-Type", type);
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	public Locale getLocale() {
		return this.locale;
	}
	
	public void addCookie(HttpCookie cookie) {
		cookies.add(cookie);
	}
	
	public HttpCookie[] getCookies() {
		return cookies.toArray(new HttpCookie[cookies.size()]);
	}
	
	public void addHeader(String name, String value) {
		List<String> values = null;
		
		if(headers.containsKey(name)) {
			values = headers.get(name);
		} else {
			values = new ArrayList<String>();
			headers.put(name, values);
		}
		
		values.add(value);
	}

	public String getHeader(String name) {
		List<String> values = headers.get(name);
		return values != null ? values.get(0) : null;
	}

	public String[] getHeaderNames() {
		return headers.keySet().toArray(new String[headers.size()]);
	}

	public String[] getHeaders(String name) {
		List<String> values = headers.get(name);
		return values != null ? values.toArray(new String[values.size()]) : null;
	}

	public HttpStatus getStatus() {
		return this.status;
	}
	
	public void setStatus(HttpStatus status) {
		this.status = status;
	}

	public void setStatus(HttpStatus status, String message) {
		this.status = status;
		this.statusMsg = message;
	}
	
	public void completed() {
		return;
	}
}
