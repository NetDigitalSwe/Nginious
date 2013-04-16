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
import java.net.Socket;

import com.nginious.http.application.Application;
import com.nginious.http.application.ApplicationManager;
import com.nginious.http.server.HttpServer;
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;
import com.nginious.http.service.TestEncodingController;
import com.nginious.http.service.TestLocaleController;
import com.nginious.http.service.TestMethodsController;
import com.nginious.http.service.TestStatusMessageController;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class Http11MiscTestCase extends TestCase {
	
	private HttpServer server;
	
    public Http11MiscTestCase() {
		super();
	}

	public Http11MiscTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		HttpServerConfiguration config = new HttpServerConfiguration();
		config.setWebappsDir(null);
		config.setServerLogPath("build/test-server.log");
		config.setAccessLogPath("build/test-access.log");
		config.setPort(9000);
		HttpServerFactory factory = HttpServerFactory.getInstance();
		this.server = factory.create(config);
		ApplicationManager manager = server.getApplicationManager();
		Application application = manager.createApplication("test");
		application.addController(new TestEncodingController());
		application.addController(new TestLocaleController());
		application.addController(new TestMethodsController());
		application.addController(new TestStatusMessageController());
		manager.publish(application);
		server.start();
	}

	protected void tearDown() throws Exception {
		if(this.server != null) {
			server.stop();
		}
	}
	
	public void testCaseInsensitiveHeaders() throws Exception {
		String requestHeaders = "POST /test/encoding HTTP/1.0\015\012" + 
			"host: localhost\015\012" +
			"content-type: text/xml, charset=utf-8\015\012" +
			"connection: close\015\012" + 
			"content-length: ";
		String requestContent = "åäöÅÄÖ";
		String request = requestHeaders + requestContent.getBytes("utf-8").length + "\015\012\015\012" + requestContent;

		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: text/plain; charset=utf-8\015\012" + 
			"Content-Length: 6\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"åäöÅÄÖ";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request.getBytes("utf-8"));
			
			String response = conn.readString("utf-8");
			assertEquals(expectedResponse, response);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
	}
	
	public void testAcceptLanguage1() throws Exception {
		String request = "GET /test/locale HTTP/1.1\015\012" +
			"Accept-Language: sv-se;q=0.8,en-us;q=0.9\015\012" +
			"Host: localhost\015\012" +
			"Connection: close\015\012\015\012"; 
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: text/plain; charset=utf-8\015\012" + 
			"Date: <date>\015\012" + 
			"Content-Length: 5\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"en-us";
		
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
	
	public void testAcceptLanguage2() throws Exception {
		String request = "GET /test/locale HTTP/1.1\015\012" +
			"Accept-Language: sv-se;q=0.9,en-us;q=0.8\015\012" +
			"Host: localhost\015\012" +
			"Connection: close\015\012\015\012"; 
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: text/plain; charset=utf-8\015\012" + 
			"Date: <date>\015\012" + 
			"Content-Length: 5\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"sv-se";
		
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
	
	public void testInvalidHttpVersion() throws Exception {
		String requestHeaders = "POST /test/encoding HTTP/1.2\015\012" + 
			"host: localhost\015\012" +
			"Content-Type: text/xml, charset=utf-8\015\012" +
			"Connection: close\015\012" + 
			"Content-length: ";
		String requestContent = "åäöÅÄÖ";
		String request = requestHeaders + requestContent.getBytes("utf-8").length + "\015\012\015\012" + requestContent;

		String expectedResponse = "HTTP/1.1 505 HTTP Version Not Supported\015\012" +
			"Content-Type: text/html; charset=utf-8\015\012" +
			"Content-Length: 97\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"<html><body><h1>505 HTTP Version Not Supported: version not supported HTTP/1.2</h1></body></html>";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request.getBytes("utf-8"));
			
			String response = conn.readString("utf-8");
			assertEquals(expectedResponse, response);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
	}
	
	public void testStatusMessage() throws Exception {
		String request = "POST /test/status HTTP/1.1\015\012" +
		"Host: localhost\015\012" +
		"Content-Length: 4\015\012" + 
		"Connection: close\015\012\015\012" +
		"Test";
		
		String expectedResponse = "HTTP/1.1 400 Bad Request\015\012" +
			"Content-Type: text/html; charset=utf-8\015\012" +
			"Date: <date>\015\012" + 
			"Content-Length: 63\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"<html><body><h1>400 Bad Request: POST method</h1></body></html>";
		
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
	
	public void testExpectationFailed() throws Exception {
		String request = "POST /test/methods HTTP/1.1\015\012" +
			"Expect: bogus\015\012" +
			"Host: localhost\015\012" +
			"Content-Length: 4\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponse = "HTTP/1.1 417 Expectation Failed\015\012" +
			"Content-Type: text/html; charset=utf-8\015\012" +
			"Date: <date>\015\012" + 
			"Content-Length: 85\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"<html><body><h1>417 Expectation Failed: unknown expect value bogus</h1></body></html>";
		
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
	
	public void testExpect100Continue() throws Exception {
		String requestHeaders = "POST /test/methods HTTP/1.1\015\012" +
			"Expect: 100-continue\015\012" +
			"Host: localhost\015\012" +
			"Content-Length: 4\015\012" + 
			"Connection: close\015\012\015\012";
			
		String requestBody = "Test";
		
		String expectedResponse1 = "HTTP/1.1 100 Continue\015\012" +
			"Date: <date>\015\012" + 
			"Content-Length: 0\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012";
		
		String expectedResponse2 = "HTTP/1.1 200 OK\015\012" +
			"Date: <date>\015\012" + 
			"Content-Length: 18\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"POST Hello World!\n";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(requestHeaders);
				
			String response = conn.readKeepAliveString();
			expectedResponse1 = conn.setHeaders(response, expectedResponse1);
			assertEquals(expectedResponse1, response);
			
			conn.write(requestBody);
			response = conn.readString();
			expectedResponse2 = conn.setHeaders(response, expectedResponse2);
			assertEquals(expectedResponse2, response);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public void testNoLineFeed() throws Exception {
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
		return new TestSuite(Http11MiscTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
