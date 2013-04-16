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
import java.net.SocketException;

import com.nginious.http.application.Application;
import com.nginious.http.application.ApplicationManager;
import com.nginious.http.server.HttpServer;
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;
import com.nginious.http.service.TestMethodsController;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class Http11KeepAliveTestCase extends TestCase {
	
	private HttpServer server;
	
	public Http11KeepAliveTestCase() {
		super();
	}

	public Http11KeepAliveTestCase(String name) {
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
		application.addController(new TestMethodsController());
		manager.publish(application);
		server.start();
	}

	protected void tearDown() throws Exception {
		if(this.server != null) {
			server.stop();
		}
	}
	
	public void testKeepAlive() throws Exception {
		Socket client = null;
		String requestHeader = "POST /test/methods HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: text/xml; charset=utf-8\015\012" +
			"Connection: <connection>\015\012" + 
			"Content-Length: <length>";

		String request404Header = "POST /test/nonexistent HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: text/xml; charset=utf-8\015\012" +
			"Connection: <connection>\015\012" + 
			"Content-Length: <length>";
		
		String request501Header = "POS /test/nonexistent HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: text/xml; charset=utf-8\015\012" +
			"Connection: <connection>\015\012" + 
			"Content-Length: <length>";
		
		String defaultRequestHeader = "POST /test/methods HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: text/xml; charset=utf-8\015\012" +
			"Content-Length: <length>";
		String requestContent = "Test";
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Date: <date>\015\012" +
			"Content-Length: 18\015\12" +
			"Connection: <connection>\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"POST Hello World!\012";
		
		String expected404Response = "HTTP/1.1 404 Not Found\015\012" +
			"Content-Type: text/html; charset=utf-8\015\012" +
			"Date: <date>\015\012" +
			"Content-Length: 67\015\012" +
			"Connection: keep-alive\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"<html><body><h1>404 Not Found: /test/nonexistent</h1></body></html>";
		
		String expected501Response = "HTTP/1.1 501 Not Implemented\015\012" +
			"Content-Type: text/html; charset=utf-8\015\012" +
			"Date: <date>\015\012" +
			"Content-Length: 74\015\012" +
			"Connection: keep-alive\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"<html><body><h1>501 Not Implemented: invalid method POS</h1></body></html>";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			
			// RFC 2616, section 8.1.2. Default behavior must be keep-alive
			String request = defaultRequestHeader.replaceFirst("<length>", "4") + "\015\012\015\012" + requestContent;
			conn.write(request);
			String response = conn.readKeepAliveString();
			assertResponseEquals(expectedResponse.replaceFirst("<connection>", "keep-alive"), response);
			
			// RFC 2616, section 8.1.1. Server must maintain connection even after errors
			request = request404Header.replaceFirst("<connection>", "keep-alive").replaceFirst("<length>", "4") + "\015\012\015\012" + requestContent;
			conn.write(request);
			response = conn.readKeepAliveString();
			assertResponseEquals(expected404Response, response);
			
			request = request501Header.replaceFirst("<connection>", "keep-alive").replaceFirst("<length>", "4") + "\015\012\015\012" + requestContent;
			conn.write(request);
			response = conn.readKeepAliveString();
			assertResponseEquals(expected501Response, response);
			
			request = requestHeader.replaceFirst("<connection>", "keep-alive").replaceFirst("<length>", "4") + "\015\012\015\012" + requestContent;
			conn.write(request);
			response = conn.readKeepAliveString();
			assertResponseEquals(expectedResponse.replaceFirst("<connection>", "keep-alive"), response);
			
			request = requestHeader.replaceFirst("<connection>", "keep-alive").replaceFirst("<length>", "4") + "\015\012\015\012" + requestContent;
			conn.write(request);
			response = conn.readKeepAliveString();
			assertResponseEquals(expectedResponse.replaceFirst("<connection>", "keep-alive"), response);			

			request = requestHeader.replaceFirst("<connection>", "keep-alive").replaceFirst("<length>", "4") + "\015\012\015\012" + requestContent;
			conn.write(request);
			response = conn.readKeepAliveString();
			assertResponseEquals(expectedResponse.replaceFirst("<connection>", "keep-alive"), response);			

			request = requestHeader.replaceFirst("<connection>", "close").replaceFirst("<length>", "4") + "\015\012\015\012" + requestContent;
			conn.write(request);
			response = conn.readKeepAliveString();
			assertResponseEquals(expectedResponse.replaceFirst("<connection>", "close"), response);
			
			Thread.sleep(500L);
			
			try {
				conn.write(request);
				response = conn.readString();
				
				if(response != null && response.equals("")) {
					response = conn.readString();
				}
				
				fail("Must not be possible to write on closed connection");
			} catch(SocketException e) {
				
			}
		} finally {
			if(client != null) {
				try { client.close(); } catch(IOException e) {}
			}
		}
	}
	
	private void assertResponseEquals(String expectedResponse, String response) throws Exception{
		int startIdx = response.indexOf("Date: ");
		int endIdx = response.indexOf("\015\012", startIdx);
		String lastModified = response.substring(startIdx + 6, endIdx);
		expectedResponse = expectedResponse.replaceFirst("<date>", lastModified);			
		assertEquals(expectedResponse, response);
	}
	
    public static Test suite() {
		return new TestSuite(Http11KeepAliveTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
