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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.nginious.http.application.Application;
import com.nginious.http.application.ApplicationManager;
import com.nginious.http.service.TestBodyController;

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
		config.setServerLogPath("build/test-server.log");
		config.setPort(9000);
		HttpServerFactory factory = HttpServerFactory.getInstance();
		this.server = factory.create(config);
		server.setAccessLogConsumer(new FileLogConsumer("build/test-access"));
		ApplicationManager manager = server.getApplicationManager();
		Application application = manager.createApplication("test");
		application.addController(new TestBodyController());
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
		int index = 0;
		
		try {
			for(int i = 0; i < numRounds; i++) {
				index = i;
				conn = new HttpTestConnection();
				byte[] data = createData(100);
				String request = requestHeader.replace("<length>", Integer.toString(data.length)) + "\n\n" + new String(data);
				conn.write(request);
				
				byte[] response = conn.readBody();
				assertEquals(data, response);
				
				conn.close();
				conn = null;
				Thread.sleep(1);
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Timeout i=" + index + " thread=" + Thread.currentThread().getName());
			throw e;
		} finally {
			if(conn != null) {
				conn.close();
			}
		}	
	}
	
	public void testStress() throws Exception {
		int numThreads = 10;
		CountDownLatch start = new CountDownLatch(numThreads);
		CountDownLatch stop = new CountDownLatch(numThreads);
		StressTester[] testers = new StressTester[numThreads];
		Thread[] threads = new Thread[numThreads];
		
		for(int i = 0; i < numThreads; i++) {
			StressTester tester = new StressTester(start, stop, "stress-tester-" + i);
			testers[i] = tester;
			threads[i] = new Thread(tester);
			threads[i].setName("stress-tester-" + i);
			threads[i].start();
			start.countDown();
		}
		
		stop.await();
		
		for(int i = 0; i < numThreads; i++) {
			Throwable t = testers[i].getThrowable();
			
			if(t != null) {
				assertNull(testers[i].getName(), t);
			}
		}
	}
	
	private class StressTester implements Runnable {
		
		private CountDownLatch start;
		
		private CountDownLatch stop;
		
		private Throwable t;
		
		private String name;
		
		StressTester(CountDownLatch start, CountDownLatch stop, String name) {
			this.start = start;
			this.stop = stop;
			this.name = name;
		}
		
		String getName() {
			return this.name;
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
