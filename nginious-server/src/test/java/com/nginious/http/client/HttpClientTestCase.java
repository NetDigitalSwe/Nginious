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

package com.nginious.http.client;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

import com.nginious.http.HttpMethod;
import com.nginious.http.TestUtils;
import com.nginious.http.application.Application;
import com.nginious.http.application.ApplicationManager;
import com.nginious.http.client.HttpClient;
import com.nginious.http.client.HttpClientRequest;
import com.nginious.http.client.HttpClientResponse;
import com.nginious.http.common.FileUtils;
import com.nginious.http.server.FileLogConsumer;
import com.nginious.http.server.HttpServer;
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;
import com.nginious.http.server.HttpTestConnection;
import com.nginious.http.service.TestBodyController;
import com.nginious.http.service.TestMethodsController;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class HttpClientTestCase extends TestCase {
	
	private HttpServer server;
	
	private File tmpDir;
	
	public HttpClientTestCase() {
		super();
	}

	public HttpClientTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		this.tmpDir = new File(System.getProperty("java.io.tmpdir"), "webapps");
		tmpDir.mkdir();
		File srcFile = TestUtils.findFile("build/libs", "testweb");
		File destFile = new File(this.tmpDir, "test.war");
		FileUtils.copyFile(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
		
		HttpServerConfiguration config = new HttpServerConfiguration();
		config.setWebappsDir(tmpDir.getAbsolutePath());
		config.setServerLogPath("build/test-server.log");
		config.setPort(9000);
		
		HttpServerFactory factory = HttpServerFactory.getInstance();
		this.server = factory.create(config);
		server.setAccessLogConsumer(new FileLogConsumer("build/test-access"));
		ApplicationManager manager = server.getApplicationManager();
		Application application = manager.createApplication("test");
		application.addController(new TestBodyController());
		application.addController(new TestMethodsController());
		manager.publish(application);
		server.start();
	}

	protected void tearDown() throws Exception {
		if(this.server != null) {
			server.stop();
		}
		
		FileUtils.deleteDir(this.tmpDir);
	}
	
	public void testHeadRequest() throws Exception {
		HttpClient client = null;
		
		try {
			HttpClientRequest request = new HttpClientRequest();
			request.setMethod(HttpMethod.HEAD);
			request.setPath("/test/methods");
			request.addHeader("Host", "localhost");
			request.addHeader("Content-Type", "text/xml; charset=utf-8");
			request.addHeader("Connection", "close");
			
			client = new HttpClient("localhost", 9000);
			HttpClientResponse response = client.request(request, new byte[0]);
			
			assertEquals(200, response.getStatus().getStatusCode());
			assertNotNull(response.getHeader("Date"));
			assertEquals(17, response.getContentLength());
			assertEquals("close", response.getHeader("Connection"));
			assertEquals("Nginious/1.0.0", response.getHeader("Server"));
		} finally {
			if(client != null) {
				client.close();
			}
		}
	}
	
	public void testGetRequest() throws Exception {
		HttpClient client = null;
		
		try {
			HttpClientRequest request = new HttpClientRequest();
			request.setMethod(HttpMethod.GET);
			request.setPath("/test/methods");
			request.setHeader("Host", "localhost");
			request.setHeader("Content-Type", "text/xml; charset=utf-8");
			request.setHeader("Connection", "close");
			
			client = new HttpClient("localhost", 9000);
			HttpClientResponse response = client.request(request, new byte[0]);
			
			assertEquals(200, response.getStatus().getStatusCode());
			assertNotNull(response.getHeader("Date"));
			assertEquals(17, response.getContentLength());
			assertEquals("close", response.getHeader("Connection"));
			assertEquals("Nginious/1.0.0", response.getHeader("Server"));
			assertEquals("GET Hello World!\012", new String(response.getContent()));
		} finally {
			if(client != null) {
				client.close();
			}
		}
	}	
	
	public void testContent() throws Exception {
		HttpClient client = null;
		
		try {
			HttpClientRequest request = new HttpClientRequest();
			request.setMethod(HttpMethod.POST);
			request.setPath("/test/body");
			request.setHeader("Host", "localhost");
			request.setHeader("Content-Type", "text/xml; charset=utf-8");
			request.setHeader("Connection", "close");
			request.setHeader("Content-Length", "4");
			
			client = new HttpClient("localhost", 9000);
			HttpClientResponse response = client.request(request, "Test".getBytes());
			
			assertEquals(200, response.getStatus().getStatusCode());
			assertNotNull(response.getHeader("Date"));
			assertEquals(4, response.getContentLength());
			assertEquals("close", response.getHeader("Connection"));
			assertEquals("Nginious/1.0.0", response.getHeader("Server"));
			assertEquals("Test", new String(response.getContent()));
		} finally {
			if(client != null) {
				client.close();
			}
		}
	}
	
	public void testKeepAlive() throws Exception {
		HttpClient client = null;
		
		try {
			HttpClientRequest request = new HttpClientRequest();
			request.setMethod(HttpMethod.POST);
			request.setPath("/test/body");
			request.setHeader("Host", "localhost");
			request.setHeader("Content-Type", "text/xml; charset=utf-8");
			request.setHeader("Connection", "keep-alive");
			request.setHeader("Content-Length", "4");
			
			client = new HttpClient("localhost", 9000);
			HttpClientResponse response = client.request(request, "Test".getBytes());
			
			assertEquals(200, response.getStatus().getStatusCode());
			assertNotNull(response.getHeader("Date"));
			assertEquals(4, response.getContentLength());
			assertEquals("keep-alive", response.getHeader("Connection"));
			assertEquals("Nginious/1.0.0", response.getHeader("Server"));
			assertEquals("Test", new String(response.getContent()));

			assertTrue(client.isKeepAlive());
			
			client = new HttpClient("localhost", 9000);
			response = client.request(request, "Test".getBytes());
			
			assertEquals(200, response.getStatus().getStatusCode());
			assertNotNull(response.getHeader("Date"));
			assertEquals(4, response.getContentLength());
			assertEquals("keep-alive", response.getHeader("Connection"));
			assertEquals("Nginious/1.0.0", response.getHeader("Server"));
			assertEquals("Test", new String(response.getContent()));
		} finally {
			if(client != null) {
				client.close();
			}
		}
	}
	
	public void testPutRequest() throws Exception {
		String request = "PUT /test/methods HTTP/1.1\n" + 
			"Host: localhost\n" +
			"Content-Type: text/xml; charset=utf-8\n" +
			"Connection: close\n" + 
			"Content-Length: 4\n\n" +
			"Test";
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Date: <date>\015\012" +
			"Content-Length: 17\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"PUT Hello World!\012";
	
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

	public void testDeleteRequest() throws Exception {
		String request = "DELETE /test/methods HTTP/1.1\n" + 
			"Host: localhost\n" +
			"Content-Type: text/xml; charset=utf-8\n" +
			"Connection: close\n" + 
			"Content-Length: 4\n\n" +
			"Test";
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Date: <date>\015\012" +
			"Content-Length: 20\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"DELETE Hello World!\012";

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
	
	public void testWildcardOptionsRequest() throws Exception {
		Socket client = null;
		String request = "OPTIONS * HTTP/1.1\n" + 
			"Host: localhost\n" +
			"Content-Length: 0\n" + 
			"Connection: close\n\n";
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Date: <date>\015\012" + 
			"Allow: GET, HEAD, POST, PUT, DELETE, OPTIONS, TRACE\015\012" + 
			"Content-Length: 0\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			String response = conn.readString();
			expectedResponse = conn.setHeaders(response, expectedResponse);
			assertEquals(expectedResponse, response);			
		} finally {
			if(client != null) {
				try { client.close(); } catch(IOException e) {}
			}
		}		
	}
	
	public void testTrace() throws Exception {
		Socket client = null;
		String request = "TRACE /test/methods HTTP/1.1\n" + 
			"Host: localhost\n" +
			"Content-Length: 4\n" + 
			"Connection: close\n\n" +
			"test";
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: message/http\015\012" + 
			"Date: <date>\015\012" + 
			"Content-Length: 86\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"TRACE /test/methods HTTP/1.1\n" + 
			"Host: localhost\n" +
			"Content-Length: 4\n" + 
			"Connection: close\n\n" +
			"test";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			String response = conn.readString();
			expectedResponse = conn.setHeaders(response, expectedResponse);
			assertEquals(expectedResponse, response);			
		} finally {
			if(client != null) {
				try { client.close(); } catch(IOException e) {}
			}
		}		
	}
	
	public static Test suite() {
		return new TestSuite(HttpClientTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
