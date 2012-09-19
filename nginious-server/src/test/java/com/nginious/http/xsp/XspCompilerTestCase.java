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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.nginious.http.HttpException;
import com.nginious.http.application.Service;
import com.nginious.http.server.HttpTestRequest;
import com.nginious.http.server.HttpTestResponse;
import com.nginious.http.xsp.XspCompiler;
import com.nginious.http.xsp.XspException;
import com.nginious.http.xsp.XspService;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class XspCompilerTestCase extends TestCase {
	
	public XspCompilerTestCase() {
		super();
	}

	public XspCompilerTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		
		File classDir = new File(System.getProperty("java.io.tmpdir"), "com/nginious/http/xsp");
		File[] classFiles = classDir.listFiles();
		
		if(classFiles != null) {
			for(File classFile : classFiles) {
				classFile.delete();
			}
		}
		
		File dir = new File(System.getProperty("java.io.tmpdir"), "com/nginious/http/xsp");
		dir.delete();
		dir = new File(System.getProperty("java.io.tmpdir"), "com/nginious/http");
		dir.delete();
		dir = new File(System.getProperty("java.io.tmpdir"), "tmp/com/nginious");
		dir.delete();
		dir = new File(System.getProperty("java.io.tmpdir"), "com");
		dir.delete();
	}
	
	public void testXspSimpleCompile() throws Exception {
		XspCompiler compiler = new XspCompiler();
		boolean result = compiler.compileClass("src/testweb/webapp/WEB-INF", 
				"src/testweb/webapp/WEB-INF/xsp/XspCompilerTest.xsp", 
				System.getProperty("java.io.tmpdir"));
		assertTrue(result);
		File classFile = new File(System.getProperty("java.io.tmpdir"), "com/nginious/http/xsp/XspCompilerTestService.class");
		assertTrue(classFile.exists());
	}
	
	public void testXspTagsCompile() throws Exception {
		XspCompiler compiler = new XspCompiler();
		boolean result = compiler.compileClass("src/testweb/webapp/WEB-INF", 
				"src/testweb/webapp/WEB-INF/xsp/XspTagsCompileTest.xsp",
				System.getProperty("java.io.tmpdir"));
		assertTrue(result);
		File classFile = new File(System.getProperty("java.io.tmpdir"), "com/nginious/http/xsp/XspTagsCompileTestService.class");
		assertTrue(classFile.exists());		
	}
	
	public void testXspOutput() throws Exception {
		XspCompiler compiler = new XspCompiler();
		XspService service = compiler.compileService("src/testweb/webapp/WEB-INF/xsp/XspOutputTest.xsp");
		assertNotNull(service);
		
		HttpTestRequest request = new HttpTestRequest();
		HttpTestResponse response = new HttpTestResponse();
		service.executeGet(request, response);
		String content = new String(response.getContent());
		String contentType = response.getHeader("Content-Type");
		assertEquals("text/html; charset=utf-8", contentType);
		assertEquals("<html>\n  <head>\n    \n    <title>XspOutputTest</title>\n  </head>\n  <body>\n    <h1>XspOutputTest</h1>\n  </body>\n</html>\n", content);
	}
	
	public void testXspTags() throws Exception {
		XspCompiler compiler = new XspCompiler();
		XspService service = compiler.compileService("src/testweb/webapp/WEB-INF/xsp/XspTagsTest.xsp");
		assertNotNull(service);
		
		ArrayList<String> testSet = new ArrayList<String>();
		testSet.add("1");
		testSet.add("2");
		testSet.add("3");
		testSet.add("4");
		testSet.add("5");
		testSet.add("6");
		testSet.add("7");
		testSet.add("8");
		testSet.add("9");
		testSet.add("10");

		HttpTestRequest request = new HttpTestRequest();
		request.setAttribute("test", "Hello world!");
		request.setAttribute("testset", testSet);
		HttpTestResponse response = new HttpTestResponse();
		service.executeGet(request, response);
		String content = new String(response.getContent());
		String contentType = response.getHeader("Content-Type");
		assertEquals("text/html; charset=utf-8", contentType);
		assertTrue(content.indexOf("Hello world! 1") > -1);
		assertTrue(content.indexOf("Hello world! 3") > -1);
		assertTrue(content.indexOf("Hello world! 3") > content.indexOf("Hello world! 1"));
		assertTrue(content.indexOf("Hello world! 5") > -1);
		assertTrue(content.indexOf("Hello world! 5") > content.indexOf("Hello world! 3"));
		assertTrue(content.indexOf("Hello world! 7") > -1);
		assertTrue(content.indexOf("Hello world! 7") > content.indexOf("Hello world! 5"));
		assertTrue(content.indexOf("Hello world! 9") > -1);
		assertTrue(content.indexOf("Hello world! 9") > content.indexOf("Hello world! 7"));
		
		// Test no collection
		request = new HttpTestRequest();
		response = new HttpTestResponse();
		service.executeGet(request, response);
		content = new String(response.getContent());
		contentType = response.getHeader("Content-Type");
		assertEquals("text/html; charset=utf-8", contentType);
		assertTrue(content.indexOf("Hello world! 1") == -1);
		assertTrue(content.indexOf("Hello world! 3") == -1);
		assertTrue(content.indexOf("Hello world! 5") == -1);
		assertTrue(content.indexOf("Hello world! 7") == -1);
		assertTrue(content.indexOf("Hello world! 9") == -1);
		
		// Test invalid collection
		try {
			request = new HttpTestRequest();
			request.setAttribute("test", "Hello world!");
			request.setAttribute("testset", new Date());
			response = new HttpTestResponse();
			service.executeGet(request, response);
			fail("Must not be possible to execute with bad collection");
		} catch(HttpException e) {}
	}
	
	public void testXspCharacter() throws Exception {
		XspCompiler compiler = new XspCompiler(Thread.currentThread().getContextClassLoader());
		XspService service = compiler.compileService("src/testweb/webapp/WEB-INF/xsp/XspCharacterTest.xsp");
		assertNotNull(service);
		
		HttpTestRequest request = new HttpTestRequest();		
		request.setAttribute("test", "åäö");
		HttpTestResponse response = new HttpTestResponse();
		service.executeGet(request, response);
		String content = new String(response.getContent());
		String contentType = response.getHeader("Content-Type");
		assertEquals("text/html; charset=utf-8", contentType);
		assertTrue(content.indexOf("Hello world!") > -1);
	}
	
	public void testXspAnnotations() throws Exception {
		XspCompiler compiler = new XspCompiler();
		XspService service = compiler.compileService("src/testweb/webapp/WEB-INF/xsp/XspAnnotationsTest.xsp");
		assertNotNull(service);
		
		Class<?> clazz = service.getClass();
		Service mapping = clazz.getAnnotation(Service.class);
		assertEquals("src/testweb/webapp/WEB-INF/xsp/XspAnnotationsTest.xsp", mapping.path());
		assertEquals(0, mapping.index());
		assertEquals("HEAD,GET,POST,PUT,DELETE", mapping.methods());
		assertEquals("", mapping.pattern());
	}
	
	public void testXspDateTag() throws Exception {
		XspCompiler compiler = new XspCompiler(Thread.currentThread().getContextClassLoader());
		XspService service = compiler.compileService("src/testweb/webapp/WEB-INF/xsp/XspDateTagTest.xsp");
		assertNotNull(service);
		
		Date curTime = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss Z");
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		String formattedCurTime = format.format(curTime);
		
		// Test normal
		HttpTestRequest request = new HttpTestRequest();		
		request.setAttribute("test", new Date());
		HttpTestResponse response = new HttpTestResponse();
		response.setLocale(Locale.getDefault());
		service.executeGet(request, response);
		String content = new String(response.getContent());
		String contentType = response.getHeader("Content-Type");
		assertEquals("text/html; charset=utf-8", contentType);
		assertTrue(content.indexOf(formattedCurTime) > -1);
		assertEquals(formattedCurTime, request.getAttribute("test2"));
		
		// Test unset attribute
		request = new HttpTestRequest();		
		response = new HttpTestResponse();
		response.setLocale(Locale.getDefault());
		service.executeGet(request, response);
		content = new String(response.getContent());
		contentType = response.getHeader("Content-Type");
		assertEquals("text/html; charset=utf-8", contentType);
		assertTrue(content.indexOf(formattedCurTime) == -1);

		// Test invalid type
		request = new HttpTestRequest();		
		request.setAttribute("test", "Invalid string");
		response = new HttpTestResponse();
		
		try {
			service.executeGet(request, response);
			fail("Must not be possible to execute with invalid date type");
		} catch(HttpException e) {}
	}
	
	public void testXspNumberTag() throws Exception {
		XspCompiler compiler = new XspCompiler(Thread.currentThread().getContextClassLoader());
		XspService service = compiler.compileService("src/testweb/webapp/WEB-INF/xsp/XspNumberTagTest.xsp");
		assertNotNull(service);
		
		double value = 100000;
		DecimalFormat format = new DecimalFormat("#,###");
		String formattedNum = format.format(value);
		
		// Test format
		HttpTestRequest request = new HttpTestRequest();		
		request.setAttribute("test", value);
		HttpTestResponse response = new HttpTestResponse();
		service.executeGet(request, response);
		String content = new String(response.getContent());
		String contentType = response.getHeader("Content-Type");
		assertEquals("text/html; charset=utf-8", contentType);
		assertTrue(content.indexOf(formattedNum) > -1);
		assertEquals(formattedNum, request.getAttribute("test2"));
		
		// Test unset attribute
		request = new HttpTestRequest();		
		response = new HttpTestResponse();
		service.executeGet(request, response);
		content = new String(response.getContent());
		contentType = response.getHeader("Content-Type");
		assertEquals("text/html; charset=utf-8", contentType);
		assertTrue(content.indexOf(formattedNum) == -1);

		// Test invalid type
		request = new HttpTestRequest();		
		request.setAttribute("test", new Date());
		response = new HttpTestResponse();
		
		try {
			service.executeGet(request, response);
			fail("Must not be possible to execute with invalid double value");
		} catch(HttpException e) {}
	}
	
	public void testXspMessageTag() throws Exception {
		XspCompiler compiler = new XspCompiler(Thread.currentThread().getContextClassLoader());
		XspService service = compiler.compileService("src/testweb/webapp/WEB-INF/xsp/XspMessageTagTest.xsp");
		assertNotNull(service);
		
		HttpTestRequest request = new HttpTestRequest();		
		Object[] args = { "test1", "test2" };
		request.setAttribute("test", args);
		HttpTestResponse response = new HttpTestResponse();
		response.setLocale(Locale.getDefault());
		service.executeGet(request, response);
		String content = new String(response.getContent());
		String contentType = response.getHeader("Content-Type");
		assertEquals("text/html; charset=utf-8", contentType);
		assertTrue(content.indexOf("Hello test1 test2") > -1);
		
		// Test unset args
		request = new HttpTestRequest();		
		response = new HttpTestResponse();
		response.setLocale(Locale.getDefault());
		service.executeGet(request, response);
		content = new String(response.getContent());
		contentType = response.getHeader("Content-Type");
		assertEquals("text/html; charset=utf-8", contentType);
		assertTrue(content.indexOf("Hello {0} {1}") > -1);
	}
	
	public void testXspBadExpression() throws Exception {
		XspCompiler compiler = new XspCompiler(Thread.currentThread().getContextClassLoader());

		try {
			compiler.compileService("src/testweb/webapp/WEB-INF/xsp/XspBadExpressionTest.xsp");
			fail("Must not be possible to compile with invalid expression");
		} catch(XspException e) {}
	}
	
	public void testXspMissingTestExpression() throws Exception {
		XspCompiler compiler = new XspCompiler(Thread.currentThread().getContextClassLoader());

		try {
			compiler.compileService("src/testweb/webapp/WEB-INF/xsp/XspMissingTestExpression.xsp");
			fail("Must not be possible to compile with missing test expression for if tag");
		} catch(XspException e) {}
	}
	
	public static Test suite() {
		return new TestSuite(XspCompilerTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
