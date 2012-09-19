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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.custommonkey.xmlunit.XMLTestCase;

import com.nginious.http.HttpException;
import com.nginious.http.HttpMethod;
import com.nginious.http.HttpStatus;
import com.nginious.http.server.HttpTestRequest;
import com.nginious.http.server.HttpTestResponse;

public class RestServiceSerializationTestCase extends XMLTestCase {
	
	public RestServiceSerializationTestCase() {
		super();
	}

	public RestServiceSerializationTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		
	}
	
	public void testRestServiceSerialization() throws Exception {
		HttpTestRequest request = new HttpTestRequest();
		request.setMethod(HttpMethod.GET);
		request.addHeader("Content-Type", "application/x-www-form-urlencoded");
		request.addHeader("Accept", "text/xml");
		request.addParameter("first", "true");
		request.addParameter("second", "0.567");
		request.addParameter("third", "0.452");
		request.addParameter("fourth", "10");
		request.addParameter("fifth", "3400000000");
		request.addParameter("sixth", "32767");
		request.addParameter("seventh", "String");
		request.addParameter("eight", "2011-08-24T08:50:23+02:00");
		request.addParameter("ninth", "2011-08-24T08:52:23+02:00");
		
		HttpTestResponse response = new HttpTestResponse();
		TestService service = new TestService();
		InvokeRestService invoke = new InvokeRestService(service);
		invoke.invoke(request, response);
		
		assertEquals(HttpStatus.OK, response.getStatus());
		byte[] content = response.getContent();
		assertNotNull(content);
		String xml = new String(content);

		assertXpathExists("/out-bean", xml);
        assertXpathEvaluatesTo("true", "/out-bean/first", xml);
        assertXpathEvaluatesTo("0.567", "/out-bean/second", xml);
        assertXpathEvaluatesTo("0.452", "/out-bean/third", xml);
        assertXpathEvaluatesTo("10", "/out-bean/fourth", xml);
        assertXpathEvaluatesTo("3400000000", "/out-bean/fifth", xml);
        assertXpathEvaluatesTo("32767", "/out-bean/sixth", xml);
        assertXpathEvaluatesTo("String", "/out-bean/seventh", xml);
        assertXpathEvaluatesTo("2011-08-24T08:50:23+02:00", "/out-bean/eight", xml);
        assertXpathEvaluatesTo("2011-08-24T08:52:23+02:00", "/out-bean/ninth", xml);
	}
	
	public void testMissingContentTypeSerialization() throws Exception {
		HttpTestRequest request = new HttpTestRequest();
		request.setMethod(HttpMethod.POST);
		request.addHeader("Accept", "text/xml");
		request.addParameter("first", "true");
		request.addParameter("second", "0.567");
		request.addParameter("third", "0.452");
		request.addParameter("fourth", "10");
		request.addParameter("fifth", "3400000000");
		request.addParameter("sixth", "32767");
		request.addParameter("seventh", "String");
		request.addParameter("eight", "2011-08-24T08:50:23+02:00");
		request.addParameter("ninth", "2011-08-24T08:52:23+02:00");
		
		HttpTestResponse response = new HttpTestResponse();
		TestService service = new TestService();
		InvokeRestService invoke = new InvokeRestService(service);
		
		try {
			invoke.invoke(request, response);
			fail("Must not be possible to post data with missing content type");
		} catch(HttpException e) {
			assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
		}
	}
	
	public void testInvalidContentTypeSerialization() throws Exception {
		HttpTestRequest request = new HttpTestRequest();
		request.setMethod(HttpMethod.POST);
		request.addHeader("Content-Type", "text/nonexistent");
		
		request.addHeader("Accept", "text/xml");
		request.addParameter("first", "true");
		request.addParameter("second", "0.567");
		request.addParameter("third", "0.452");
		request.addParameter("fourth", "10");
		request.addParameter("fifth", "3400000000");
		request.addParameter("sixth", "32767");
		request.addParameter("seventh", "String");
		request.addParameter("eight", "2011-08-24T08:50:23+02:00");
		request.addParameter("ninth", "2011-08-24T08:52:23+02:00");
		
		HttpTestResponse response = new HttpTestResponse();
		TestService service = new TestService();
		InvokeRestService invoke = new InvokeRestService(service);
		
		try {
			invoke.invoke(request, response);
			fail("Must not be possible to post data with invalid content type");
		} catch(HttpException e) {
			assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
		}
	}
	
	public void testMissingAcceptSerialization() throws Exception {
		HttpTestRequest request = new HttpTestRequest();
		request.setMethod(HttpMethod.GET);
		request.addHeader("Content-Type", "application/x-www-form-urlencoded");
		request.addParameter("first", "true");
		request.addParameter("second", "0.567");
		request.addParameter("third", "0.452");
		request.addParameter("fourth", "10");
		request.addParameter("fifth", "3400000000");
		request.addParameter("sixth", "32767");
		request.addParameter("seventh", "String");
		request.addParameter("eight", "2011-08-24T08:50:23+02:00");
		request.addParameter("ninth", "2011-08-24T08:52:23+02:00");
		
		HttpTestResponse response = new HttpTestResponse();
		TestService service = new TestService();
		InvokeRestService invoke = new InvokeRestService(service);
		invoke.invoke(request, response);
		
		assertEquals(HttpStatus.OK, response.getStatus());
		assertEquals("application/json", response.getContentType());
	}
	
	public void testInvalidAcceptSerialization() throws Exception {
		HttpTestRequest request = new HttpTestRequest();
		request.setMethod(HttpMethod.GET);
		request.addHeader("Content-Type", "application/x-www-form-urlencoded");
		request.addHeader("Accept", "text/nonexistent");
		request.addParameter("first", "true");
		request.addParameter("second", "0.567");
		request.addParameter("third", "0.452");
		request.addParameter("fourth", "10");
		request.addParameter("fifth", "3400000000");
		request.addParameter("sixth", "32767");
		request.addParameter("seventh", "String");
		request.addParameter("eight", "2011-08-24T08:50:23+02:00");
		request.addParameter("ninth", "2011-08-24T08:52:23+02:00");
		
		HttpTestResponse response = new HttpTestResponse();
		TestService service = new TestService();
		InvokeRestService invoke = new InvokeRestService(service);
		invoke.invoke(request, response);
		
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
	}
	
	public void testVoidServiceSerialization() throws Exception {
		HttpTestRequest request = new HttpTestRequest();
		request.setMethod(HttpMethod.GET);
		request.addHeader("Content-Type", "application/x-www-form-urlencoded");
		request.addHeader("Accept", "text/xml");
		request.addParameter("first", "true");
		request.addParameter("second", "0.567");
		request.addParameter("third", "0.452");
		request.addParameter("fourth", "10");
		request.addParameter("fifth", "3400000000");
		request.addParameter("sixth", "32767");
		request.addParameter("seventh", "String");
		request.addParameter("eight", "2011-08-24T08:50:23+02:00");
		request.addParameter("ninth", "2011-08-24T08:52:23+02:00");
		
		HttpTestResponse response = new HttpTestResponse();
		TestVoidService service = new TestVoidService();
		InvokeRestService invoke = new InvokeRestService(service);
		invoke.invoke(request, response);
		
		assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
		byte[] content = response.getContent();
		assertNull(content);
	}
	
	public static Test suite() {
		return new TestSuite(RestServiceSerializationTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
