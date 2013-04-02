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

package com.nginious.http.serialize;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.nginious.http.application.Application;
import com.nginious.http.application.ApplicationManager;
import com.nginious.http.common.FileUtils;
import com.nginious.http.server.FileLogConsumer;
import com.nginious.http.server.HttpServer;
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;
import com.nginious.http.server.HttpTestConnection;
import com.nginious.http.service.TestAsyncRestController;

public class TestAsyncResponseTestCase extends TestCase {
	
	private HttpServer server;
	
	private File tmpDir;
	
	public TestAsyncResponseTestCase() {
		super();
	}

	public TestAsyncResponseTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		this.tmpDir = new File(System.getProperty("java.io.tmpdir"), "webapps");
		tmpDir.mkdir();
		// File destFile = new File(this.tmpDir, "test.war");
		// File srcFile = TestUtils.findFile("build/libs", "testweb");
		// FileUtils.copyFile(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
		
		HttpServerConfiguration config = new HttpServerConfiguration();
		config.setWebappsDir(tmpDir.getAbsolutePath());
		config.setPort(9000);
		HttpServerFactory factory = HttpServerFactory.getInstance();
		this.server = factory.create(config);
		ApplicationManager manager = server.getApplicationManager();
		Application application = manager.createApplication("test");
		application.addController(new TestAsyncRestController());
		manager.publish(application);
		server.setAccessLogConsumer(new FileLogConsumer("build/test-access"));
		server.setMessageLogConsumer(new FileLogConsumer("build/test-server"));
		server.start();
	}

	protected void tearDown() throws Exception {
		if(this.server != null) {
			server.stop();
		}
		
		FileUtils.deleteDir(this.tmpDir);
	}
	
	public void testAsyncResponse() throws Exception {
		String request = "GET /test/rasync?first=true&second=0.567&third=0.452&fourth=10&fifth=3400000000&sixth=32767&" +
			"seventh=String&eight=2011-08-24T08:50:23%2B02:00&ninth=2011-08-24T08:52:23%2B02:00 HTTP/1.1\015\012" +
			"Host: localhost\015\012" +
			"Content-Type: application/x-www-form-urlencoded\015\012" +
			"Accept: text/xml\015\012" +
			"Connection: close\015\012\015\012"; 
			
		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
			"Content-Type: text/xml; charset=utf-8\015\012" +
			"Date: <date>\015\012" +
			"Content-Length: 322\015\012" + 
			"Connection: close\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012" +
			"<?xml version=\"1.0\" encoding=\"utf-8\"?>\012" +
			"<test-bean2>\n" +
			"  <first>true</first>\n" +
			"  <second>0.567</second>\n" +
			"  <third>0.452</third>\n" +
			"  <fourth>10</fourth>\n" +
			"  <fifth>3400000000</fifth>\n" +
			"  <sixth>32767</sixth>\n" +
			"  <seventh>String</seventh>\n" +
			"  <eight>2011-08-24T08:50:23+02:00</eight>\n" +
			"  <ninth>2011-08-24T08:52:23+02:00</ninth>\n" +
			"</test-bean2>";
		
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
	
	public static Test suite() {
		return new TestSuite(TestAsyncResponseTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
