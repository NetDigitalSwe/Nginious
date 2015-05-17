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

public class JsonDeserializerTestCase extends TestCase {
	
	public JsonDeserializerTestCase() {
		super();
	}

	public JsonDeserializerTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		
	}
	
	public void testJsonDeserialization() throws Exception {
		HttpTestRequest request = new HttpTestRequest();
		request.setMethod(HttpMethod.GET);
		request.addHeader("Content-Type", "application/json");
		
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
		DeserializerFactoryImpl deserializerFactory = new DeserializerFactoryImpl(classLoader);
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
	
	public void testJsonArrayDeserialization() throws Exception {
		HttpTestRequest request = new HttpTestRequest();
		request.setMethod(HttpMethod.GET);
		request.addHeader("Content-Type", "application/json");
		
		String content = "{\"serializableArrayBean\":";
		content += "{ \"booleanArrayValue\": [ \"true\", \"false\" ],";
		content += "\"doubleArrayValue\": [ 1.1, 1.2 ],";
		content += "\"floatArrayValue\": [ 1.12, 1.22 ],";
		content += "\"intArrayValue\": [ 2, 3 ],";
		content += "\"longArrayValue\": [ 4, 3 ],";
		content += "\"shortArrayValue\": [ 6, 7 ],";
		content += "\"stringArrayValue\": [ \"One\", \"Two\", \"Three\" ] }";
		content += "}";
		request.setContent(content.getBytes());
		request.addHeader("Accept", "text/xml");
		
		ApplicationClassLoader classLoader = new ApplicationClassLoader(Thread.currentThread().getContextClassLoader());
		DeserializerFactoryImpl deserializerFactory = new DeserializerFactoryImpl(classLoader);
		Deserializer<SerializableArrayBean> deserializer = deserializerFactory.createDeserializer(SerializableArrayBean.class, "application/json");
		assertEquals("application/json", deserializer.getMimeType());
		SerializableArrayBean bean = deserializer.deserialize(request);
		
		boolean[] bValues = bean.getBooleanArrayValue();
		assertNotNull(bValues);
		assertEquals(2, bValues.length);
		assertTrue(bValues[0]);
		assertFalse(bValues[1]);
		
		double[] dValues = bean.getDoubleArrayValue();
		assertNotNull(dValues);
		assertEquals(2, dValues.length);
		assertEquals(1.1, dValues[0]);
		assertEquals(1.2, dValues[1]);
		
		float[] fValues = bean.getFloatArrayValue();
		assertNotNull(fValues);
		assertEquals(2, fValues.length);
		assertEquals((float)1.12, fValues[0]);
		assertEquals((float)1.22, fValues[1]);
		
		int[] iValues = bean.getIntArrayValue();
		assertNotNull(iValues);
		assertEquals(2, iValues.length);
		assertEquals(2, iValues[0]);
		assertEquals(3, iValues[1]);
		
		long[] lValues = bean.getLongArrayValue();
		assertNotNull(lValues);
		assertEquals(2, lValues.length);
		assertEquals(4L, lValues[0]);
		assertEquals(3L, lValues[1]);
		
		short[] sValues = bean.getShortArrayValue();
		assertNotNull(sValues);
		assertEquals(2, sValues.length);
		assertEquals((short)6, sValues[0]);
		assertEquals((short)7, sValues[1]);
		
		String[] values = bean.getStringArrayValue();
		assertNotNull(values);
		assertEquals(3, values.length);
		assertEquals("One", values[0]);
		assertEquals("Two", values[1]);
		assertEquals("Three", values[2]);
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
		DeserializerFactoryImpl deserializerFactory = new DeserializerFactoryImpl(classLoader);
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
		DeserializerFactoryImpl deserializerFactory = new DeserializerFactoryImpl(classLoader);
		Deserializer<SerializableBean> deserializer = deserializerFactory.createDeserializer(SerializableBean.class, "text/nonexistent");
		assertNull(deserializer);
	}
	
	public void testJsonDeserializationBadValues() throws Exception {
		HttpTestRequest request = new HttpTestRequest();
		request.setMethod(HttpMethod.GET);
		request.addHeader("Content-Type", "application/json");
		
		ApplicationClassLoader classLoader = new ApplicationClassLoader(Thread.currentThread().getContextClassLoader());
		DeserializerFactoryImpl deserializerFactory = new DeserializerFactoryImpl(classLoader);
		Deserializer<SerializableBean> deserializer = deserializerFactory.createDeserializer(SerializableBean.class, "application/json");
		assertEquals("application/json", deserializer.getMimeType());
		
		String content = "{\"serializableBean\":";
		content += "{\"booleanValue\":true,";
		content += "\"doubleValue\":\"X\",";
		content += "\"floatValue\":1.34,";
		content += "\"intValue\":3400100,";
		content += "\"longValue\":3400100200,";
		content += "\"shortValue\":32767,";
		content += "\"stringValue\":\"String\",";
		content += "\"dateValue\":\"2011-08-24T08:50:23+02:00\",";
		content += "\"calendarValue\":\"2011-08-24T08:52:23+02:00\"}}";
		request.setContent(content.getBytes());
		
		try {
			deserializer.deserialize(request);
			fail("Must not be possible to deserialize with bad double value");
		} catch(SerializerException e) {}
		
		content = "{\"serializableBean\":";
		content += "{\"booleanValue\":true,";
		content += "\"doubleValue\":0.451,";
		content += "\"floatValue\":\"X\",";
		content += "\"intValue\":3400100,";
		content += "\"longValue\":3400100200,";
		content += "\"shortValue\":32767,";
		content += "\"stringValue\":\"String\",";
		content += "\"dateValue\":\"2011-08-24T08:50:23+02:00\",";
		content += "\"calendarValue\":\"2011-08-24T08:52:23+02:00\"}}";
		request.setContent(content.getBytes());
		
		try {
			deserializer.deserialize(request);
			fail("Must not be possible to deserialize with bad float value");
		} catch(SerializerException e) {}
		
		content = "{\"serializableBean\":";
		content += "{\"booleanValue\":true,";
		content += "\"doubleValue\":0.451,";
		content += "\"floatValue\":1.34,";
		content += "\"intValue\":\"X\",";
		content += "\"longValue\":3400100200,";
		content += "\"shortValue\":32767,";
		content += "\"stringValue\":\"String\",";
		content += "\"dateValue\":\"2011-08-24T08:50:23+02:00\",";
		content += "\"calendarValue\":\"2011-08-24T08:52:23+02:00\"}}";
		request.setContent(content.getBytes());
		
		try {
			deserializer.deserialize(request);
			fail("Must not be possible to deserialize with bad int value");
		} catch(SerializerException e) {}
		
		content = "{\"serializableBean\":";
		content += "{\"booleanValue\":true,";
		content += "\"doubleValue\":0.451,";
		content += "\"floatValue\":1.34,";
		content += "\"intValue\":3400100,";
		content += "\"longValue\":\"X\",";
		content += "\"shortValue\":32767,";
		content += "\"stringValue\":\"String\",";
		content += "\"dateValue\":\"2011-08-24T08:50:23+02:00\",";
		content += "\"calendarValue\":\"2011-08-24T08:52:23+02:00\"}}";
		request.setContent(content.getBytes());
		
		try {
			deserializer.deserialize(request);
			fail("Must not be possible to deserialize with bad long value");
		} catch(SerializerException e) {}
		
		content = "{\"serializableBean\":";
		content += "{\"booleanValue\":true,";
		content += "\"doubleValue\":0.451,";
		content += "\"floatValue\":1.34,";
		content += "\"intValue\":3400100,";
		content += "\"longValue\":3400100200,";
		content += "\"shortValue\":\"X\",";
		content += "\"stringValue\":\"String\",";
		content += "\"dateValue\":\"2011-08-24T08:50:23+02:00\",";
		content += "\"calendarValue\":\"2011-08-24T08:52:23+02:00\"}}";
		request.setContent(content.getBytes());
		request.addHeader("Accept", "text/xml");
		
		try {
			deserializer.deserialize(request);
			fail("Must not be possible to deserialize with bad short value");
		} catch(SerializerException e) {}		

		content = "{\"serializableBean\":";
		content += "{\"booleanValue\":true,";
		content += "\"doubleValue\":0.451,";
		content += "\"floatValue\":1.34,";
		content += "\"intValue\":3400100,";
		content += "\"longValue\":3400100200,";
		content += "\"shortValue\":32767,";
		content += "\"stringValue\":\"String\",";
		content += "\"dateValue\":\"X\",";
		content += "\"calendarValue\":\"2011-08-24T08:52:23+02:00\"}}";
		request.setContent(content.getBytes());
		
		try {
			deserializer.deserialize(request);
			fail("Must not be possible to deserialize with bad date value");
		} catch(SerializerException e) {}
		
		content = "{\"serializableBean\":";
		content += "{\"booleanValue\":true,";
		content += "\"doubleValue\":0.451,";
		content += "\"floatValue\":1.34,";
		content += "\"intValue\":3400100,";
		content += "\"longValue\":3400100200,";
		content += "\"shortValue\":32767,";
		content += "\"stringValue\":\"String\",";
		content += "\"dateValue\":\"2011-08-24T08:50:23+02:00\",";
		content += "\"calendarValue\":\"X\"}}";
		request.setContent(content.getBytes());
		
		try {
			deserializer.deserialize(request);
			fail("Must not be possible to deserialize with bad calendar value");
		} catch(SerializerException e) {}
	}
	
	public void testJsonDeserializationNullValues() throws Exception {
		HttpTestRequest request = new HttpTestRequest();
		request.setMethod(HttpMethod.GET);
		request.addHeader("Content-Type", "application/json");
		request.addHeader("Accept", "text/xml");
		
		ApplicationClassLoader classLoader = new ApplicationClassLoader(Thread.currentThread().getContextClassLoader());
		DeserializerFactoryImpl deserializerFactory = new DeserializerFactoryImpl(classLoader);
		Deserializer<SerializableBean> deserializer = deserializerFactory.createDeserializer(SerializableBean.class, "application/json");
		SerializableBean bean = deserializer.deserialize(request);
		assertNull(bean);
		
		String content = "{\"serializableBean\":{}}";
		request.setContent(content.getBytes());
		
		bean = deserializer.deserialize(request);
		assertNotNull(bean);
		
		assertEquals(false, bean.getBooleanValue());
		assertEquals(0.0d, bean.getDoubleValue());
		assertEquals(0.0f, bean.getFloatValue());
		assertEquals(0, bean.getIntValue());
		assertEquals(0L, bean.getLongValue());
		assertEquals(0, bean.getShortValue());
		assertNull(bean.getStringValue());
		assertNull(bean.getDateValue());
		assertNull(bean.getCalendarValue());
	}
	
	public void testJsonDeserializationAnnotations() throws Exception {
		HttpTestRequest request = new HttpTestRequest();
		request.setMethod(HttpMethod.GET);
		request.addHeader("Content-Type", "application/json");

		String content = "{\"jsonAnnotatedBean\":";
		content += "{\"first\":\"one\",";
		content += "\"second\":\"two\",";
		content += "\"third\":\"three\"}}";
		request.setContent(content.getBytes());
		
		ApplicationClassLoader classLoader = new ApplicationClassLoader(Thread.currentThread().getContextClassLoader());
		DeserializerFactoryImpl deserializerFactory = new DeserializerFactoryImpl(classLoader);
		Deserializer<JsonAnnotatedBean> deserializer = deserializerFactory.createDeserializer(JsonAnnotatedBean.class, "application/json");
		assertEquals("application/json", deserializer.getMimeType());
		JsonAnnotatedBean bean = deserializer.deserialize(request);
		
		assertNotNull(bean);
		assertNull(bean.getFirst());
		assertNull(bean.getSecond());
		assertEquals("Value is " + bean.getThird(), "three", bean.getThird());
	}
	
	public void testJsonDeserializationFactory() throws Exception {
		HttpTestRequest request = new HttpTestRequest();
		request.setMethod(HttpMethod.GET);
		request.addHeader("Content-Type", "application/json");
		
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
		DeserializerFactoryImpl deserializerFactory = new DeserializerFactoryImpl(classLoader);
		Deserializer<InBean> deserializer = deserializerFactory.createDeserializer(InBean.class, "application/json");
		Deserializer<InBean> deserializer2 = deserializerFactory.createDeserializer(InBean.class, "application/json");
		assertTrue(deserializer == deserializer2);
		
		deserializer = deserializerFactory.createDeserializer(InBean.class, "nonexistent/contentType");
		assertNull(deserializer);
		
		try {
			deserializerFactory.createDeserializer(NotAnnotatedBean.class, "application/json");
			fail("Must not be possible to create deserializer for bean not annotated as deserializable");
		} catch(SerializerFactoryException e) {}

		try {
			deserializerFactory.createDeserializer(NotDeserializableBean.class, "application/json");
			fail("Must not be possible to create deserializer for bean where deserialize = false");
		} catch(SerializerFactoryException e) {}

		try {
			deserializerFactory.createDeserializer(NotJsonBean.class, "application/json");
			fail("Must not be possible to create deserializer for bean where json is missing in type annotation");
		} catch(SerializerFactoryException e) {}
	}
	
	private String formatDate(Date value) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
		String formatted = format.format(value);
		formatted = formatted.substring(0, formatted.length() - 2) + ":" + formatted.substring(formatted.length() - 2);
		return formatted;
	}
	
	public static Test suite() {
		return new TestSuite(JsonDeserializerTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
