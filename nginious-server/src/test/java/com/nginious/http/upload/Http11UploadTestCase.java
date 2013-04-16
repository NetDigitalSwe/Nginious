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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.nginious.http.TestUtils;
import com.nginious.http.application.Application;
import com.nginious.http.application.ApplicationManager;
import com.nginious.http.server.FileLogConsumer;
import com.nginious.http.server.HttpServer;
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;
import com.nginious.http.server.HttpTestConnection;
import com.nginious.http.service.TestUploadController;

public class Http11UploadTestCase extends TestCase {
	
	private HttpServer server;
	
	public Http11UploadTestCase() {
		super();
	}

	public Http11UploadTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		HttpServerConfiguration config = new HttpServerConfiguration();
		config.setWebappsDir(null);
		config.setServerLogPath("build/test-server.log");
		config.setPort(9000);
		HttpServerFactory factory = HttpServerFactory.getInstance();
		this.server = factory.create(config);
		server.setAccessLogConsumer(new FileLogConsumer("build/test-access"));
		ApplicationManager manager = server.getApplicationManager();
		Application application = manager.createApplication("test");
		application.addController(new TestUploadController());
		manager.publish(application);
		server.start();
	}

	protected void tearDown() throws Exception {
		if(this.server != null) {
			server.stop();
		}
	}
	
	public void testUpload() throws Exception {
		String requestHeader = "POST /test/upload HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: multipart/form-data; boundary=AaB03x\015\012" +
			"Connection: <connection>\015\012" + 
			"Content-Length: <length>";
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			
			for(int i = 4096; i <= 131072; i += 4096) {
				String data = createData(i);
				String multi = createMultipartData(data);
				String request = requestHeader.replace("<length>", Integer.toString(multi.length())) + "\015\012\015\012" + multi;
				request = request.replace("<connection>", "keep-alive");
				
				conn.write(request);
				
				byte[] responseBytes = conn.readKeepAliveBody();
				String response = new String(responseBytes);
				assertEquals(data, response);
			}
			
			String data = createData(1756427);
			String multi = createMultipartData(data);
			String request = requestHeader.replace("<length>", Integer.toString(multi.length())) + "\015\012\015\012" + multi;
			request = request.replace("<connection>", "close");
			conn.write(request);
			
			byte[] responseBytes = conn.readKeepAliveBody();
			String response = new String(responseBytes);
			assertEquals(data, response);
			
			conn.close();
			conn = null;
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public void testUpload2() throws Exception {
		String requestHeader = "POST /test/upload HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: multipart/form-data; boundary=AaB03x\015\012" +
			"Connection: <connection>\015\012" + 
			"Content-Length: <length>";
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			String[] datas = { "\015\012--AsTest", "xyz" };
			
			for(String data : datas) {
				String multi = createMultipartData(data);
				String request = requestHeader.replace("<length>", Integer.toString(multi.length())) + "\015\012\015\012" + multi;
				request = request.replace("<connection>", "keep-alive");
				
				conn.write(request);
				
				byte[] responseBytes = conn.readKeepAliveBody();
				String response = new String(responseBytes);
				assertEquals(data, response);
			}
						
			conn.close();
			conn = null;
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public void testMultipartInputStreamUpload() throws Exception {
		String requestHeader = "POST /test/upload HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: multipart/form-data; boundary=AaB03x\015\012" +
			"Connection: <connection>\015\012" + 
			"Content-Length: <length>";
		HttpTestConnection conn = null;
		MultipartInputStream in = null;
		
		try {
			conn = new HttpTestConnection();
			
			for(int i = 4096; i <= 131072; i += 4096) {
				String data = createData(i);
				byte[] byteData = data.getBytes();
				in = new MultipartInputStream("AaB03x", new ByteArrayInputStream(byteData), byteData.length);
				in.setHeader("Content-Disposition", "form-data; name=\"pics\"; filename=\"file.txt\"");
				in.setHeader("Content-Type", "text/plain");

				String request = requestHeader.replace("<length>", Long.toString(in.length())) + "\015\012\015\012";
				request = request.replace("<connection>", "keep-alive");
				
				conn.write(request.getBytes());
				byte[] b = new byte[1024];
				int len = 0;
				
				while((len = in.read(b)) > 0) {
					conn.write(b, 0, len);
				}
				
				byte[] responseBytes = conn.readKeepAliveBody();
				
				assertTrue(in.length() > byteData.length);
				assertEquals(byteData.length, responseBytes.length);
				
				for(int j = 0; j < byteData.length; j++) {
					assertTrue(byteData[j] == responseBytes[j]);
				}
				
				in.close();
			}
			
			String data = createData(1756427);
			byte[] byteData = data.getBytes();
			in = new MultipartInputStream("AaB03x", new ByteArrayInputStream(byteData), byteData.length);
			in.setHeader("Content-Disposition", "form-data; name=\"pics\"; filename=\"file.txt\"");
			in.setHeader("Content-Type", "text/plain");

			String request = requestHeader.replace("<length>", Long.toString(in.length())) + "\015\012\015\012";
			request = request.replace("<connection>", "close");
			
			conn.write(request.getBytes());
			byte[] b = new byte[1024];
			int len = 0;
			
			while((len = in.read(b)) > 0) {
				conn.write(b, 0, len);
			}
			
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
	
	public void testFileUpload() throws Exception {
		String requestHeader = "POST /test/upload HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: multipart/form-data; boundary=AaB03x\015\012" +
			"Connection: <connection>\015\012" + 
			"Content-Length: <length>";
		HttpTestConnection conn = null;
		MultipartInputStream in = null;
		
		try {
			conn = new HttpTestConnection();
			
			File file = TestUtils.findFile("build/libs", "testweb");
			long fileLen = file.length();
			FileInputStream fIn = new FileInputStream(file);
			byte[] byteData = new byte[(int)fileLen];
			int read = fIn.read(byteData);
			assertEquals(fileLen, (long)read);
			fIn.close();
			
			in = new MultipartInputStream("AaB03x", new ByteArrayInputStream(byteData), byteData.length);
			in.setHeader("Content-Disposition", "form-data; name=\"pics\"; filename=\"file.txt\"");
			in.setHeader("Content-Type", "text/plain");
			
			String request = requestHeader.replace("<length>", Long.toString(in.length())) + "\015\012\015\012";
			request = request.replace("<connection>", "close");
			conn.write(request.getBytes());
			
			byte[] b = new byte[2048];
			int len = 0;
			
			while((len = in.read(b)) > 0) {
				conn.write(b, 0, len);
			}
			
			byte[] responseBytes = conn.readKeepAliveBody();
			
			assertTrue(in.length() > byteData.length);
			assertEquals(byteData.length, responseBytes.length);
			
			for(int j = 0; j < responseBytes.length; j++) {
				assertTrue("Pos: " + j + ", original: " + byteData[j] + ", response: " + responseBytes[j], byteData[j] == responseBytes[j]);
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
	
	private static String createMultipartData(String data) {
		StringBuffer multi = new StringBuffer();
		multi.append("--AaB03x\015\012");
		multi.append("Content-Disposition: form-data; name=\"pics\"; filename=\"file.txt\"\015\012");
		multi.append("Content-Type: text/plain\015\012\015\012");
		multi.append(data);
		multi.append("\015\012--AaB03x--\015\012");
		return multi.toString();
	}
	
	public static Test suite() {
		return new TestSuite(Http11UploadTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
