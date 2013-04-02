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

package com.nginious.http.application;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.nginious.http.server.FileLogConsumer;
import com.nginious.http.server.HttpServer;
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;
import com.nginious.http.server.HttpTestConnection;

public class Http11StaticContentTestCase extends TestCase {
	
	private HttpServer server;
	
    public Http11StaticContentTestCase() {
		super();
	}

	public Http11StaticContentTestCase(String name) {
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
		application.setBaseDir(new File("build/resources/testweb"));
		manager.publish(application);
		server.start();
	}

	protected void tearDown() throws Exception {
		if(this.server != null) {
			server.stop();
		}
		
		File index = new File("build/resources/testweb/static/index.html");
		
		if(index.exists()) {
			index.delete();
		}
	}
	
	public void testStaticContent1() throws Exception {
		String request = "GET /test/static/test.txt HTTP/1.1\015\012" + 
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
	
	public void testStaticContent2() throws Exception {
		String request = "GET /test/static/test.doc HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponseHeaders = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: application/msword\015\012" +
			"ETag: 03BF7869AF41E9C64DCEAD7FB5D9235B\015\012" +
			"Date: <date>\015\012" +
			"Last-Modified: <modified>\015\012" + 
			"Accept-Ranges: bytes\015\012" +
			"Content-Length: 2316665\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			byte[] response = conn.readBytes();
			int contentStart = conn.findContentStart(response);
			String responseHeaders = new String(response, 0, contentStart);
			
			expectedResponseHeaders = conn.setHeaders(responseHeaders, expectedResponseHeaders);
			assertEquals(expectedResponseHeaders, responseHeaders);
			int contentLength = response.length - contentStart;
			File file = new File("src/testweb/webapp/static/test.doc");
			assertEquals(contentLength, (int)file.length());
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}	
	
	public void testDefaultFileNotFound() throws Exception {
		String request = "GET /test/static/ HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponse = "HTTP/1.1 404 Not Found\015\012" +
			"Content-Type: text/html; charset=utf-8\015\012" +
			"Date: <date>\015\012" + 
			"Content-Length: 68\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"<html><body><h1>404 Not Found: /static/index.html</h1></body></html>";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			
			SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			request = request.replaceFirst("<modified>", format.format(new Date()));
			
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

	public void testDefaultFile() throws Exception {
		File index = new File("build/resources/testweb/static/index.html");
		FileOutputStream out = new FileOutputStream(index);
		out.write("Hello world!\n".getBytes());
		out.flush();
		out.close();
		
		String request = "GET /test/static/ HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: text/html\015\012" +
			"ETag: 59CA0EFA9F5633CB0371BBC0355478D8\015\012" +
			"Date: <date>\015\012" + 
			"Last-Modified: <modified>\015\012" +
			"Accept-Ranges: bytes\015\012" +
			"Content-Length: 13\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"Hello world!\n";

		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			
			SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			request = request.replaceFirst("<modified>", format.format(new Date()));
			
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
	
	public void testInvalidMethod() throws Exception {
		String request = "POST /test/static/test.txt HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"If-None-Match: <match>\015\012" + 
			"Content-Length: 4\015\012" + 
			"Connection: close\015\012\015\012" +
			"Test";
		
		String expectedResponse = "HTTP/1.1 405 Not Allowed\015\012" +
			"Content-Type: text/html; charset=utf-8\015\012" +
			"Date: <date>\015\012" + 
			"Content-Length: 75\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"<html><body><h1>405 Not Allowed: method not allowed POST</h1></body></html>";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			
			SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			request = request.replace("<match>", "DD18BF3A8E0A2A3E53E2661C7FB53535");
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
	
	public void testAccept1() throws Exception {
		String request = "GET /test/static/test.jpg HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 0\015\012" +
			"Accept: text/html,image/*;q=0.9\015\012" +
			"Connection: close\015\012\015\012";
		
		String expectedResponseHeaders = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: image/jpeg\015\012" +
			"ETag: 509BF211D6D16657F179A19783AC9BE0\015\012" +
			"Date: <date>\015\012" +
			"Last-Modified: <modified>\015\012" + 
			"Accept-Ranges: bytes\015\012" +
			"Content-Length: 79885\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			byte[] response = conn.readBytes();
			int contentStart = conn.findContentStart(response);
			String responseHeaders = new String(response, 0, contentStart);
			expectedResponseHeaders = conn.setHeaders(responseHeaders, expectedResponseHeaders);
			assertEquals(expectedResponseHeaders, responseHeaders);	
		
			int contentLength = response.length - contentStart;
			File file = new File("src/testweb/webapp/static/test.jpg");
			assertEquals(file.length(), contentLength);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public void testAccept2() throws Exception {
		String request = "GET /test/static/test.jpg HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 0\015\012" +
			"Accept: text/html, image/png;q=0.9\015\012" +
			"Connection: close\015\012\015\012";
		
		String expectedResponse = "HTTP/1.1 406 Not Acceptable\015\012" +
			"Content-Type: text/html; charset=utf-8\015\012" +
			"Date: <date>\015\012" +
			"Content-Length: 119\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"<html><body><h1>406 Not Acceptable: content type image/jpeg not acceptable for path /static/test.jpg</h1></body></html>";
		
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
	
	public void testRange1() throws Exception {
		String request = "GET /test/static/test.jpg HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Range: 100-\015\012" +
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponseHeaders = "HTTP/1.1 206 Partial Content\015\012" +
			"Content-Type: image/jpeg\015\012" +
			"ETag: 509BF211D6D16657F179A19783AC9BE0\015\012" +
			"Date: <date>\015\012" +
			"Last-Modified: <modified>\015\012" + 
			"Accept-Ranges: bytes\015\012" +
			"Content-Range: 100-\015\012" +
			"Content-Length: 79785\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			byte[] response = conn.readBytes();
			int contentStart = conn.findContentStart(response);
			String responseHeaders = new String(response, 0, contentStart);
			expectedResponseHeaders = conn.setHeaders(responseHeaders, expectedResponseHeaders);
			assertEquals(expectedResponseHeaders, responseHeaders);	
			
			int contentLength = response.length - contentStart;
			byte[] body = new byte[contentLength];
			System.arraycopy(response, contentStart, body, 0, contentLength);
			File file = new File("src/testweb/webapp/static/test.jpg");
			assertEquals((int)file.length(), contentLength + 100);
			assertEquals(file, body, 100, 79785);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public void testRange2() throws Exception {
		String request = "GET /test/static/test.jpg HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Range: -100\015\012" +
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponseHeaders = "HTTP/1.1 206 Partial Content\015\012" +
			"Content-Type: image/jpeg\015\012" +
			"ETag: 509BF211D6D16657F179A19783AC9BE0\015\012" +
			"Date: <date>\015\012" +
			"Last-Modified: <modified>\015\012" + 
			"Accept-Ranges: bytes\015\012" +
			"Content-Range: -100\015\012" +
			"Content-Length: 100\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			byte[] response = conn.readBytes();
			int contentStart = conn.findContentStart(response);
			String responseHeaders = new String(response, 0, contentStart);
			expectedResponseHeaders = conn.setHeaders(responseHeaders, expectedResponseHeaders);
			assertEquals(expectedResponseHeaders, responseHeaders);	
			
			int contentLength = response.length - contentStart;
			byte[] body = new byte[contentLength];
			System.arraycopy(response, contentStart, body, 0, contentLength);
			File file = new File("src/testweb/webapp/static/test.jpg");
			assertEquals(file, body, 79785, 79884);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public void testRange3() throws Exception {
		String request = "GET /test/static/test.jpg HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Range: 100-200\015\012" +
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponseHeaders = "HTTP/1.1 206 Partial Content\015\012" +
			"Content-Type: image/jpeg\015\012" +
			"ETag: 509BF211D6D16657F179A19783AC9BE0\015\012" +
			"Date: <date>\015\012" +
			"Last-Modified: <modified>\015\012" + 
			"Accept-Ranges: bytes\015\012" +
			"Content-Range: 100-200\015\012" +
			"Content-Length: 101\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			byte[] response = conn.readBytes();
			int contentStart = conn.findContentStart(response);
			String responseHeaders = new String(response, 0, contentStart);
			expectedResponseHeaders = conn.setHeaders(responseHeaders, expectedResponseHeaders);
			assertEquals(expectedResponseHeaders, responseHeaders);	
			
			int contentLength = response.length - contentStart;
			byte[] body = new byte[contentLength];
			System.arraycopy(response, contentStart, body, 0, contentLength);
			File file = new File("src/testweb/webapp/static/test.jpg");
			assertEquals(file, body, 100, 200);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public void testRange4() throws Exception {
		String request = "GET /test/static/test.jpg HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Range: 100-80000\015\012" +
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponseHeaders = "HTTP/1.1 206 Partial Content\015\012" +
			"Content-Type: image/jpeg\015\012" +
			"ETag: 509BF211D6D16657F179A19783AC9BE0\015\012" +
			"Date: <date>\015\012" +
			"Last-Modified: <modified>\015\012" + 
			"Accept-Ranges: bytes\015\012" +
			"Content-Range: 100-79884\015\012" +
			"Content-Length: 79785\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			byte[] response = conn.readBytes();
			int contentStart = conn.findContentStart(response);
			String responseHeaders = new String(response, 0, contentStart);
			expectedResponseHeaders = conn.setHeaders(responseHeaders, expectedResponseHeaders);
			assertEquals(expectedResponseHeaders, responseHeaders);	
			
			int contentLength = response.length - contentStart;
			byte[] body = new byte[contentLength];
			System.arraycopy(response, contentStart, body, 0, contentLength);
			File file = new File("src/testweb/webapp/static/test.jpg");
			assertEquals(file, body, 100, 79884);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public void testRange5() throws Exception {
		String request = "GET /test/static/test.jpg HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Range: badrange\015\012" +
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponseHeaders = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: image/jpeg\015\012" +
			"ETag: 509BF211D6D16657F179A19783AC9BE0\015\012" +
			"Date: <date>\015\012" +
			"Last-Modified: <modified>\015\012" + 
			"Accept-Ranges: bytes\015\012" +
			"Content-Length: 79885\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			byte[] response = conn.readBytes();
			int contentStart = conn.findContentStart(response);
			String responseHeaders = new String(response, 0, contentStart);
			expectedResponseHeaders = conn.setHeaders(responseHeaders, expectedResponseHeaders);
			assertEquals(expectedResponseHeaders, responseHeaders);	
			
			int contentLength = response.length - contentStart;
			byte[] body = new byte[contentLength];
			System.arraycopy(response, contentStart, body, 0, contentLength);
			File file = new File("src/testweb/webapp/static/test.jpg");
			assertEquals(file, body, 0, 79884);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public void testRange6() throws Exception {
		String request = "GET /test/static/test.jpg HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Range: 100-200, 300-400\015\012" +
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponseHeaders = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: image/jpeg\015\012" +
			"ETag: 509BF211D6D16657F179A19783AC9BE0\015\012" +
			"Date: <date>\015\012" +
			"Last-Modified: <modified>\015\012" + 
			"Accept-Ranges: bytes\015\012" +
			"Content-Length: 79885\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			byte[] response = conn.readBytes();
			int contentStart = conn.findContentStart(response);
			String responseHeaders = new String(response, 0, contentStart);
			expectedResponseHeaders = conn.setHeaders(responseHeaders, expectedResponseHeaders);
			assertEquals(expectedResponseHeaders, responseHeaders);	
			
			int contentLength = response.length - contentStart;
			byte[] body = new byte[contentLength];
			System.arraycopy(response, contentStart, body, 0, contentLength);
			File file = new File("src/testweb/webapp/static/test.jpg");
			assertEquals(file, body, 0, 79884);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public void testRange7() throws Exception {
		String request = "GET /test/static/test.jpg HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Range: 80000-90000\015\012" +
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponse = "HTTP/1.1 416 Range Not Satisfiable\015\012" +
			"Content-Type: text/html; charset=utf-8\015\012" +
			"Date: <date>\015\012" +
			"Content-Range: 0-79884\015\012" +
			"Content-Length: 119\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"<html><body><h1>416 Range Not Satisfiable: range 80000-90000 can't be satisfied for /static/test.jpg</h1></body></html>";
		
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
	
	public void testIfRange1() throws Exception {
		String request = "GET /test/static/test.jpg HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"If-Range: 509BF211D6D16657F179A19783AC9BE0\015\012" +
			"Range: 100-200\015\012" +
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponseHeaders = "HTTP/1.1 206 Partial Content\015\012" +
			"Content-Type: image/jpeg\015\012" +
			"ETag: 509BF211D6D16657F179A19783AC9BE0\015\012" +
			"Date: <date>\015\012" +
			"Last-Modified: <modified>\015\012" + 
			"Accept-Ranges: bytes\015\012" +
			"Content-Range: 100-200\015\012" +
			"Content-Length: 101\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			byte[] response = conn.readBytes();
			int contentStart = conn.findContentStart(response);
			String responseHeaders = new String(response, 0, contentStart);
			expectedResponseHeaders = conn.setHeaders(responseHeaders, expectedResponseHeaders);
			assertEquals(expectedResponseHeaders, responseHeaders);	
			
			int contentLength = response.length - contentStart;
			byte[] body = new byte[contentLength];
			System.arraycopy(response, contentStart, body, 0, contentLength);
			File file = new File("src/testweb/webapp/static/test.jpg");
			assertEquals(file, body, 100, 200);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public void testIfRange2() throws Exception {
		String request = "GET /test/static/test.jpg HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"If-Range: 509BF211D6D16657F179A19783AC9BE1\015\012" +
			"Range: 100-200\015\012" +
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponseHeaders = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: image/jpeg\015\012" +
			"ETag: 509BF211D6D16657F179A19783AC9BE0\015\012" +
			"Date: <date>\015\012" +
			"Last-Modified: <modified>\015\012" + 
			"Accept-Ranges: bytes\015\012" +
			"Content-Length: 79885\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			byte[] response = conn.readBytes();
			int contentStart = conn.findContentStart(response);
			String responseHeaders = new String(response, 0, contentStart);
			expectedResponseHeaders = conn.setHeaders(responseHeaders, expectedResponseHeaders);
			assertEquals(expectedResponseHeaders, responseHeaders);	
			
			int contentLength = response.length - contentStart;
			byte[] body = new byte[contentLength];
			System.arraycopy(response, contentStart, body, 0, contentLength);
			File file = new File("src/testweb/webapp/static/test.jpg");
			assertEquals(file, body, 0, 79884);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public void testAcceptEncoding1() throws Exception {
		String request = "GET /test/static/test.jpg HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 0\015\012" +
			"Accept-Encoding: gzip\015\012" +
			"Connection: close\015\012\015\012";
		
		String expectedResponseHeaders = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: image/jpeg\015\012" +
			"ETag: 509BF211D6D16657F179A19783AC9BE0\015\012" +
			"Date: <date>\015\012" +
			"Content-Encoding: gzip\015\012" + 
			"Last-Modified: <modified>\015\012" + 
			"Content-Length: 57467\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			byte[] response = conn.readBytes();
			int contentStart = conn.findContentStart(response);
			String responseHeaders = new String(response, 0, contentStart);
			expectedResponseHeaders = conn.setHeaders(responseHeaders, expectedResponseHeaders);
			assertEquals(expectedResponseHeaders, responseHeaders);	
			
			ByteArrayInputStream in = new ByteArrayInputStream(response, contentStart, response.length - contentStart);
			GZIPInputStream zIn = new GZIPInputStream(in);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			int len = 0;
			
			while((len = zIn.read(b)) > 0) {
				out.write(b, 0, len);
			}
			
			byte[] fileContent = out.toByteArray();
			File file = new File("src/testweb/webapp/static/test.jpg");
			assertEquals((int)file.length(), fileContent.length);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public void testAcceptEncoding2() throws Exception {
		String request = "GET /test/static/test.doc HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 0\015\012" +
			"Accept-Encoding: gzip\015\012" +
			"Connection: close\015\012\015\012";
		
		String expectedResponseHeaders = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: application/msword\015\012" +
			"ETag: 03BF7869AF41E9C64DCEAD7FB5D9235B\015\012" +
			"Date: <date>\015\012" +
			"Content-Encoding: gzip\015\012" + 
			"Last-Modified: <modified>\015\012" + 
			"Content-Length: 1665599\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			byte[] response = conn.readBytes();
			int contentStart = conn.findContentStart(response);
			String responseHeaders = new String(response, 0, contentStart);
			expectedResponseHeaders = conn.setHeaders(responseHeaders, expectedResponseHeaders);
			assertEquals(expectedResponseHeaders, responseHeaders);	
			
			ByteArrayInputStream in = new ByteArrayInputStream(response, contentStart, response.length - contentStart);
			GZIPInputStream zIn = new GZIPInputStream(in);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			int len = 0;
			
			while((len = zIn.read(b)) > 0) {
				out.write(b, 0, len);
			}
			
			byte[] fileContent = out.toByteArray();
			File file = new File("src/testweb/webapp/static/test.doc");
			assertEquals((int)file.length(), fileContent.length);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public void testIfModifiedSince1() throws Exception {
		String request = "GET /test/static/test.txt HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"If-Modified-Since: <modified>\015\012" + 
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponse = "HTTP/1.1 304 Not Modified\015\012" +
			"Date: <date>\015\012" +
			"Content-Length: 0\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			
			SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			request = request.replaceFirst("<modified>", format.format(new Date()));
			
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
	
	public void testIfModifiedSince2() throws Exception {
		String request = "GET /test/static/test.txt HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"If-Modified-Since: <modified>\015\012" + 
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
			
			Calendar modifiedCal = Calendar.getInstance();
			modifiedCal.add(Calendar.YEAR, -2);
			SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			request = request.replaceFirst("<modified>", format.format(modifiedCal.getTime()));
			
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
	
	public void testIfModifiedSince3() throws Exception {
		String request = "GET /test/static/testnonexist.txt HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"If-Modified-Since: <modified>\015\012" + 
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponse = "HTTP/1.1 404 Not Found\015\012" +
			"Content-Type: text/html; charset=utf-8\015\012" +
			"Date: <date>\015\012" + 
			"Content-Length: 79\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"<html><body><h1>404 Not Found: /test/static/testnonexist.txt</h1></body></html>";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			
			SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			request = request.replaceFirst("<modified>", format.format(new Date()));
			
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
	
	public void testIfUnmodifiedSince1() throws Exception {
		String request = "GET /test/static/test.txt HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"If-Unmodified-Since: <modified>\015\012" + 
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponse = "HTTP/1.1 412 Precondition Failed\015\012" +
			"Content-Type: text/html; charset=utf-8\015\012" + 
			"Date: <date>\015\012" + 
			"Content-Length: 94\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"<html><body><h1>412 Precondition Failed: resource modified /static/test.txt</h1></body></html>";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			
			Calendar modifiedCal = Calendar.getInstance();
			modifiedCal.add(Calendar.YEAR, -2);
			SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			request = request.replaceFirst("<modified>", format.format(modifiedCal.getTime()));
			
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
	
	public void testIfUnmodifiedSince2() throws Exception {
		String request = "GET /test/static/test.txt HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"If-Unmodified-Since: <modified>\015\012" + 
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
			
			SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			request = request.replaceFirst("<modified>", format.format(new Date(System.currentTimeMillis())));
			
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
	
	public void testIfMatch1() throws Exception {
		String request = "GET /test/static/test.txt HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"If-Match: DD18BF3A8E0A2A3E53E2661C7FB53534\015\012" + 
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
	
	public void testIfMatch2() throws Exception {
		String requestHeaders = "GET /test/static/test.txt HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"If-Match: *, DD18BF3A8E0A2A3E53E2661C7FB53535\015\012" + 
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		String request = requestHeaders;
		
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
	
	public void testIfMatch3() throws Exception {
		String requestHeaders = "GET /test/static/test.txt HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"If-Match: DD18BF3A8E0A2A3E53E2661C7FB53535\015\012" + 
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		String request = requestHeaders;
		
		String expectedResponse = "HTTP/1.1 412 Precondition Failed\015\012" +
			"Content-Type: text/html; charset=utf-8\015\012" + 
			"Date: <date>\015\012" + 
			"Content-Length: 94\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"<html><body><h1>412 Precondition Failed: resource modified /static/test.txt</h1></body></html>";
		
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
	
	public void testIfNoneMatch1() throws Exception {
		String request = "GET /test/static/test.txt HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"If-None-Match: <match>\015\012" + 
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponse = "HTTP/1.1 304 Not Modified\015\012" +
			"Date: <date>\015\012" + 
			"Content-Length: 0\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			
			SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			request = request.replace("<match>", "DD18BF3A8E0A2A3E53E2661C7FB53534");
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
	
	public void testIfNoneMatch2() throws Exception {
		String request = "GET /test/static/test.txt HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"If-None-Match: <match>\015\012" + 
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
			
			SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			request = request.replace("<match>", "DD18BF3A8E0A2A3E53E2661C7FB53535");
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
	
	public void testStaticContentOptions() throws Exception {
		String request = "OPTIONS /test/static/test.txt HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Date: <date>\015\012" + 
			"Allow: GET, HEAD, OPTIONS\015\012" + 
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
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	private void assertEquals(File file, byte[] data, int startInclusive, int endInclusive) throws Exception {
		RandomAccessFile in = new RandomAccessFile(file, "r");
		in.seek(startInclusive);
		
		for(int i= startInclusive; i <= endInclusive; i++) {
			assertEquals(data[i - startInclusive], (byte)in.read());
		}
	}
	
	public static Test suite() {
		return new TestSuite(Http11StaticContentTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
