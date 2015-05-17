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

import java.io.File;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.json.JSONObject;

import com.nginious.http.common.FileUtils;
import com.nginious.http.server.HttpServer;
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;
import com.nginious.http.server.HttpTestConnection;

public class Http11SerializerTestCase extends TestCase {
	
	private HttpServer server;
	
	private File tmpDir;
	
    public Http11SerializerTestCase() {
		super();
	}

	public Http11SerializerTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		this.tmpDir = new File(System.getProperty("java.io.tmpdir"), "webapps");
		tmpDir.mkdir();
		File destFile = new File(this.tmpDir, "test.war");
		FileUtils.copyFile("build/libs/nginious-server-0.9.2-testweb.war", destFile.getAbsolutePath());
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
	
	public void testHttpSerializerGet() throws Exception {
		String request = "GET /test/serializer HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 0\015\012" + 
			"Accept: application/json\015\012" +
			"Connection: close\015\012\015\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			String body = conn.readBodyString("utf-8");
			testResponse(body);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
		
	public void testHttpSerializerPost() throws Exception {
		String request = "POST /test/serializer HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 0\015\012" + 
			"Accept: application/json\015\012" +
			"Connection: close\015\012\015\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			String body = conn.readBodyString("utf-8");
			testResponse(body);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}

	public void testHttpSerializerPut() throws Exception {
		String request = "PUT /test/serializer HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 0\015\012" + 
			"Accept: application/json\015\012" +
			"Connection: close\015\012\015\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			String body = conn.readBodyString("utf-8");
			testResponse(body);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public void testHttpSerializerDelete() throws Exception {
		String request = "DELETE /test/serializer HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Length: 0\015\012" + 
			"Accept: application/json\015\012" +
			"Connection: close\015\012\015\012";
		
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			String body = conn.readBodyString("utf-8");
			testResponse(body);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	private void testResponse(String body) throws Exception {
		JSONObject bean = new JSONObject(body);
		assertTrue(bean.has("testBean1"));
		bean = bean.getJSONObject("testBean1");
		
		assertEquals(true, bean.getBoolean("first"));
		assertEquals(1.1d, bean.getDouble("second"));
		assertEquals(1.2f, (float)bean.getDouble("third"));
		assertEquals(2, bean.getInt("fourth"));
		assertEquals(5L, bean.getLong("fifth"));
		assertEquals((short)3, (short)bean.getInt("sixth"));
		assertEquals("Seven", bean.getString("seventh"));
		assertTrue(bean.has("eight"));
		assertTrue(bean.has("ninth"));
	}
	
	public static Test suite() {
		return new TestSuite(Http11SerializerTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
