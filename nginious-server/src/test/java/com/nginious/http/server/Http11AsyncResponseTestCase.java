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

import com.nginious.http.application.Application;
import com.nginious.http.application.ApplicationManager;
import com.nginious.http.server.HttpServer;
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;
import com.nginious.http.service.TestAsyncController;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class Http11AsyncResponseTestCase extends TestCase {
	
	private HttpServer server;
	
	public Http11AsyncResponseTestCase() {
		super();
	}

	public Http11AsyncResponseTestCase(String name) {
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
		application.addController(new TestAsyncController());
		manager.publish(application);
		server.start();
	}

	protected void tearDown() throws Exception {
		if(this.server != null) {
			server.stop();
		}
	}
	
	public void testAsyncResponse1() throws Exception {
		String request = "GET /test/async HTTP/1.1\015\012" +
			"Host: localhost\015\012" +
			"Connection: close\015\012\015\012"; 
			
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Date: <date>\015\012" +
			"Content-Length: 17\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"GET Async World!\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection(4000);
			conn.write(request);
			
			long startTimeMillis = System.currentTimeMillis();
			String response = conn.readString();
			long endTimeMillis = System.currentTimeMillis();
			expectedResponse = conn.setHeaders(response, expectedResponse);
			assertEquals(expectedResponse, response);
			assertTrue(endTimeMillis - startTimeMillis >= 2000);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public void testAsyncResponse2() throws Exception {
		String requestHeaders = "GET /test/async HTTP/1.1\015\012" +
			"Host: localhost\015\012" +
			"Connection: <connection>\015\012\015\012"; 
			
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Date: <date>\015\012" +
			"Content-Length: 17\015\012" +
			"Connection: <connection>\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"GET Async World!\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection(4000);
			
			for(int i = 0; i < 3; i++) {
				String request = requestHeaders.replaceFirst("<connection>", i == 2 ? "close" : "keep-alive");
				conn.write(request);
				
				long startTimeMillis = System.currentTimeMillis();
				String response = null;
				
				if(i == 2) {
					response = conn.readString();
				} else {
					response = conn.readKeepAliveString();
				}
				
				long endTimeMillis = System.currentTimeMillis();
				String expectedResponse2 = conn.setHeaders(response, expectedResponse);
				expectedResponse2 = expectedResponse2.replaceFirst("<connection>", i == 2 ? "close" : "keep-alive");
				assertEquals(expectedResponse2, response);
				assertTrue(endTimeMillis - startTimeMillis >= 2000);
			}
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public static Test suite() {
		return new TestSuite(Http11AsyncResponseTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
