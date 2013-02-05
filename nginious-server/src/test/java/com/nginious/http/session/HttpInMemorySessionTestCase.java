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

package com.nginious.http.session;

import com.nginious.http.HttpCookie;
import com.nginious.http.HttpSession;
import com.nginious.http.application.Application;
import com.nginious.http.application.ApplicationManager;
import com.nginious.http.server.FileLogConsumer;
import com.nginious.http.server.HttpServer;
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;
import com.nginious.http.server.HttpTestConnection;
import com.nginious.http.server.HttpTestRequest;
import com.nginious.http.server.HttpTestResponse;
import com.nginious.http.service.TestMemorySessionController;
import com.nginious.http.session.HttpInMemorySessionManager;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class HttpInMemorySessionTestCase extends TestCase {
	
	private HttpServer server;
	
	public HttpInMemorySessionTestCase(String name) {
        super(name);
    }

    public void setUp() throws Exception {
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
		application.addController(new TestMemorySessionController());
		manager.publish(application);
		server.start();
   }

    public void tearDown() throws Exception {
    	super.tearDown();
    	
		if(this.server != null) {
			server.stop();
		}
    }
    
    public void testHttpSession1() throws Exception {
    	HttpInMemorySessionManager manager = new HttpInMemorySessionManager();
    	HttpTestRequest request = new HttpTestRequest();
    	request.setPath("/test/testing");
    	HttpTestResponse response = new HttpTestResponse();
    	
    	HttpSession session1 = manager.getSession(request, true);
    	assertNotNull(session1);
    	
    	manager.storeSession(request, response, session1);
    	HttpCookie[] cookies = response.getCookies();
    	assertNotNull(cookies);
    	assertEquals(1, cookies.length);
    	
    	request = new HttpTestRequest();
    	request.addCookie(cookies[0]);
    	HttpSession session2 = manager.getSession(request, false);
    	assertNotNull(session2);
    	assertEquals(session1, session2);
    }
    
    public void testHttpSession2() throws Exception {
		String request = "GET /test/memory?operation=create HTTP/1.0\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012"; 
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: text/plain; charset=utf-8\015\012" + 
			"Content-Length: 4\015\012" +
			"Connection: close\015\012" +
			"Set-Cookie: JSESSIONID=<sessionid>;Path=/test;Expires=<expires>\015\012" + 
			"Server: Nginious/1.0.0\015\012\015\012" +
			"test";
		
		HttpTestConnection conn = null;
		String response = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			response = conn.readString();
			expectedResponse = conn.setHeaders(response, expectedResponse);
			assertEquals(expectedResponse, response);
			conn.close();
			conn = null;
		} finally {
			if(conn != null) {
				conn.close();
			}
		}

		request = "GET /test/memory?operation=change HTTP/1.0\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 0\015\012" + 
			"Cookie: JSESSIONID=<sessionid>\015\012" +
			"Connection: close\015\012\015\012"; 
		
		int startIdx = response.indexOf("JSESSIONID=");
		int endIdx = response.indexOf(";", startIdx);
		String sessionId = response.substring(startIdx + 11, endIdx);
		request = request.replaceFirst("<sessionid>", sessionId);
		
		expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: text/plain; charset=utf-8\015\012" + 
			"Content-Length: 8\015\012" +
			"Connection: close\015\012" +
			"Set-Cookie: JSESSIONID=<sessionid>;Path=/test;Expires=<expires>\015\012" + 
			"Server: Nginious/1.0.0\015\012\015\012" +
			"testtest";
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			response = conn.readString();
			expectedResponse = conn.setHeaders(response, expectedResponse);
			assertEquals(expectedResponse, response);
			conn.close();
			conn = null;
		} finally {
			if(conn != null) {
				conn.close();
			}
		}

		request = "GET /test/memory?operation=delete HTTP/1.0\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 0\015\012" + 
			"Cookie: JSESSIONID=<sessionid>\015\012" +
			"Connection: close\015\012\015\012"; 
		
		startIdx = response.indexOf("JSESSIONID=");
		endIdx = response.indexOf(";", startIdx);
		sessionId = response.substring(startIdx + 11, endIdx);
		request = request.replaceFirst("<sessionid>", sessionId);
		
		expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: text/plain; charset=utf-8\015\012" + 
			"Content-Length: 12\015\012" +
			"Connection: close\015\012" +
			"Set-Cookie: JSESSIONID=<sessionid>;Path=/test;Expires=<expires>\015\012" + 
			"Server: Nginious/1.0.0\015\012\015\012" +
			"testtesttest";
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			response = conn.readString();
			expectedResponse = conn.setHeaders(response, expectedResponse);
			assertEquals(expectedResponse, response);
			conn.close();
			conn = null;
		} finally {
			if(conn != null) {
				conn.close();
			}
		}

		request = "GET /test/memory?operation=change HTTP/1.0\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 0\015\012" + 
			"Cookie: JSESSIONID=<sessionid>\015\012" +
			"Connection: close\015\012\015\012"; 
	
		startIdx = response.indexOf("JSESSIONID=");
		endIdx = response.indexOf(";", startIdx);
		sessionId = response.substring(startIdx + 11, endIdx);
		request = request.replaceFirst("<sessionid>", sessionId);
		
		expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: text/plain; charset=utf-8\015\012" + 
			"Content-Length: 4\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"test";
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			response = conn.readString();
			expectedResponse = conn.setHeaders(response, expectedResponse);
			assertEquals(expectedResponse, response);
			conn.close();
			conn = null;
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
    }
    
    public static Test suite() {
        return new TestSuite(HttpInMemorySessionTestCase.class);
    }    
}
