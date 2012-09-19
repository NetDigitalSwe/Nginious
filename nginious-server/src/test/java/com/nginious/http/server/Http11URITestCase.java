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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLEncoder;
import java.util.Enumeration;

import com.nginious.http.application.Application;
import com.nginious.http.application.ApplicationManager;
import com.nginious.http.server.HttpServer;
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;
import com.nginious.http.service.TestParametersService;
import com.nginious.http.service.TestURIService;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class Http11URITestCase extends TestCase {
	
    private HttpServer server;
	
	public Http11URITestCase() {
		super();
	}

	public Http11URITestCase(String name) {
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
		application.addHttpService(new TestParametersService());
		application.addHttpService(new TestURIService());
		application.setBaseDir(new File("src/testweb/webapp"));
		manager.publish(application);
		server.start();
	}

	protected void tearDown() throws Exception {
		if(this.server != null) {
			server.stop();
		}
	}
	
	public void testGetParameters() throws Exception {
		String request =  "GET /test/parameters?param1=1&param2=Hejsan%20svejsan&param3=One&param3=Two&param3=Three" +
			"&param4=%e5 HTTP/1.0\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: text/xml; charset=iso-8859-1\015\012" +
			"Connection: close\015\012\015\012"; 
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"param1=1&param2=Hejsan svejsan&param3=One&param3=Two&param3=Three&param4=å\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			byte[] data = conn.readBytes();
			String response = new String(data, "utf-8");
			assertEquals(expectedResponse, response);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
		
	}
	
	public void testPostParameters() throws Exception {
		String requestHeader = "POST /test/parameters HTTP/1.0\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: application/x-www-form-urlencoded; charset=iso-8859-1\015\012" +
			"Connection: close\015\012" + 
			"Content-Length: ";
		String requestContent = "param1=1&param2=Hejsan%20svejsan&param3=One&param3=Two&param3=Three&param4=%e5";
		String request = requestHeader + requestContent.getBytes().length + "\015\012\015\012" + requestContent;
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"param1=1&param2=Hejsan svejsan&param3=One&param3=Two&param3=Three&param4=å\012";
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			byte[] data = conn.readBytes();
			String response = new String(data, "utf-8");
			assertEquals(expectedResponse, response);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
	}
	
	public void testGetParametersEncoding() throws Exception {
		String requestHeader = "GET /test/uri?param=<param> HTTP/1.0\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: text/plain; charset=<encoding>\015\012" +
			"Connection: close\015\012" + 
			"Content-Length: <length>\015\012\015\012";
		
		HttpTestConnection conn = null;
		String[] encodings = { "iso-8859-1", "utf-8", "utf-16", "utf-16le", "utf-16be" };
		
		try {
			for(String encoding : encodings) {
				conn = new HttpTestConnection();
				
				String content = "Den snabba räven hoppar över den slöa grodan.";
				String uriEncoded = URLEncoder.encode(content, encoding);
				String request = requestHeader.replaceFirst("<encoding>", encoding);
				request = request.replaceFirst("<length>", "0");
				request = request.replaceFirst("<param>", uriEncoded);
				byte[] headerData = request.getBytes();
				byte[] data = new byte[headerData.length + uriEncoded.length()];
				System.arraycopy(headerData, 0, data, 0, headerData.length);
				byte[] encoded = uriEncoded.getBytes();
				System.arraycopy(encoded, 0, data, headerData.length, encoded.length);
				
				conn.write(data);
				
				byte[] response = conn.readBody();
				assertEquals(content.getBytes(encoding), response);
				
				conn.close();
				conn = null;
			}
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
	}
	
	public void testPostParametersEncoding() throws Exception {
		String requestHeader = "POST /test/uri HTTP/1.0\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: application/x-www-form-urlencoded; charset=<encoding>\015\012" +
			"Connection: close\015\012" + 
			"Content-Length: <length>\015\012\015\012" + 
			"param=<param>";
		
		HttpTestConnection conn = null;
		String[] encodings = { "iso-8859-1", "utf-8", "utf-16", "utf-16le", "utf-16be" };
		
		try {
			for(String encoding : encodings) {
				conn = new HttpTestConnection();
				
				String content = "Den snabba räven hoppar över den slöa grodan.";
				String uriEncoded = URLEncoder.encode(content, encoding);
				String request = requestHeader.replaceFirst("<encoding>", encoding);
				request = request.replaceFirst("<length>", Integer.toString("param=".length() + uriEncoded.length()));
				request = request.replaceFirst("<param>", uriEncoded);
				byte[] data = request.getBytes();
				
				conn.write(data);
				
				byte[] response = conn.readBody();
				assertEquals(content.getBytes(encoding), response);
				
				conn.close();
				conn = null;
			}
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
	}
	
	public void testAbsoluteURI() throws Exception {
		String hostname = getHostname();
		
		if(hostname == null) {
			fail("Must be connected to run test");
		}
		
		String request = "GET http://" + hostname + "/test/static/test.txt HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: text/plain\015\012" +
			"ETag: DD18BF3A8E0A2A3E53E2661C7FB53534\015\012" +
			"Date: <date>\015\012" + 
			"Last-Modified: <modified>\015\012" + 
			"Accept-Ranges: bytes\015\012" + 
			"Content-Length: 8\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"test.txt";
		
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
	
	public void testAbsoluteBadHostnameURI() throws Exception {
		String request = "GET http://www.badhost.com/test/static/test.txt HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponse = "HTTP/1.1 400 Bad Request\015\012" +
			"Content-Type: text/html; charset=utf-8\015\012" +
			"Content-Length: 80\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"<html><body><h1>400 Bad Request: invalid host www.badhost.com</h1></body></html>";
		
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
	
	private void assertEquals(byte[] first, byte[] second) {
		assertEquals(first.length, second.length);
		
		for(int i = 0 ; i< first.length; i++) {
			if(first[i] != second[i]) {
				throw new AssertionError("Failed");
			}
		}
	}
	
	private String getHostname() throws IOException {
		Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();

		while(ifaces.hasMoreElements()) {
			NetworkInterface iface = ifaces.nextElement();
			
			if(!iface.isLoopback()) {
				Enumeration<InetAddress> inets = iface.getInetAddresses();
				
				while(inets.hasMoreElements()) {
					InetAddress inet = inets.nextElement();
					
					if(!inet.getCanonicalHostName().equalsIgnoreCase(inet.getHostAddress())) {
						return inet.getCanonicalHostName();
					}
				}
			}
		}
		
		return null;
	}
	
	public static Test suite() {
		return new TestSuite(Http11URITestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
