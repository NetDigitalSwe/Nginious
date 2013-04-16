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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.custommonkey.xmlunit.XMLTestCase;

import com.nginious.http.TestUtils;
import com.nginious.http.common.FileUtils;
import com.nginious.http.server.HttpServer;
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;
import com.nginious.http.server.HttpTestConnection;

public class Http11BackupTestCase extends XMLTestCase {
	
	private HttpServer server;
	
	private File tmpDir;
	
	public Http11BackupTestCase() {
		super();
	}

	public Http11BackupTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		this.tmpDir = new File(System.getProperty("java.io.tmpdir"), "webapps");
		tmpDir.mkdir();
		
		HttpServerConfiguration config = new HttpServerConfiguration();
		config.setAdminPwd("admin");
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
	
	public void testBackupDir() throws Exception {
		HttpTestConnection conn = null;
		
		try {
			conn = new HttpTestConnection();
			getAllNoContent(conn);
			File srcFile = TestUtils.findFile("build/libs", "testload1.war");
			publish(conn, srcFile.getAbsolutePath());
			service(conn, false);
			srcFile = TestUtils.findFile("build/libs", "testload2.war");
			rePublish(conn, srcFile.getAbsolutePath(), 2);
			getAll(conn);
			rollback(conn, 1);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
		
	private void service(HttpTestConnection conn, boolean three) throws Exception {
		String requestHeader = "GET /test/test?first=one&second=two&third=three HTTP/1.1\015\012" + 
			"Host: localhost\015\012" +
			"Content-Type: application/x-www-form-urlencoded\015\012" + 
			"Connection: keep-alive\015\012" +
			"Accept: text/xml\015\012" +
			"Content-Length: 0\015\012\015\012";
		
		byte[] header = requestHeader.getBytes();
		conn.write(header);
		byte[] responseBytes = conn.readKeepAliveBody();
		String xml = new String(responseBytes);
		
		assertXpathExists("/test-bean2", xml);
		assertXpathEvaluatesTo("one", "/test-bean2/first", xml);
		assertXpathEvaluatesTo("two", "/test-bean2/second", xml);
		
		if(three) {
			assertXpathEvaluatesTo("three", "/test-bean2/third", xml);			
		} else {
			assertXpathNotExists("test-bean2/third", xml);
		}
	}
	
	private void publish(HttpTestConnection conn, String warFileName) throws Exception {
		String requestHeader = "PUT /admin/application/test HTTP/1.1\015\012" + 
			"Authorization: Digest username=\"admin\", " +
				"realm=\"admin\", " +
				"nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", " +
				"uri=\"/admin\", " +
				"qop=auth, " +
				"nc=00000001, " +
				"cnonce=\"0a4f113b\", " +
				"response=\"ce26a7af74c4134152f3eafc94c9b802\", " +
				"opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"\015\012" +
			"Host: localhost\015\012" +
			"Content-Type: multipart/form-data; boundary=AaB03x\015\012" +
			"Connection: keep-alive\015\012" +
			"Accept: text/xml\015\012" +
			"Content-Length: <length>\015\012\015\012";
		
		byte[] multipartContent = createMultipartData(new File(warFileName));
		byte[] header = requestHeader.replace("<length>", Integer.toString(multipartContent.length)).getBytes();
		byte[] request = new byte[header.length + multipartContent.length];
		System.arraycopy(header, 0, request, 0, header.length);
		System.arraycopy(multipartContent, 0, request, header.length, multipartContent.length);
		
		conn.write(request);
		byte[] responseBytes = conn.readKeepAliveBody();
		String xml = new String(responseBytes);
		
		assertXpathExists("/application-info", xml);
		assertXpathEvaluatesTo("test", "/application-info/name", xml);
		assertXpathExists("/application-info/versions", xml);
		assertXpathExists("/application-info/versions/application-version", xml);
		assertXpathEvaluatesTo("0", "/application-info/versions/application-version/version-number", xml);
		assertXpathExists("/application-info/versions/application-version/publish-time", xml);
	}
	
	private void rePublish(HttpTestConnection conn, String warFileName, int numVersions) throws Exception {
		String requestHeader = "POST /admin/application/test HTTP/1.1\015\012" + 
			"Authorization: Digest username=\"admin\", " +
				"realm=\"admin\", " +
				"nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", " +
				"uri=\"/admin\", " +
				"qop=auth, " +
				"nc=00000001, " +
				"cnonce=\"0a4f113b\", " +
				"response=\"f437036b8aeba82446da5de5ad2d732f\", " +
				"opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"\015\012" +
			"Host: localhost\015\012" +
			"Content-Type: multipart/form-data; boundary=AaB03x\015\012" +
			"Connection: keep-alive\015\012" +
			"Accept: text/xml\015\012" +
			"Content-Length: <length>\015\012\015\012";
		
		byte[] multipartContent = createMultipartData(new File(warFileName));
		byte[] header = requestHeader.replace("<length>", Integer.toString(multipartContent.length)).getBytes();
		byte[] request = new byte[header.length + multipartContent.length];
		System.arraycopy(header, 0, request, 0, header.length);
		System.arraycopy(multipartContent, 0, request, header.length, multipartContent.length);
		
		conn.write(request);
		byte[] responseBytes = conn.readKeepAliveBody();
		String xml = new String(responseBytes);
		
		assertXpathExists("/application-info", xml);
		assertXpathEvaluatesTo("test", "/application-info/name", xml);
		assertXpathExists("/application-info/versions", xml);
		assertXpathExists("/application-info/versions/application-version", xml);
		
		for(int i = 1; i <= numVersions; i++) {
			int version = i - 1;
			assertXpathEvaluatesTo(Integer.toString(version), "/application-info/versions/application-version[" + i + "]/version-number", xml);
			assertXpathExists("/application-info/versions/application-version[" + i + "]/publish-time", xml);
		}
		
		int notExists = numVersions + 1;
		assertXpathNotExists("/application-info/versions/applivation-version[" + notExists + "]/version-number", xml);
		assertXpathNotExists("/application-info/versions/application-version[" + notExists + "]/publish-time", xml);		
	}
	
	private void rollback(HttpTestConnection conn, int numVersions) throws Exception {
		String requestHeader = "POST /admin/application/test HTTP/1.1\015\012" + 
			"Authorization: Digest username=\"admin\", " +
				"realm=\"admin\", " +
				"nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", " +
				"uri=\"/admin\", " +
				"qop=auth, " +
				"nc=00000001, " +
				"cnonce=\"0a4f113b\", " +
				"response=\"f437036b8aeba82446da5de5ad2d732f\", " +
				"opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"\015\012" +
			"Host: localhost\015\012" +
			"Content-Type: text/plain\015\012" + 
			"Connection: keep-alive\015\012" +
			"Accept: text/xml\015\012" +
			"Content-Length: 0\015\012\015\012";
		
		byte[] header = requestHeader.getBytes();
		conn.write(header);
		byte[] responseBytes = conn.readKeepAliveBody();
		String xml = new String(responseBytes);
		
		assertXpathExists("/application-info", xml);
		assertXpathEvaluatesTo("test", "/application-info/name", xml);
		assertXpathExists("/application-info/versions", xml);
		assertXpathExists("/application-info/versions/application-version", xml);
		
		for(int i = 1; i <= numVersions; i++) {
			int version = i - 1;
			assertXpathEvaluatesTo(Integer.toString(version), "/application-info/versions/application-version[" + i + "]/version-number", xml);
			assertXpathExists("/application-info/versions/application-version[" + i + "]/publish-time", xml);
		}
		
		int notExists = numVersions + 1;
		assertXpathNotExists("/application-info/versions/application-version[" + notExists + "]/version-number", xml);
		assertXpathNotExists("/application-info/versions/application-version[" + notExists + "]/publish-time", xml);		
	}
	
	private void getAll(HttpTestConnection conn) throws Exception {
		String requestHeader = "GET /admin/applications HTTP/1.1\015\012" + 
			"Authorization: Digest username=\"admin\", " +
				"realm=\"admin\", " +
				"nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", " +
				"uri=\"/admin\", " +
				"qop=auth, " +
				"nc=00000001, " +
				"cnonce=\"0a4f113b\", " +
				"response=\"fae9315716e12851e61f9608eda5543f\", " +
				"opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"\015\012" +
			"Host: localhost\015\012" +
			"Connection: keep-alive\015\012" +
			"Accept: text/xml\015\012\015\012";
		
		byte[] header = requestHeader.getBytes();
		conn.write(header);
		byte[] responseData = conn.readKeepAliveBody();
		String xml = new String(responseData);
		
		assertXpathExists("/applications-info", xml);
		assertXpathExists("/applications-info/applications", xml);
		assertXpathExists("/applications-info/applications/application-info", xml);
		assertXpathEvaluatesTo("test", "/applications-info/applications/application-info/name", xml);
		assertXpathEvaluatesTo("0", "/applications-info/applications/application-info/versions/application-version/version-number", xml);
	}
	
	private void getAllNoContent(HttpTestConnection conn) throws Exception {
		String requestHeader = "GET /admin/applications HTTP/1.1\015\012" + 
			"Authorization: Digest username=\"admin\", " +
				"realm=\"admin\", " +
				"nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", " +
				"uri=\"/admin\", " +
				"qop=auth, " +
				"nc=00000001, " +
				"cnonce=\"0a4f113b\", " +
				"response=\"fae9315716e12851e61f9608eda5543f\", " +
				"opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"\015\012" +
			"Host: localhost\015\012" +
			"Connection: keep-alive\015\012" +
			"Accept: text/xml\015\012\015\012";
		
		String expectedResponse = "HTTP/1.1 204 No Content\015\012" +
			"Date: <date>\015\012" +
			"Content-Length: 0\015\012" +
			"Connection: keep-alive\015\012" +
			"Server: Nginious/1.0.0\015\012\015\012";
		
		byte[] header = requestHeader.getBytes();
		conn.write(header);
		String response = conn.readKeepAliveString();
		expectedResponse = conn.setHeaders(response, expectedResponse);
		assertEquals(expectedResponse, response);
	}
	
	private static byte[] createMultipartData(File warFile) throws IOException {
		byte[] content = getFileContent(warFile);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		out.write("--AaB03x\015\012".getBytes());
		out.write("Content-Disposition: form-data; name=\"test\"; filename=\"test.war\"\015\012".getBytes());
		out.write("Content-Type: application/octet-stream\015\012\015\012".getBytes());
		out.write(content);
		out.write("\015\012--AaB03x--\015\012".getBytes());
		return out.toByteArray();
	}
	
	private static byte[] getFileContent(File warFile) throws IOException {
		FileInputStream in = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		try {
			in = new FileInputStream(warFile);
			byte[] b = new byte[1024];
			int len = 0;
			
			while((len = in.read(b)) > 0) {
				out.write(b, 0, len);
			}
			
			return out.toByteArray();
		} finally {
			if(in != null) {
				try { in.close(); } catch(IOException e) {}
			}
		}
	}
	
	public static Test suite() {
		return new TestSuite(Http11BackupTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
