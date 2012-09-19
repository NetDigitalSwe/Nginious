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

import java.util.Random;
import java.util.concurrent.CountDownLatch;

import com.nginious.http.application.Application;
import com.nginious.http.application.ApplicationManager;
import com.nginious.http.server.HttpServer;
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;
import com.nginious.http.service.TestBodyService;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class Http11StressTestCase extends TestCase {
	
	private HttpServer server;
	
	public Http11StressTestCase() {
		super();
	}

	public Http11StressTestCase(String name) {
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
		application.addHttpService(new TestBodyService());
		manager.publish(application);
		server.start();
	}

	protected void tearDown() throws Exception {
		if(this.server != null) {
			server.stop();
		}
	}
	
	public void testResource() throws Exception {
		testResource(10000);
	}
	
	private void testResource(int numRounds) throws Exception {
		String requestHeader = "POST /test/body HTTP/1.0\n" + 
			"Host: localhost\n" +
			"Content-Type: application/octet-stream\n" +
			"Connection: close\n" + 
			"Content-Length: <length>";
		
		HttpTestConnection conn = null;
		
		try {
			for(int i = 0; i < numRounds; i++) {
				conn = new HttpTestConnection();
				byte[] data = createData(100);
				String request = requestHeader.replace("<length>", Integer.toString(data.length)) + "\n\n" + new String(data);
				conn.write(request);
				
				byte[] response = conn.readBody();
				assertEquals(data, response);
				
				conn.close();
				conn = null;
			}
		} finally {
			if(conn != null) {
				conn.close();
			}
		}	
	}
	
	public void testStress() throws Exception {
		CountDownLatch start = new CountDownLatch(10);
		CountDownLatch stop = new CountDownLatch(10);
		StressTester[] testers = new StressTester[10];
		Thread[] threads = new Thread[10];
		
		for(int i = 0; i < 10; i++) {
			StressTester tester = new StressTester(start, stop);
			testers[i] = tester;
			threads[i] = new Thread(tester);
			threads[i].start();
			start.countDown();
		}
		
		stop.await();
		
		for(int i = 0; i < 10; i++) {
			Throwable t = testers[i].getThrowable();
			
			if(t != null) {
				assertNull(t);
			}
		}
	}
	
	private class StressTester implements Runnable {
		
		private CountDownLatch start;
		
		private CountDownLatch stop;
		
		private Throwable t;
		
		StressTester(CountDownLatch start, CountDownLatch stop) {
			this.start = start;
			this.stop = stop;
		}
		
		Throwable getThrowable() {
			return this.t;
		}
		
		public void run() {
			try {
				start.await();
				testResource(1000);
			} catch(Throwable t) {
				this.t = t;
			} finally {
				stop.countDown();
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
	
	private static final String chars = "ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvxyz0123456789";
	
	private static byte[] createData(int length) {
		byte[] data = new byte[length];
		Random rnd = new Random();
		
		for(int i = 0; i < length; i++) {
			int idx = rnd.nextInt(chars.length());
			data[i] = chars.substring(idx, idx + 1).getBytes()[0];
		}
		
		return data;
	}
	
	public static Test suite() {
		return new TestSuite(Http11StressTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
