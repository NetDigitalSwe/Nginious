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

import com.nginious.http.common.FileUtils;
import com.nginious.http.server.HttpServer;
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;
import com.nginious.http.server.HttpTestConnection;

public class Http11PrimitiveControllerTypesTestCase extends TestCase {
	
	private HttpServer server;
	
	private File tmpDir;
	
    public Http11PrimitiveControllerTypesTestCase() {
		super();
	}

	public Http11PrimitiveControllerTypesTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		this.tmpDir = new File(System.getProperty("java.io.tmpdir"), "webapps");
		tmpDir.mkdir();
		File destFile = new File(this.tmpDir, "test.war");
		FileUtils.copyFile("build/libs/nginious-server-0.9.2-testweb.war", destFile.getAbsolutePath());
		HttpServerConfiguration config = new HttpServerConfiguration();
		config.setWebappsDir(tmpDir.getAbsolutePath());
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

		FileUtils.deleteDir(this.tmpDir);
	}
	
	public void testStringType() throws Exception {
		String request = "GET /test/primitive?test=Hello HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Date: <date>\015\012" + 
			"Content-Length: 6\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"Hello\n";
		
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
	
	public void testIntType() throws Exception {
		String request = "POST /test/primitive HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 7\015\012" + 
			"Content-Type: application/x-www-form-urlencoded\015\012" +
			"Connection: close\015\012\015\012" +
			"test=15";
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
				"Date: <date>\015\012" + 
				"Content-Length: 3\015\012" +
				"Connection: close\015\012" +
				"Server: Nginious/1.0.0\015\012\015\012" +
				"15\n";
			
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
	
	public void testLongObject() throws Exception {
		String request = "PUT /test/primitive HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 11\015\012" + 
			"Content-Type: application/x-www-form-urlencoded\015\012" +
			"Connection: close\015\012\015\012" +
			"test=201124";
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
				"Date: <date>\015\012" + 
				"Content-Length: 7\015\012" +
				"Connection: close\015\012" +
				"Server: Nginious/1.0.0\015\012\015\012" +
				"201124\n";
			
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
	
	public void testBooleanType() throws Exception {
		String request = "DELETE /test/primitive HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 9\015\012" + 
			"Content-Type: application/x-www-form-urlencoded\015\012" +
			"Connection: close\015\012\015\012" +
			"test=true";
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
				"Date: <date>\015\012" + 
				"Content-Length: 5\015\012" +
				"Connection: close\015\012" +
				"Server: Nginious/1.0.0\015\012\015\012" +
				"true\n";
		
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
		return new TestSuite(Http11PrimitiveControllerTypesTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
