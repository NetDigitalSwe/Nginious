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

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Random;

import com.nginious.http.application.Application;
import com.nginious.http.application.ApplicationManager;
import com.nginious.http.server.HttpServer;
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;
import com.nginious.http.service.TestBodyController;
import com.nginious.http.service.TestMethodsController;
import com.nginious.http.service.TestNoDataController;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class Http11ContentTestCase extends TestCase {
	
	private HttpServer server;
	
	public Http11ContentTestCase() {
		super();
	}

	public Http11ContentTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		HttpServerConfiguration config = new HttpServerConfiguration();
		config.setWebappsDir(null);
		config.setPort(9000);
		HttpServerFactory factory = HttpServerFactory.getInstance();
		this.server = factory.create(config);
		server.setAccessLogConsumer(new FileLogConsumer("build/test-access"));
		server.setMessageLogConsumer(new FileLogConsumer("build/test-server"));
		ApplicationManager manager = server.getApplicationManager();
		Application application = manager.createApplication("test");
		application.addController(new TestBodyController());
		application.addController(new TestMethodsController());
		application.addController(new TestNoDataController());
		manager.publish(application);
		server.start();
	}

	protected void tearDown() throws Exception {
		if(this.server != null) {
			server.stop();
		}
	}
	
	public void testContent() throws Exception {
		String requestHeader = "POST /test/body HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: application/octet-stream\015\012" +
			"Connection: <connection>\015\012" + 
			"Content-Length: <length>";
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			
			for(int i = 10100; i < 1000000; i += 10000) {
				byte[] data = createData(i);
				String request = requestHeader.replace("<length>", Integer.toString(data.length)) + "\015\012\015\012" + new String(data);
				
				if(i > 1000000 - 10000) {
					request = request.replace("<connection>", "close");
				} else {
					request = request.replace("<connection>", "keep-alive");
				}
				
				conn.write(request);
				
				byte[] response = conn.readKeepAliveBody();
				assertEquals(data, response);
			}
			
			conn.close();
			conn = null;
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
		
	}
	
	public void testTooLargeContent() throws Exception {
		String requestHeader = "POST /test/body HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: application/octet-stream\015\012" +
			"Connection: keep-alive\015\012" + 
			"Content-Length: <length>";
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			
			byte[] data = createData(2097153);
			String request = requestHeader.replace("<length>", Integer.toString(data.length)) + "\015\012\015\012" + new String(data);
			
			try {
				conn.write(request);
				fail("Must not be possible to send too large content");
			} catch(IOException e) {}
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public void testTooShortContentLength() throws Exception {
		String requestHeader = "POST /test/body HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: application/octet-stream\015\012" +
			"Connection: close\015\012" + 
			"Content-Length: <length>";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			
			byte[] data = createData(1000);
			String request = requestHeader.replace("<length>", "900") + "\015\012\015\012" + new String(data);
			conn.write(request);
			
			byte[] response = conn.readBody();
			assertEquals(data, response, 900);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
		
	}

	public void testTooLongContentLength() throws Exception {
		String requestHeader = "POST /test/body HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: application/octet-stream\015\012" +
			"Connection: close\015\012" + 
			"Content-Length: <length>";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			
			byte[] data = createData(1000);
			String request = requestHeader.replace("<length>", "1100") + "\015\012\015\012" + new String(data);
			conn.write(request);
			
			try {
				conn.readBody();
				fail("Should receive a timeout after 2000 millis because of wrong content length");
			} catch(SocketTimeoutException e) {
				// Should end up here
			}
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
		
	}
	
	public void testGetNoContent() throws Exception {
		String request = "GET /test/nodata HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: application/octet-stream\015\012" +
			"Connection: close\015\012" + 
			"Content-Length: 0\015\012\015\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			byte[] response = conn.readBody();
			assertEquals(response.length, 0);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
	}
	
	public void testPostNoContent() throws Exception {
		String request = "POST /test/nodata HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: application/octet-stream\015\012" +
			"Connection: close\015\012" + 
			"Content-Length: 0\015\012\015\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			byte[] response = conn.readBody();
			assertEquals(response.length, 0);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
		
	}
	
	public void testMissingContentLength() throws Exception {
		String request = "POST /test/methods HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Connection: close\015\012\015\012" +
			"Test";
		
		String expectedResponse = "HTTP/1.1 411 Length Required\015\012" +
			"Content-Type: text/html; charset=utf-8\015\012" +
			"Date: <date>\015\012" + 
			"Content-Length: 90\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"<html><body><h1>411 Length Required: missing content length header POST</h1></body></html>";
		
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
	
	private void assertEquals(byte[] local, byte[] response, int len) {
		assertEquals(response.length, len);
		
		for(int i = 0 ; i< len; i++) {
			if(local[i] != response[i]) {
				throw new AssertionError("Failed: " + i + ", " + local[i] + ", " + response[i]);
			}
		}
	}
	private void assertEquals(byte[] first, byte[] second) {
		assertEquals(first.length, second.length);
		
		for(int i = 0 ; i< first.length; i++) {
			if(first[i] != second[i]) {
				throw new AssertionError("Failed: " + i + ", " + first[i] + ", " + second[i]);
			}
		}
	}
	
	private static final String chars = "ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvxyz0123456789";
	
	private static byte[] createData(int length) {
		byte[] data = new byte[length];
		Random rnd = new Random();
		
		for(int i = 0; i < length; i++) {
			int idx = rnd.nextInt(chars.length());
			data[i] = chars.substring(idx, idx + 1).getBytes()[0];
		}
		
		return data;
	}
	
	public static Test suite() {
		return new TestSuite(Http11ContentTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
