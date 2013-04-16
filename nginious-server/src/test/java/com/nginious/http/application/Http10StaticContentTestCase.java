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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.nginious.http.common.FileUtils;
import com.nginious.http.server.FileLogConsumer;
import com.nginious.http.server.HttpServer;
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;
import com.nginious.http.server.HttpTestConnection;

public class Http10StaticContentTestCase extends TestCase {
	
	private HttpServer server;
	
    public Http10StaticContentTestCase() {
		super();
	}

	public Http10StaticContentTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		File webappsDir = new File("build/test-webapps");
		webappsDir.mkdirs();
		
		HttpServerConfiguration config = new HttpServerConfiguration();
		config.setWebappsDir("build/test-webapps");
		config.setServerLogPath("build/test-server.log");
		config.setPort(9000);
		HttpServerFactory factory = HttpServerFactory.getInstance();
		this.server = factory.create(config);
		server.setAccessLogConsumer(new FileLogConsumer("build/test-access"));
		server.start();
		ApplicationManager manager = server.getApplicationManager();
		Application application = manager.createApplication("test");
		application.setBaseDir(new File("build/resources/testweb"));
		manager.publish(application);
	}

	protected void tearDown() throws Exception {
		if(this.server != null) {
			server.stop();
		}
		
		FileUtils.deleteDir("build/test-webapps");
	}
	
	public void testStaticContent() throws Exception {
		String request = "GET /test/static/test.txt HTTP/1.0\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: text/plain\015\012" +
			"Last-Modified: <modified>\015\012" + 
			"Content-Length: 8\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"test.txt";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			String response = conn.readString();

			expectedResponse = conn.setHeaders(response, expectedResponse);
			assertEquals(expectedResponse, response);			
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public void testInvalidMethod() throws Exception {
		String request = "POST /test/static/test.txt HTTP/1.0\015\012" + 
			"Host: localhost\015\012" +
			"If-None-Match: <match>\015\012" + 
			"Content-Length: 4\015\012" + 
			"Connection: close\015\012\015\012" +
			"Test";
		
		String expectedResponse = "HTTP/1.1 405 Not Allowed\015\012" +
			"Content-Type: text/html; charset=utf-8\015\012" +
			"Content-Length: 75\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"<html><body><h1>405 Not Allowed: method not allowed POST</h1></body></html>";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			
			SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			request = request.replace("<match>", "DD18BF3A8E0A2A3E53E2661C7FB53535");
			conn.write(request);
			
			String response = conn.readString();
			expectedResponse = conn.setHeaders(response, expectedResponse);
			assertEquals(expectedResponse, response);			
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public void testIfModifiedSince1() throws Exception {
		String request = "GET /test/static/test.txt HTTP/1.0\015\012" + 
			"Host: localhost\015\012" +
			"If-Modified-Since: <modified>\015\012" + 
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponse = "HTTP/1.1 304 Not Modified\015\012" +
			"Content-Length: 0\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			
			SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			request = request.replaceFirst("<modified>", format.format(new Date()));
			
			conn.write(request);
			
			String response = conn.readString();
			expectedResponse = conn.setHeaders(response, expectedResponse);
			assertEquals(expectedResponse, response);			
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public void testIfModifiedSince2() throws Exception {
		String request = "GET /test/static/test.txt HTTP/1.0\015\012" + 
			"Host: localhost\015\012" +
			"If-Modified-Since: <modified>\015\012" + 
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: text/plain\015\012" +
			"Last-Modified: <modified>\015\012" + 
			"Content-Length: 8\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"test.txt";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			
			Calendar modifiedCal = Calendar.getInstance();
			modifiedCal.add(Calendar.YEAR, -2);
			SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			request = request.replaceFirst("<modified>", format.format(modifiedCal.getTime()));
			
			conn.write(request);
			
			String response = conn.readString();
			expectedResponse = conn.setHeaders(response, expectedResponse);
			assertEquals(expectedResponse, response);			
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public void testIfModifiedSince3() throws Exception {
		String request = "GET /test/static/testnonexist.txt HTTP/1.0\015\012" + 
			"Host: localhost\015\012" +
			"If-Modified-Since: <modified>\015\012" + 
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponse = "HTTP/1.1 404 Not Found\015\012" +
			"Content-Type: text/html; charset=utf-8\015\012" +
			"Content-Length: 79\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"<html><body><h1>404 Not Found: /test/static/testnonexist.txt</h1></body></html>";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			
			SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			request = request.replaceFirst("<modified>", format.format(new Date()));
			
			conn.write(request);
			
			String response = conn.readString();
			expectedResponse = conn.setHeaders(response, expectedResponse);
			assertEquals(expectedResponse, response);			
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public static Test suite() {
		return new TestSuite(Http10StaticContentTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
