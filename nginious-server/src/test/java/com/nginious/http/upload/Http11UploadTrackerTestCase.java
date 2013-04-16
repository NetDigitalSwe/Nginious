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

package com.nginious.http.upload;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.nginious.http.application.Application;
import com.nginious.http.application.ApplicationManager;
import com.nginious.http.server.HttpServer;
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;
import com.nginious.http.server.HttpTestConnection;
import com.nginious.http.service.TestUploadController;
import com.nginious.http.service.TestUploadProgressController;
import com.nginious.http.service.TestUploadTrackerController;

public class Http11UploadTrackerTestCase extends TestCase {
	
	private HttpServer server;
	
	public Http11UploadTrackerTestCase() {
		super();
	}

	public Http11UploadTrackerTestCase(String name) {
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
		application.addController(new TestUploadController());
		application.addController(new TestUploadTrackerController());
		application.addController(new TestUploadProgressController());
		manager.publish(application);
		server.start();
	}

	protected void tearDown() throws Exception {
		if(this.server != null) {
			server.stop();
		}
	}
	
	public void testUploadTracker1() throws Exception {
		String request = "GET /test/tracker HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: text/plain\015\012" +
			"Connection: close\015\012" + 
			"Content-Length: 0\015\012\015\012";
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			String response = conn.readString();
			
			assertNotNull(response);
			int startIdx = response.indexOf("http_upload_id=");
			assertTrue(startIdx > -1);
			int endIdx = response.indexOf(";", startIdx);
			String trackerCookie = response.substring(startIdx + 15, endIdx);
			assertTrue(response.indexOf("Set-Cookie: http_upload_id=" + trackerCookie + ";Path=/test;Expires=") > -1);
			conn.close();
			conn = null;
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public void testUploadTracker2() throws Exception {
		String request = "GET /test/tracker HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: text/plain\015\012" +
			"Connection: close\015\012" + 
			"Content-Length: 0\015\012\015\012";
		HttpTestConnection conn = null;
		String tracker = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			String response = conn.readString();
			
			assertNotNull(response);
			int startIdx = response.indexOf("http_upload_id=");
			assertTrue(startIdx > -1);
			int endIdx = response.indexOf(";", startIdx);
			tracker = response.substring(startIdx + 15, endIdx);
			
			conn.close();
			conn = null;
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	
		String requestHeader = "POST /test/upload HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: multipart/form-data; boundary=AaB03x\015\012" +
			"Connection: <connection>\015\012" +
			"Cookie: http_upload_id=<tracker>\015\012" +
			"Content-Length: <length>";
		
		MultipartInputStream in = null;
		
		try {
			conn = new HttpTestConnection();
			String data = createData(1048576);
			byte[] byteData = data.getBytes();
			in = new MultipartInputStream("AaB03x", new ByteArrayInputStream(byteData), byteData.length);
			in.setHeader("Content-Disposition", "form-data; name=\"pics\"; filename=\"file.txt\"");
			in.setHeader("Content-Type", "text/plain");
			
			request = requestHeader.replace("<length>", Long.toString(in.length())) + "\015\012\015\012";
			request = request.replace("<connection>", "keep-alive");
			request = request.replace("<tracker>", tracker);
			
			conn.write(request.getBytes());
			byte[] b = new byte[8192];
			int len = 0;
			int lenWritten = 0;
			int progress = 0;
			
			while((len = in.read(b)) > 0) {
				conn.write(b, 0, len);
				lenWritten += len;
				progress = getUploadProgress(tracker);
				assertTrue(progress >= 0);
			}
			
			assertTrue(byteData.length <= progress);
			assertTrue(lenWritten >= progress);
			
			byte[] responseBytes = conn.readKeepAliveBody();
			
			assertTrue(in.length() > byteData.length);
			assertEquals(byteData.length, responseBytes.length);
			
			for(int j = 0; j < byteData.length; j++) {
				assertTrue(byteData[j] == responseBytes[j]);
			}
			
			conn.close();
			conn = null;
		} finally {
			if(in != null) {
				try { in.close(); } catch(IOException e) {}
			}
			
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	private int getUploadProgress(String trackerId) throws Exception {
		String request = "GET /test/progress HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: text/plain\015\012" +
			"Connection: close\015\012" + 
			"Cookie: http_upload_id=<tracker>\015\012" +
			"Content-Length: 0\015\012\015\012";
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			request = request.replaceFirst("<tracker>", trackerId);
			conn.write(request);
			String response = conn.readBodyString("utf-8");
			
			assertNotNull(response);
			int value = Integer.parseInt(response);
			
			conn.close();
			conn = null;
			
			return value;			
		} finally {
			if(conn != null) {
				conn.close();
			}
		}				
	}
		
	private static final String chars = "ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvxyz0123456789";
	
	private static String createData(int length) {
		StringBuffer data = new StringBuffer();
		Random rnd = new Random();
		
		for(int i = 0; i < length; i++) {
			int idx = rnd.nextInt(chars.length());
			data.append(chars.substring(idx, idx + 1));
		}
		
		return data.toString();
	}
	
	public static Test suite() {
		return new TestSuite(Http11UploadTrackerTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
