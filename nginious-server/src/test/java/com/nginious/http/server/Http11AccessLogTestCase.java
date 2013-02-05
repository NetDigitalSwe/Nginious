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

import java.io.File;
import java.io.IOException;
import java.net.Socket;

import com.nginious.http.HttpStatus;
import com.nginious.http.application.Application;
import com.nginious.http.application.ApplicationManager;
import com.nginious.http.server.HttpServer;
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;
import com.nginious.http.service.TestEncodingController;
import com.nginious.http.service.TestMethodsController;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class Http11AccessLogTestCase extends TestCase {
	
	private HttpServer server;
	
	public Http11AccessLogTestCase() {
		super();
	}

	public Http11AccessLogTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		File accessLog = new File("logs/access.log");
		accessLog.delete();
		
		HttpServerConfiguration config = new HttpServerConfiguration();
		config.setWebappsDir(null);
		config.setPort(9000);
		HttpServerFactory factory = HttpServerFactory.getInstance();
		this.server = factory.create(config);
		server.setAccessLogConsumer(new FileLogConsumer("build/test-access"));
		server.setMessageLogConsumer(new FileLogConsumer("build/test-server"));
		ApplicationManager manager = server.getApplicationManager();
		Application application = manager.createApplication("test");
		application.addController(new TestEncodingController());
		application.addController(new TestMethodsController());
		manager.publish(application);
		server.start();
	}

	protected void tearDown() throws Exception {
		if(this.server != null) {
			server.stop();
		}
	}
	
	public void testHeadRequest() throws Exception {
		String request = "HEAD /test/methods HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: text/xml; charset=utf-8\015\012" +
			"Connection: close\015\012" + 
			"Content-Length: 4\015\012\015\012" +
			"Test";
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" + 
			"Date: <date>\015\012" +
			"Content-Length: 17\015\012" + 
			"Connection: close\015\012" + 
			"Server: Nginious/1.0.0\015\012\015\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			String response = conn.readString();
			expectedResponse = conn.setHeaders(response, expectedResponse);	
			assertEquals(expectedResponse, response);
			
			Thread.sleep(500);
			CombinedLogEntry entry = conn.getLastAccessLogLine();
			assertNotNull(entry);
			assertEquals("127.0.0.1", entry.getRemoteAddr());
			assertEquals(HttpStatus.OK.getStatusCode(), entry.getStatus());
			assertEquals("HEAD /test/methods HTTP/1.1", entry.getRequest().toString());
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
		
	}
	
	public void testGetRequest() throws Exception {
		String request = "GET /test/methods HTTP/1.1\015\012" +
			"Host: localhost\015\012" +
			"Content-Type: text/xml; charset=utf-8\015\012" +
			"Connection: close\015\012" + 
			"Content-Length: 4\015\012\015\012" +
			"Test";
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Date: <date>\015\012" +
			"Content-Length: 17\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"GET Hello World!\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			String response = conn.readString();
			expectedResponse = conn.setHeaders(response, expectedResponse);
			assertEquals(expectedResponse, response);

			Thread.sleep(500);
			CombinedLogEntry entry = conn.getLastAccessLogLine();
			assertNotNull(entry);
			assertEquals("127.0.0.1", entry.getRemoteAddr());
			assertEquals(HttpStatus.OK.getStatusCode(), entry.getStatus());
			assertEquals("GET /test/methods HTTP/1.1", entry.getRequest().toString());
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}	
	
	public void testPostRequest() throws Exception {
		String request = "POST /test/methods HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: text/xml; charset=utf-8\015\012" +
			"Connection: close\015\012" + 
			"Content-Length: 4\015\012\015\012" +
			"Test";
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Date: <date>\015\012" +
			"Content-Length: 18\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"POST Hello World!\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			String response = conn.readString();
			expectedResponse = conn.setHeaders(response, expectedResponse);
			assertEquals(expectedResponse, response);

			Thread.sleep(500);
			CombinedLogEntry entry = conn.getLastAccessLogLine();
			assertNotNull(entry);
			assertEquals("127.0.0.1", entry.getRemoteAddr());
			assertEquals(HttpStatus.OK.getStatusCode(), entry.getStatus());
			assertEquals("POST /test/methods HTTP/1.1", entry.getRequest().toString());
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
	}
	
	public void testPutRequest() throws Exception {
		String request = "PUT /test/methods HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: text/xml; charset=utf-8\015\012" +
			"Connection: close\015\012" + 
			"Content-Length: 4\015\012\015\012" +
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

			Thread.sleep(500);
			CombinedLogEntry entry = conn.getLastAccessLogLine();
			assertNotNull(entry);
			assertEquals("127.0.0.1", entry.getRemoteAddr());
			assertEquals(HttpStatus.OK.getStatusCode(), entry.getStatus());
			assertEquals("PUT /test/methods HTTP/1.1", entry.getRequest().toString());
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
		
	}

	public void testDeleteRequest() throws Exception {
		String request = "DELETE /test/methods HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: text/xml; charset=utf-8\015\012" +
			"Connection: close\015\012" + 
			"Content-Length: 4\015\012\015\012" +
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

			Thread.sleep(500);
			CombinedLogEntry entry = conn.getLastAccessLogLine();
			assertNotNull(entry);
			assertEquals("127.0.0.1", entry.getRemoteAddr());
			assertEquals(HttpStatus.OK.getStatusCode(), entry.getStatus());
			assertEquals("DELETE /test/methods HTTP/1.1", entry.getRequest().toString());
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
	}
	
	public void testWildcardOptionsRequest() throws Exception {
		Socket client = null;
		String request = "OPTIONS * HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
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

			Thread.sleep(500);
			CombinedLogEntry entry = conn.getLastAccessLogLine();
			assertNotNull(entry);
			assertEquals("127.0.0.1", entry.getRemoteAddr());
			assertEquals(HttpStatus.OK.getStatusCode(), entry.getStatus());
			assertEquals("OPTIONS * HTTP/1.1", entry.getRequest().toString());
		} finally {
			if(client != null) {
				try { client.close(); } catch(IOException e) {}
			}
		}		
	}
	
	public void testTrace() throws Exception {
		Socket client = null;
		String request = "TRACE /test/methods HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 4\015\012" + 
			"Connection: close\015\012\015\012" +
			"test";
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: message/http\015\012" + 
			"Date: <date>\015\012" + 
			"Content-Length: 91\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"TRACE /test/methods HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 4\015\012" + 
			"Connection: close\015\012\015\012" +
			"test";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			String response = conn.readString();
			expectedResponse = conn.setHeaders(response, expectedResponse);
			assertEquals(expectedResponse, response);			
			
			Thread.sleep(500);
			CombinedLogEntry entry = conn.getLastAccessLogLine();
			assertNotNull(entry);
			assertEquals("127.0.0.1", entry.getRemoteAddr());
			assertEquals(HttpStatus.OK.getStatusCode(), entry.getStatus());
			assertEquals("TRACE /test/methods HTTP/1.1", entry.getRequest().toString());
		} finally {
			if(client != null) {
				try { client.close(); } catch(IOException e) {}
			}
		}		
	}
	
	public void testBadEncoding() throws Exception {
		String requestHeader = "POST /test/encoding HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: text/plain; charset=<encoding>\015\012" +
			"Connection: close\015\012" + 
			"Content-Length: <length>\015\012\015\012";
		String expectedResponse = "HTTP/1.1 400 Bad Request\015\012" +
			"Content-Type: text/html; charset=utf-8\015\012" +
			"Date: <date>\015\012" + 
			"Content-Length: 84\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"<html><body><h1>400 Bad Request: unsupported encoding nonexistent</h1></body></html>";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();

			String request = requestHeader.replaceFirst("<encoding>", "nonexistent");
			request = request.replaceFirst("<length>", Integer.toString(4));
			request = request + "Test";
			conn.write(request);
			
			String response = conn.readString();
			expectedResponse = conn.setHeaders(response, expectedResponse);
			assertEquals(expectedResponse, response);

			Thread.sleep(500);
			CombinedLogEntry entry = conn.getLastAccessLogLine();
			assertNotNull(entry);
			assertEquals("127.0.0.1", entry.getRemoteAddr());
			assertEquals(HttpStatus.BAD_REQUEST.getStatusCode(), entry.getStatus());
			assertEquals("POST /test/encoding HTTP/1.1", entry.getRequest().toString());
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public static Test suite() {
		return new TestSuite(Http11AccessLogTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
