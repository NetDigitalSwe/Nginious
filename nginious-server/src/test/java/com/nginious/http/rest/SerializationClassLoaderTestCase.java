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

package com.nginious.http.rest;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.custommonkey.xmlunit.XMLTestCase;

import com.nginious.http.HttpMethod;
import com.nginious.http.TestUtils;
import com.nginious.http.application.ApplicationClassLoader;
import com.nginious.http.common.FileUtils;
import com.nginious.http.rest.RestService;
import com.nginious.http.server.HttpTestRequest;
import com.nginious.http.server.HttpTestResponse;

public class SerializationClassLoaderTestCase extends XMLTestCase {
	
	private File tmpDir;
	
    public SerializationClassLoaderTestCase() {
		super();
	}
    
	public SerializationClassLoaderTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		this.tmpDir = new File(System.getProperty("java.io.tmpdir"), "webapps");
		
		File dir = new File(this.tmpDir, "classload/WEB-INF/lib");
		dir.mkdirs();
		dir = new File(this.tmpDir, "classload/WEB-INF/classes");
		dir.mkdir();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		FileUtils.deleteDir(this.tmpDir);
	}
	
	public void testSerialization() throws Exception {
		File destFile = new File(this.tmpDir, "classload/WEB-INF/lib/nginious-loader.jar");
		File srcFile = TestUtils.findFile("build/libs", "testload1");
		FileUtils.copyFile(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
		
		File webappDir = new File(this.tmpDir, "classload");
		ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
		contextLoader.loadClass("com.nginious.http.rest.InBean");
		contextLoader.loadClass("com.nginious.http.rest.InBean");
		
		ApplicationClassLoader loader = new ApplicationClassLoader(contextLoader, webappDir);
		
		Class<?> serviceClazz = loader.loadClass("com.nginious.http.loader.TestService");
		serviceClazz = loader.loadClass("com.nginious.http.loader.TestService");
		serviceClazz = loader.loadClass("com.nginious.http.loader.TestService");
		RestService<?, ?> service = (RestService<?, ?>)serviceClazz.newInstance();
		InvokeRestService invoke = new InvokeRestService(service);
		
		HttpTestRequest request = new HttpTestRequest();
		request.setMethod(HttpMethod.GET);
		request.addHeader("Content-Type", "application/x-www-form-urlencoded");
		request.addHeader("Accept", "text/xml");
		request.addParameter("first", "one");
		request.addParameter("second", "two");
		request.addParameter("third", "three");
		
		HttpTestResponse response = new HttpTestResponse();
		
		invoke.invoke(request, response);
		byte[] content = response.getContent();
		String xml = new String(content);
		
		assertXpathExists("/test-bean2", xml);
        assertXpathEvaluatesTo("one", "/test-bean2/first", xml);
        assertXpathEvaluatesTo("two", "/test-bean2/second", xml);
        
        service = null;
		Thread.sleep(1000L);
		
		srcFile = TestUtils.findFile("build/libs", "testload2");
		FileUtils.copyFile(srcFile.getAbsolutePath(), destFile.getAbsolutePath());

		serviceClazz = loader.loadClass("com.nginious.http.loader.TestService");
		service = (RestService<?, ?>)serviceClazz.newInstance();
		invoke = new InvokeRestService(service);
		
		request = new HttpTestRequest();
		request.setMethod(HttpMethod.GET);
		request.addHeader("Content-Type", "application/x-www-form-urlencoded");
		request.addHeader("Accept", "text/xml");
		request.addParameter("first", "one");
		request.addParameter("second", "two");
		request.addParameter("third", "three");
		
		response = new HttpTestResponse();
		
		invoke.invoke(request, response);
		content = response.getContent();
		xml = new String(content);
		
		assertXpathExists("/test-bean2", xml);
        assertXpathEvaluatesTo("one", "/test-bean2/first", xml);
        assertXpathEvaluatesTo("two", "/test-bean2/second", xml);
        assertXpathEvaluatesTo("three", "/test-bean2/third", xml);

        service = null;
		Thread.sleep(1000L);
		
		srcFile = TestUtils.findFile("build/libs", "testload1");
		FileUtils.copyFile(srcFile.getAbsolutePath(), destFile.getAbsolutePath());

		serviceClazz = loader.loadClass("com.nginious.http.loader.TestService");
		service = (RestService<?, ?>)serviceClazz.newInstance();
		invoke = new InvokeRestService(service);
		
		request = new HttpTestRequest();
		request.setMethod(HttpMethod.GET);
		request.addHeader("Content-Type", "application/x-www-form-urlencoded");
		request.addHeader("Accept", "text/xml");
		request.addParameter("first", "one");
		request.addParameter("second", "two");
		request.addParameter("third", "three");
		
		response = new HttpTestResponse();
		
		invoke.invoke(request, response);
		content = response.getContent();
		xml = new String(content);
		
		assertXpathExists("/test-bean2", xml);
        assertXpathEvaluatesTo("one", "/test-bean2/first", xml);
        assertXpathEvaluatesTo("two", "/test-bean2/second", xml);
        assertXpathNotExists("/test-bean2/third", xml);
	}
	
	public static Test suite() {
		return new TestSuite(SerializationClassLoaderTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
