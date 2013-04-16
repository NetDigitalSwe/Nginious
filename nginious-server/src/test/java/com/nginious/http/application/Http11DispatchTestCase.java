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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.nginious.http.TestUtils;
import com.nginious.http.common.FileUtils;
import com.nginious.http.server.HttpServer;
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;
import com.nginious.http.server.HttpTestConnection;

public class Http11DispatchTestCase extends TestCase {
	
	private HttpServer server;
	
    public Http11DispatchTestCase() {
		super();
	}

	public Http11DispatchTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		File bakDir = new File("build/backup");
		bakDir.mkdirs();
		File webappsDir = new File("build/test-webapps");
		webappsDir.mkdirs();
		
		File srcFile = TestUtils.findFile("build/libs", "testweb");
		FileUtils.copyFile(srcFile.getAbsolutePath(), "build/test-webapps/test.war");
		HttpServerConfiguration config = new HttpServerConfiguration();
		config.setWebappsDir("build/test-webapps");
		config.setServerLogPath("build/test-server.log");
		config.setAccessLogPath("build/test-access.log");
		config.setPort(9000);
		HttpServerFactory factory = HttpServerFactory.getInstance();
		this.server = factory.create(config);
		server.start();
	}

	protected void tearDown() throws Exception {
		if(this.server != null) {
			server.stop();
		}
		
		FileUtils.deleteDir("build/test-webapps");
	}
	
	public void testHttpGetDispatch() throws Exception {
		String request = "GET /test/dispatch1 HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: text/plain; charset=utf-8\015\012" +
			"Date: <date>\015\012" + 
			"Content-Length: 12\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"GET dispatch";
		
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
	
	public void testHttpPostDispatch() throws Exception {
		String request = "POST /test/dispatch1 HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 4\015\012" + 
			"Connection: close\015\012\015\012" +
			"Test";
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: text/plain; charset=utf-8\015\012" +
			"Date: <date>\015\012" + 
			"Content-Length: 13\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"POST dispatch";
		
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
	
	public void testHttpPutDispatch() throws Exception {
		String request = "PUT /test/dispatch1 HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 4\015\012" + 
			"Connection: close\015\012\015\012" +
			"Test";
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: text/plain; charset=utf-8\015\012" +
			"Date: <date>\015\012" + 
			"Content-Length: 12\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"PUT dispatch";
		
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
	
	public void testHttpDeleteDispatch() throws Exception {
		String request = "DELETE /test/dispatch1 HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 4\015\012" + 
			"Connection: close\015\012\015\012" +
			"Test";
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: text/plain; charset=utf-8\015\012" +
			"Date: <date>\015\012" + 
			"Content-Length: 15\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"DELETE dispatch";
		
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
	
	public void testHttpNotFoundDispatch() throws Exception {
		String request = "GET /test/dispatch3 HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponse = "HTTP/1.1 404 Not Found\015\012" +
			"Content-Type: text/html; charset=utf-8\015\012" +
			"Date: <date>\015\012" + 
			"Content-Length: 65\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"<html><body><h1>404 Not Found: /test/dispatch4</h1></body></html>";
		
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
	
	public static Test suite() {
		return new TestSuite(Http11DispatchTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
