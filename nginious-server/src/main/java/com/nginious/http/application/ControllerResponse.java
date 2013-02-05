package com.nginious.http.application;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

import com.nginious.http.HttpCookie;
import com.nginious.http.HttpException;
import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.HttpStatus;

class ControllerResponse implements HttpResponse {
	
	private ControllerService service;
	
	private HttpRequest request;
	
	private HttpResponse response;
	
	ControllerResponse(ControllerService service, HttpRequest request, HttpResponse response) {
		this.service = service;
		this.request = request;
		this.response = response;
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

	public void setCharacterEncoding(String encoding) {
		response.setCharacterEncoding(encoding);
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
		response.addHeader(name,  value);
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

	public void setData(Object data) {
		if(data == null) {
			response.setStatus(HttpStatus.NO_CONTENT);			
		} else if(data instanceof String) {
			String content = (String)data;
			response.setContentType("text/plain");
			response.setCharacterEncoding("utf-8");
			response.setContentLength(content.getBytes().length);
		} else {
			try {
				service.serialize(data, this.request, this.response);
			} catch(HttpException e) {
				setStatus(e.getStatus(), e.getMessage());
			} catch(IOException e) {
				setStatus(HttpStatus.INTERNAL_SERVER_ERROR, "Serialization failed");
			}
		}
	}

	public void completed() {
		response.completed();
	}

	public boolean isCommitted() {
		return response.isCommitted();
	}
}
