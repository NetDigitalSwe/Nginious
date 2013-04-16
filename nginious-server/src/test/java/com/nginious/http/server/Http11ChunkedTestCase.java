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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.nginious.http.application.Application;
import com.nginious.http.application.ApplicationManager;
import com.nginious.http.service.TestBodyController;
import com.nginious.http.service.TestChunkingController;

public class Http11ChunkedTestCase extends TestCase {
	
	private HttpServer server;
	
	public Http11ChunkedTestCase() {
		super();
	}

	public Http11ChunkedTestCase(String name) {
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
		application.addController(new TestBodyController());
		application.addController(new TestChunkingController());
		manager.publish(application);
		server.start();
	}

	protected void tearDown() throws Exception {
		if(this.server != null) {
			server.stop();
		}
	}
	
	public void testChunkedClientEncoding() throws Exception {
		String requestHeader = "POST /test/body HTTP/1.0\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: application/octet-stream\015\012" +
			"Connection: close\015\012" + 
			"Transfer-Encoding: chunked";
		
		HttpTestConnection conn = null;
		
		try {
			for(int i = 10100; i < 1000000; i += 10000) {
				conn = new HttpTestConnection();
				
				byte[][] data = createClientData(i);
				String request = requestHeader + "\015\012\015\012" + new String(data[0]);
				conn.write(request);
				
				byte[] response = conn.readBody();
				assertEquals(data[1], response);
				
				conn.close();
				conn = null;
			}
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
		
	}

	public void testFragmentedChunkedClientEncoding() throws Exception {
		String requestHeader = "POST /test/body HTTP/1.0\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: application/octet-stream\015\012" +
			"Connection: close\015\012" + 
			"Transfer-Encoding: chunked";
		
		HttpTestConnection conn = null;
		
		try {
			for(int i = 2; i <= 8192; i *= 2) {
				conn = new HttpTestConnection();
				
				byte[][] data = createClientData(100100);
				String request = requestHeader + "\015\012\015\012" + new String(data[0]);
				byte[] outData = request.getBytes();
				int parts = outData.length / i;
				
				for(int j = 0; j < parts; j ++) {
					conn.write(outData, j * i, i);
				}
				
				conn.write(outData, parts * i, outData.length - (parts * i));
				
				byte[] response = conn.readBody();
				assertEquals(data[1], response);
				
				conn.close();
				conn = null;
			}
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
		
	}
	
	public void testEmptyChunkedClientEncoding() throws Exception {
		String requestHeader = "POST /test/body HTTP/1.0\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: application/octet-stream\015\012" +
			"Connection: close\015\012" + 
			"Transfer-Encoding: chunked";
	
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			
			byte[][] data = createClientData(0);
			String request = requestHeader + "\015\012\015\012" + new String(data[0]);
			
			conn.write(request);
			
			byte[] response = conn.readBody();
			assertEquals(data[1], response);
			
			conn.close();
			conn = null;
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
	}
	
	public void testChunkedServerEncoding() throws Exception {
		String requestHeader = "POST /test/chunking HTTP/1.0\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: application/octet-stream\015\012" +
			"Connection: close\015\012" + 
			"Content-Length: <length>";

		HttpTestConnection conn = null;
		
		try {
			for(int i = 1; i < 100100; i *= 2) {
				conn = new HttpTestConnection();
				
				byte[] data = createServerData(i);
				String request = requestHeader.replace("<length>", Integer.toString(data.length)) + "\015\012\015\012" + new String(data);
				conn.write(request);
				
				byte[] response = conn.readBody();
				response = decodeChunked(response);
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
	
	private void assertEquals(byte[] first, byte[] second) {
		assertEquals(first.length, second.length);
		
		for(int i = 0 ; i< first.length; i++) {
			if(first[i] != second[i]) {
				throw new AssertionError("Failed");
			}
		}
	}
	
	private static final String chars = "ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvxyz0123456789";
	
	private static byte[] decodeChunked(byte[] data) {
		boolean stateChunkLength = true;
		int pos = 0;
		int start = 0;
		int chunkLen = 0;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		while(pos < data.length) {
			if(stateChunkLength) {
				if(data[pos] == '\015' && data[pos + 1] == '\012') {
					String lenStr = new String(data, start, pos - start);
					chunkLen = Integer.parseInt(lenStr, 16);
					stateChunkLength = false;
					pos += 2;
				} else {
					pos++;
				}
			} else {
				out.write(data, pos, chunkLen);
				pos += chunkLen + 2;
				stateChunkLength = true;
				start = pos;
			}
		}
		
		return out.toByteArray();
	}
	
	private static byte[] createServerData(int length) {
		byte[] data = new byte[length];
		Random rnd = new Random();
		
		for(int i = 0; i < length; i++) {
			int idx = rnd.nextInt(chars.length());
			data[i] = chars.substring(idx, idx + 1).getBytes()[0];
		}
		
		return data;
	}
	
	private static byte[][] createClientData(int length) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] expectedResponse = new byte[length];
		int expectedResponsePos = 0;
		Random rnd = new Random();
		
		for(int i = 0; i < length; i+= 1024) {
			int end = i + 1024 > length ? length : i + 1024;
			int len = i + 1024 > length ? length - i : 1024;
			out.write(Integer.toHexString(len).getBytes());
			out.write("\015\012".getBytes());
			
			for(int j = i; j < end; j++) {
				int idx = rnd.nextInt(chars.length());
				expectedResponse[expectedResponsePos] = chars.substring(idx, idx + 1).getBytes()[0];
				out.write(expectedResponse[expectedResponsePos]);
				expectedResponsePos++;
			}
			
			out.write("\015\012".getBytes());
		}
		
		out.write("0\015\012".getBytes());
		byte[] requestData = out.toByteArray();
		byte[][] returnData = { requestData, expectedResponse };
		return returnData;
	}
	
	public static Test suite() {
		return new TestSuite(Http11ChunkedTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
