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
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;
import com.nginious.http.server.HttpServerImpl;
import com.nginious.http.service.TestMethodsController;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class Http11TimeoutTestCase extends TestCase {
	
	private HttpServerImpl server;
	
	public Http11TimeoutTestCase() {
		super();
	}

	public Http11TimeoutTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		HttpServerConfiguration config = new HttpServerConfiguration();
		config.setWebappsDir(null);
		config.setPort(9000);
		HttpServerFactory factory = HttpServerFactory.getInstance();
		this.server = (HttpServerImpl)factory.create(config);
		server.setAccessLogConsumer(new FileLogConsumer("build/test-access"));
		server.setMessageLogConsumer(new FileLogConsumer("build/test-server"));
		ApplicationManager manager = server.getApplicationManager();
		Application application = manager.createApplication("test");
		application.addController(new TestMethodsController());
		manager.publish(application);
		server.setConnectionTimeoutMillis(2000L);
		server.start();
	}

	protected void tearDown() throws Exception {
		if(this.server != null) {
			server.stop();
		}
	}
	
	public void testInitialTimeout() throws Exception {
		Socket client = null;
		String request = "POST /test/methods HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: text/xml; charset=utf-8\015\012" +
			"Connection: keep-alive\015\012" + 
			"Content-Length: 4\015\012\015\012Test";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection(10000);
			Socket socket = conn.getSocket();
			assertTrue(socket.isConnected());
			Thread.sleep(4000L);
			
			conn.write(request);
			boolean closed = conn.checkClosed();
			assertTrue(closed);
		} finally {
			if(client != null) {
				try { client.close(); } catch(IOException e) {}
			}
		}
	}
	
	public void testSubsequentTimeout() throws Exception {
		Socket client = null;
		String request = "POST /test/methods HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: text/xml; charset=utf-8\015\012" +
			"Connection: keep-alive\015\012" + 
			"Content-Length: 4\015\012\015\012Test";
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Date: <date>\015\012" +
			"Content-Length: 18\015\12" +
			"Connection: keep-alive\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"POST Hello World!\012";
	
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection(10000);
			Socket socket = conn.getSocket();
			assertTrue(socket.isConnected());
			
			conn.write(request);
			String response = conn.readKeepAliveString();
			assertResponseEquals(expectedResponse.replaceFirst("<connection>", "keep-alive"), response);			

			Thread.sleep(4000L);
			conn.write(request);
			boolean closed = conn.checkClosed();
			assertTrue(closed);			
		} finally {
			if(client != null) {
				try { client.close(); } catch(IOException e) {}
			}
		}		
	}
	
	public void testMultipleRequests() throws Exception {
		Socket client = null;
		String request = "POST /test/methods HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: text/xml; charset=utf-8\015\012" +
			"Connection: keep-alive\015\012" + 
			"Content-Length: 4\015\012\015\012Test";
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Date: <date>\015\012" +
			"Content-Length: 18\015\12" +
			"Connection: keep-alive\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"POST Hello World!\012";
	
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection(10000);
			Socket socket = conn.getSocket();
			assertTrue(socket.isConnected());

			for(int i = 0; i < 10; i++) {
				conn.write(request);
				String response = conn.readKeepAliveString();
				assertResponseEquals(expectedResponse.replaceFirst("<connection>", "keep-alive"), response);			
				Thread.sleep(1000L);
			}
			
			Thread.sleep(4000L);
			conn.write(request);
			boolean closed = conn.checkClosed();
			assertTrue(closed);						
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
		return new TestSuite(Http11TimeoutTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
