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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.nginious.http.application.Application;
import com.nginious.http.application.ApplicationManager;
import com.nginious.http.server.HttpServer;
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;
import com.nginious.http.service.TestMethodsService;

public class Http11MalformedTestCase extends TestCase {
	
	private HttpServer server;
	
	public Http11MalformedTestCase() {
		super();
	}

	public Http11MalformedTestCase(String name) {
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
		application.addHttpService(new TestMethodsService());
		manager.publish(application);
		server.start();
	}

	protected void tearDown() throws Exception {
		if(this.server != null) {
			server.stop();
		}
	}
	
	public void testMalformedMethod() throws Exception {
		String request = "P OST /test/methods HTTP/1.0\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: text/xml; charset=utf-8\015\012" +
			"Connection: close\015\012"+"Content-Length: 4\015\012\015\012" +
			"Test";
		
		String expectedResponse = "HTTP/1.1 400 Bad Request\015\012" +
			"Content-Type: text/html; charset=utf-8\015\012" +
			"Content-Length: 63\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"<html><body><h1>400 Bad Request: bad URL OST</h1></body></html>";
		
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
	
	public void testMalformedMethod2() throws Exception {
		String request = "P	 OST /test/methods HTTP/1.0\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: text/xml; charset=utf-8\015\012" +
			"Connection: close\015\012" + 
			"Content-Length: 4\015\012\015\012" +
			"Test";
		
		String expectedResponse = "HTTP/1.1 400 Bad Request\015\012" +
			"Content-Type: text/html; charset=utf-8\015\012" +
			"Content-Length: 66\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"<html><body><h1>400 Bad Request: invalid method</h1></body></html>";
		
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

	public void testMalformedURIDelimiter() throws Exception {
		String request = "POST 	/test/methods HTTP/1.0\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: text/xml; charset=utf-8\015\012" +
			"Connection: close\015\012" + 
			"Content-Length: 4\015\012\015\012" +
			"Test";
		
		String expectedResponse = "HTTP/1.1 400 Bad Request\015\012" +
			"Content-Type: text/html; charset=utf-8\015\012" +
			"Content-Length: 73\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"<html><body><h1>400 Bad Request: invalid url delimiter</h1></body></html>";
		
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
	
	public void testFragmentedRequest() throws Exception {
		String requestHeader = "POST /test/methods HTTP/1.0\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: text/xml; charset=utf-8\015\012" +
			"Connection: close\015\012" + 
			"Content-Length: ";
		String requestContent = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\015\012" +
			"<test xmlns:xsi=\"http://www.w3.org/2005/XMLSchema-instance\"\015\012" +
			"        xsi:noNamespaceSchemaLocation=\"test.xsd\" version=\"1.0\">\015\012" +
			"</test>";
		String request = requestHeader + requestContent.getBytes().length + "\015\012\015\012" + requestContent;
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
				"Content-Length: 18\015\012C" +
				"onnection: close\015\012" +
				"Server: Nginious/1.0.0\015\012\015\012" +
				"POST Hello World!\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			
			conn.write(request.substring(0, 16).getBytes());
			Thread.sleep(15L);
			
			conn.write(request.substring(16, 32).getBytes());
			Thread.sleep(15L);
			
			conn.write(request.substring(32).getBytes());
			Thread.sleep(15L);

			String response = conn.readString();
			assertEquals(expectedResponse, response);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
	}
	
	public void testMultiFragmentedRequest() throws Exception {
		String requestHeader = "POST /test/methods HTTP/1.0\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: text/xml; charset=utf-8\015\012" +
			"Connection: close\015\012" + 
			"Content-Length: ";
		String requestContent = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\015\012" +
			"<test xmlns:xsi=\"http://www.w3.org/2005/XMLSchema-instance\"\015\012" +
			"        xsi:noNamespaceSchemaLocation=\"test.xsd\" version=\"1.0\">\015\012" +
			"</test>";
		String request = requestHeader + requestContent.getBytes().length + "\015\012\015\012" + requestContent;
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Content-Length: 18\015\012C" +
			"onnection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"POST Hello World!\012";
	
		HttpTestConnection conn = null;
		
		try {
			for(int i = 1; i <= 16; i++) {
				for(int j = 1; j <= 16; j++) {
					conn = new HttpTestConnection();
					
					conn.write(request.substring(0, i).getBytes());
					Thread.sleep(15L);
					
					conn.write(request.substring(i, i + j).getBytes());
					Thread.sleep(15L);					

					conn.write(request.substring(i + j).getBytes());
					Thread.sleep(15L);

					String response = conn.readString();
					assertEquals(expectedResponse, response);
					
					conn.close();
					conn = null;
				}
			}
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
	}
	
    public static Test suite() {
		return new TestSuite(Http11MalformedTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
