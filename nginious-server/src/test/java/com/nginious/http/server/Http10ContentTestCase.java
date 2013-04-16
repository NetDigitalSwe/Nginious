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
import java.util.Random;

import com.nginious.http.application.Application;
import com.nginious.http.application.ApplicationManager;
import com.nginious.http.server.HttpServer;
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;
import com.nginious.http.service.TestBodyController;
import com.nginious.http.service.TestNoDataController;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class Http10ContentTestCase extends TestCase {
	
	private HttpServer server;
	
	public Http10ContentTestCase() {
		super();
	}

	public Http10ContentTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		HttpServerConfiguration config = new HttpServerConfiguration();
		config.setWebappsDir(null);
		config.setServerLogPath("build/test-server.log");
		config.setPort(9000);
		HttpServerFactory factory = HttpServerFactory.getInstance();
		this.server = factory.create(config);
		server.setAccessLogConsumer(new FileLogConsumer("build/test-access"));
		ApplicationManager manager = server.getApplicationManager();
		Application application = manager.createApplication("test");
		application.addController(new TestBodyController());
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
		String requestHeader = "POST /test/body HTTP/1.0\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: application/octet-stream\015\012" +
			"Connection: close\015\012" + 
			"Content-Length: <length>";
		HttpTestConnection conn = null;
		
		try {
			for(int i = 10100; i < 1000000; i += 10000) {
				conn = new HttpTestConnection();
				byte[] data = createData(i);
				String request = requestHeader.replace("<length>", Integer.toString(data.length)) + "\015\012\015\012" + new String(data);
				
				conn.write(request);
				
				byte[] response = conn.readBody();
				assertEquals(data, response);
				
				conn.close();
				conn = null;
			}
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
		
	}
	
	public void testTooLargeContent() throws Exception {
		String requestHeader = "POST /test/body HTTP/1.0\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: application/octet-stream\015\012" +
			"Connection: close\015\012" + 
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
	
	public void testGetNoContent() throws Exception {
		String request = "GET /test/nodata HTTP/1.0\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: application/octet-stream\015\012" +
			"Connection: close\015\012" + 
			"Content-Length: 0\015\012\015\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			byte[] response = conn.readBody();
			System.out.println("Resp: " + new String(response));
			assertEquals(response.length, 0);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
	}
	
	public void testPostNoContent() throws Exception {
		String request = "POST /test/nodata HTTP/1.0\015\012" + 
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
	
	public void testPostNoContentLength() throws Exception {
		String request = "POST /test/methods HTTP/1.0\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: text/xml; charset=utf-8\015\012" +
			"Connection: close\015\012\015\012" +
			"Test";
		
		String expectedResponse = "HTTP/1.1 400 Bad Request\015\012" +
			"Content-Type: text/html; charset=utf-8\015\012" +
			"Content-Length: 86\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"<html><body><h1>400 Bad Request: missing content length header POST</h1></body></html>";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			String response = conn.readString();
			assertEquals(expectedResponse, response);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
		
	}	

	private void assertEquals(byte[] first, byte[] second) {
		assertEquals(first.length, second.length);
		
		for(int i = 0 ; i< first.length; i++) {
			if(first[i] != second[i]) {
				throw new AssertionError("Failed");
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
		return new TestSuite(Http10ContentTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
