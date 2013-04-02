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

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.nginious.http.HttpMethod;
import com.nginious.http.application.ApplicationClassLoader;
import com.nginious.http.server.HttpTestRequest;

public class DeserializerTestCase extends TestCase {
	
	public DeserializerTestCase() {
		super();
	}

	public DeserializerTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		
	}
	
	public void testMissingContentTypeDeserialization() throws Exception {
		HttpTestRequest request = new HttpTestRequest();
		request.setMethod(HttpMethod.GET);
		
		String content = "{\"serializableBean\":";
		content += "{\"booleanValue\":true,";
		content += "\"doubleValue\":0.451,";
		content += "\"floatValue\":1.34,";
		content += "\"intValue\":3400100,";
		content += "\"longValue\":3400100200,";
		content += "\"shortValue\":32767,";
		content += "\"stringValue\":\"String\",";
		content += "\"dateValue\":\"2011-08-24T08:50:23+02:00\",";
		content += "\"calendarValue\":\"2011-08-24T08:52:23+02:00\"}}";
		request.setContent(content.getBytes());
		request.addHeader("Accept", "text/xml");
		
		ApplicationClassLoader classLoader = new ApplicationClassLoader(Thread.currentThread().getContextClassLoader());
		DeserializerFactory deserializerFactory = new DeserializerFactory(classLoader);
		Deserializer<SerializableBean> deserializer = deserializerFactory.createDeserializer(SerializableBean.class, "application/json");
		assertEquals("application/json", deserializer.getMimeType());
		SerializableBean bean = deserializer.deserialize(request);
		
		assertEquals(true, bean.getBooleanValue());
		assertEquals(0.451, bean.getDoubleValue());
		assertEquals(1.34f, bean.getFloatValue());
		assertEquals(3400100, bean.getIntValue());
		assertEquals(3400100200L, bean.getLongValue());
		assertEquals(32767, bean.getShortValue());
		assertEquals("String", bean.getStringValue());
		assertEquals("2011-08-24T08:50:23+02:00", formatDate(bean.getDateValue()));
		assertEquals("2011-08-24T08:52:23+02:00", formatDate(bean.getCalendarValue().getTime()));
	}
	
	public void testInvalidContentTypeDeserialization() throws Exception {
		HttpTestRequest request = new HttpTestRequest();
		request.setMethod(HttpMethod.GET);
		request.addHeader("Content-Type", "text/nonexistent");
		
		String content = "{\"serializableBean\":";
		content += "{\"booleanValue\":true,";
		content += "\"doubleValue\":0.451,";
		content += "\"floatValue\":1.34,";
		content += "\"intValue\":3400100,";
		content += "\"longValue\":3400100200,";
		content += "\"shortValue\":32767,";
		content += "\"stringValue\":\"String\",";
		content += "\"dateValue\":\"2011-08-24T08:50:23+02:00\",";
		content += "\"calendarValue\":\"2011-08-24T08:52:23+02:00\"}}";
		request.setContent(content.getBytes());
		request.addHeader("Accept", "text/xml");
		
		ApplicationClassLoader classLoader = new ApplicationClassLoader(Thread.currentThread().getContextClassLoader());
		DeserializerFactory deserializerFactory = new DeserializerFactory(classLoader);
		Deserializer<SerializableBean> deserializer = deserializerFactory.createDeserializer(SerializableBean.class, "text/nonexistent");
		assertNull(deserializer);
	}
	
	private String formatDate(Date value) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
		String formatted = format.format(value);
		formatted = formatted.substring(0, formatted.length() - 2) + ":" + formatted.substring(formatted.length() - 2);
		return formatted;
	}
	
	public static Test suite() {
		return new TestSuite(DeserializerTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
