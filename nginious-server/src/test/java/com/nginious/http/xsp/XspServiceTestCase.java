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

package com.nginious.http.xsp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.nginious.http.TestUtils;
import com.nginious.http.common.FileUtils;
import com.nginious.http.server.HttpServer;
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;
import com.nginious.http.server.HttpTestConnection;

public class XspServiceTestCase extends TestCase {
	
	private HttpServer server;
	
	private File tmpDir;
	
    public XspServiceTestCase() {
		super();
	}

	public XspServiceTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		this.tmpDir = new File(System.getProperty("java.io.tmpdir"), "webapps");
		tmpDir.mkdirs();
		File warFile = new File(this.tmpDir, "test.war");
		File srcFile = TestUtils.findFile("build/libs", "testweb");
		FileUtils.copyFile(srcFile.getAbsolutePath(), warFile.getAbsolutePath());
		File uncompiledDir = new File(this.tmpDir, "uncompiled");
		FileUtils.copyDir("src/testweb/webapp", uncompiledDir.getAbsolutePath());
		File classesDir = new File(uncompiledDir, "WEB-INF/classes");
		FileUtils.copyDir("build/classes/testweb/classes", classesDir.getAbsolutePath());
		HttpServerConfiguration config = new HttpServerConfiguration();
		config.setWebappsDir(tmpDir.getAbsolutePath());
		config.setServerLogPath("build/test-server.log");
		config.setAccessLogPath("build/test-access.log");
		config.setPort(9000);
		HttpServerFactory factory = HttpServerFactory.getInstance();
		this.server = factory.create(config);
		server.start();
	}

	protected void tearDown() throws Exception {
		if(this.server != null) {
			server.stop();
		}
		
		FileUtils.deleteDir(this.tmpDir);
	}
	
	public void testXspService() throws Exception {
		String request = "GET /test/xsp HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: text/html; charset=utf-8\015\012" +
			"Date: <date>\015\012" + 
			"Content-Length: 190\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"<html>\n  <head>";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			String response = conn.readString();
			expectedResponse = conn.setHeaders(response, expectedResponse);
			assertTrue(response, response.startsWith(expectedResponse));
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public void testUncompiledXspService() throws Exception {
		String request = "GET /uncompiled/xsp HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 0\015\012" + 
			"Connection: close\015\012\015\012";
		
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: text/html; charset=utf-8\015\012" +
			"Date: <date>\015\012" + 
			"Content-Length: 186\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012";
		
		HttpTestConnection conn = null;
		
		File srcFile = new File(this.tmpDir, "uncompiled/WEB-INF/xsp/XspTest1.xsp");
		File destFile = new File(this.tmpDir, "uncompiled/WEB-INF/xsp/XspTest.xsp");
		copy(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			String response = conn.readString();
			expectedResponse = conn.setHeaders(response, expectedResponse);
			assertTrue(response, response.startsWith(expectedResponse));
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
		
		Thread.sleep(1000L);
		srcFile = new File(this.tmpDir, "uncompiled/WEB-INF/xsp/XspTest2.xsp");
		destFile = new File(this.tmpDir, "uncompiled/WEB-INF/xsp/XspTest.xsp");		
		copy(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
		
		expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: text/html; charset=utf-8\015\012" +
			"Date: <date>\015\012" + 
			"Content-Length: 190\015\012" +
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012";
	
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			String response = conn.readString();
			expectedResponse = conn.setHeaders(response, expectedResponse);
			assertTrue(response, response.startsWith(expectedResponse));
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	private void copy(String from, String to) throws IOException {
		FileInputStream in = null;
		FileOutputStream out = null;
		
		try {
			in = new FileInputStream(from);
			out = new FileOutputStream(to);
			byte[] b = new byte[1024];
			int len = 0;
			
			while((len = in.read(b)) > 0) {
				out.write(b, 0, len);
			}
			
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
		} finally {
			if(in != null) {
				try { in.close(); } catch(IOException e) {}
			}
			
			if(out != null) {
				try { out.close(); } catch(IOException e) {}
			}
		}
	}
	
	public static Test suite() {
		return new TestSuite(XspServiceTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
