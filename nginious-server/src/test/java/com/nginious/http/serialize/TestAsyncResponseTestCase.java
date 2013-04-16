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
import junit.framework.TestSuite;

import org.custommonkey.xmlunit.XMLTestCase;

import com.nginious.http.application.Application;
import com.nginious.http.application.ApplicationManager;
import com.nginious.http.common.FileUtils;
import com.nginious.http.server.FileLogConsumer;
import com.nginious.http.server.HttpServer;
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;
import com.nginious.http.server.HttpTestConnection;
import com.nginious.http.service.TestAsyncRestController;

public class TestAsyncResponseTestCase extends XMLTestCase {
	
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
		
		HttpServerConfiguration config = new HttpServerConfiguration();
		config.setWebappsDir(tmpDir.getAbsolutePath());
		config.setServerLogPath("build/test-server.log");
		config.setPort(9000);
		HttpServerFactory factory = HttpServerFactory.getInstance();
		this.server = factory.create(config);
		ApplicationManager manager = server.getApplicationManager();
		Application application = manager.createApplication("test");
		application.addController(new TestAsyncRestController());
		server.setAccessLogConsumer(new FileLogConsumer("build/test-access"));
		server.start();
		manager.publish(application);
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
			
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection(4000);
			conn.write(request);
			
			long startTimeMillis = System.currentTimeMillis();
			String xml = conn.readBodyString("utf-8");
			long endTimeMillis = System.currentTimeMillis();
			assertTrue(endTimeMillis - startTimeMillis >= 2000);

			assertXpathExists("/test-bean2", xml);
	        assertXpathEvaluatesTo("true", "/test-bean2/first", xml);
	        assertXpathEvaluatesTo("0.567", "/test-bean2/second", xml);
	        assertXpathEvaluatesTo("0.452", "/test-bean2/third", xml);
	        assertXpathEvaluatesTo("10", "/test-bean2/fourth", xml);
	        assertXpathEvaluatesTo("3400000000", "/test-bean2/fifth", xml);
	        assertXpathEvaluatesTo("32767", "/test-bean2/sixth", xml);
	        assertXpathEvaluatesTo("String", "/test-bean2/seventh", xml);
	        assertXpathEvaluatesTo("2011-08-24T08:50:23+02:00", "/test-bean2/eight", xml);
	        assertXpathEvaluatesTo("2011-08-24T08:52:23+02:00", "/test-bean2/ninth", xml);
			
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
