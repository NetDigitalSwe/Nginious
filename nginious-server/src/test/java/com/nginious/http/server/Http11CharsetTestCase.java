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

import java.nio.charset.Charset;

import com.nginious.http.application.Application;
import com.nginious.http.application.ApplicationManager;
import com.nginious.http.server.HttpServer;
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;
import com.nginious.http.service.TestEncodingController;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class Http11CharsetTestCase extends TestCase {
	
    private HttpServer server;
	
	public Http11CharsetTestCase() {
		super();
	}

	public Http11CharsetTestCase(String name) {
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
		manager.publish(application);
		server.start();
	}

	protected void tearDown() throws Exception {
		if(this.server != null) {
			server.stop();
		}
	}
	
	public void testEncodings() throws Exception {
		String requestHeader = "POST /test/encoding HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: text/plain; charset=<encoding>\015\012" +
			"Connection: close\015\012" + 
			"Content-Length: <length>\015\012\015\012";
		String[] encodings = { "iso-8859-1", "utf-8", "utf-16", "utf-16le", "utf-16be", "us-ascii" };
		HttpTestConnection conn = null;
		
		try {
			for(String encoding : encodings) {
				conn = new HttpTestConnection();
				
				String content = "Den snabba bruna räven hoppar över den slöa grodan.";
				byte[] contentData = content.getBytes(Charset.forName(encoding));
				String request = requestHeader.replaceFirst("<encoding>", encoding);
				request = request.replaceFirst("<length>", Integer.toString(contentData.length));
				byte[] headerData = request.getBytes();
				byte[] data = new byte[headerData.length + contentData.length];
				System.arraycopy(headerData, 0, data, 0, headerData.length);
				System.arraycopy(contentData, 0, data, headerData.length, contentData.length);
				
				conn.write(data);
				String response = conn.readBodyString(encoding);
				
				if(encoding.equals("us-ascii")) {
					assertTrue(!content.equals(response));
				} else {
					assertEquals(content, response);
				}
				
				conn.close();
				conn = null;
			}
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
	}
	
	public void testDefaultCharset() throws Exception {
		String requestHeaders = "POST /test/encoding HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: text/xml\015\012" +
			"Connection: close\015\012" + 
			"Content-Length: ";
		String requestContent = "åäöÅÄÖ";
		String request = requestHeaders + requestContent.getBytes("iso-8859-1").length + "\015\012\015\012" + requestContent;

		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: text/plain; charset=iso-8859-1\015\012" + 
			"Date: <date>\015\012" + 
			"Content-Length: 6\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"åäöÅÄÖ";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request.getBytes("iso-8859-1"));
			
			String response = conn.readString("iso-8859-1");
			expectedResponse = conn.setHeaders(response, expectedResponse);
			assertEquals(expectedResponse, response);
		} finally {
			if(conn != null) {
				conn.close();
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
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
		
    public static Test suite() {
		return new TestSuite(Http11CharsetTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
