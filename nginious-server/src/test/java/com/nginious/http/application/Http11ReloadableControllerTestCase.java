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

import com.nginious.http.common.FileUtils;
import com.nginious.http.server.FileLogConsumer;
import com.nginious.http.server.HttpServer;
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;
import com.nginious.http.server.HttpTestConnection;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class Http11ReloadableControllerTestCase extends TestCase {
	
	private HttpServer server;
	
	private File tmpDir;
	
	private File destFile;
	
    public Http11ReloadableControllerTestCase() {
		super();
	}

	public Http11ReloadableControllerTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		this.tmpDir = new File(System.getProperty("java.io.tmpdir"), "webapps");
		File dir = new File(this.tmpDir, "test/WEB-INF/classes/com/nginious/http/loader");
		dir.mkdirs();
		
		this.destFile = new File(this.tmpDir, "test/WEB-INF/classes/com/nginious/http/loader/ReloadTestController.class");
		FileUtils.copyFile("build/classes/testload1/classes/com/nginious/http/loader/ReloadTestController.class", destFile.getAbsolutePath());
		HttpServerConfiguration config = new HttpServerConfiguration();
		config.setWebappsDir(tmpDir.getAbsolutePath());
		config.setServerLogPath("build/test-server.log");
		config.setPort(9000);
		HttpServerFactory factory = HttpServerFactory.getInstance();
		this.server = factory.create(config);
		server.setAccessLogConsumer(new FileLogConsumer("build/test-access"));
		server.start();
	}

	protected void tearDown() throws Exception {
		if(this.server != null) {
			server.stop();
		}
		
		FileUtils.deleteDir(this.tmpDir);
	}
	
	public void testReloadableController() throws Exception {
		String request = "GET /test/reload HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Connection: close\015\012\015\012";
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: text/plain; charset=utf-8\015\012" +
			"Date: <date>\015\012" + 
			"Content-Length: 23\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"ReloadTestController 1\n";
		
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
		
		Thread.sleep(1000L);
		FileUtils.copyFile("build/classes/testload2/classes/com/nginious/http/loader/ReloadTestController.class", destFile.getAbsolutePath());
		
		expectedResponse = "HTTP/1.1 200 OK\015\012" +
				"Content-Type: text/plain; charset=utf-8\015\012" +
				"Date: <date>\015\012" + 
				"Content-Length: 23\015\012" +
				"Connection: close\015\012" +
				"Server: Nginious/1.0.0\015\012\015\012" +
				"ReloadTestController 2\n";
		
		conn = null;
		
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
		
		destFile.delete();

		expectedResponse = "HTTP/1.1 404 Not Found\015\012" +
				"Content-Type: text/html; charset=utf-8\015\012" +
				"Date: <date>\015\012" + 
				"Content-Length: 62\015\012" +
				"Connection: close\015\012" +
				"Server: Nginious/1.0.0\015\012\015\012" +
				"<html><body><h1>404 Not Found: /test/reload</h1></body></html>";
		
		conn = null;
		
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
		
		request = "GET /test/reload2 HTTP/1.1\015\012" + 
				"Host: localhost\015\012" +
				"Connection: close\015\012\015\012";
		
		expectedResponse = "HTTP/1.1 404 Not Found\015\012" +
				"Content-Type: text/html; charset=utf-8\015\012" +
				"Date: <date>\015\012" + 
				"Content-Length: 63\015\012" +
				"Connection: close\015\012" +
				"Server: Nginious/1.0.0\015\012\015\012" +
				"<html><body><h1>404 Not Found: /test/reload2</h1></body></html>";
		
		conn = null;
		
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
		
		this.destFile = new File(this.tmpDir, "test/WEB-INF/classes/com/nginious/http/loader/ReloadTestController2.class");
		FileUtils.copyFile("build/classes/testload1/classes/com/nginious/http/loader/ReloadTestController2.class", destFile.getAbsolutePath());
		
		expectedResponse = "HTTP/1.1 200 OK\015\012" +
				"Content-Type: text/plain; charset=utf-8\015\012" +
				"Date: <date>\015\012" + 
				"Content-Length: 24\015\012" +
				"Connection: close\015\012" +
				"Server: Nginious/1.0.0\015\012\015\012" +
				"ReloadTestController2 1\n";
			
		conn = null;
		
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
		
		Thread.sleep(1000L);
		FileUtils.copyFile("build/classes/testload2/classes/com/nginious/http/loader/ReloadTestController2.class", destFile.getAbsolutePath());
		
		expectedResponse = "HTTP/1.1 200 OK\015\012" +
				"Content-Type: text/plain; charset=utf-8\015\012" +
				"Date: <date>\015\012" + 
				"Content-Length: 24\015\012" +
				"Connection: close\015\012" +
				"Server: Nginious/1.0.0\015\012\015\012" +
				"ReloadTestController2 2\n";
			
		conn = null;
		
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
		return new TestSuite(Http11ReloadableControllerTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
