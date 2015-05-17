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
import junit.framework.TestSuite;

import org.custommonkey.xmlunit.XMLTestCase;

import com.nginious.http.HttpMethod;
import com.nginious.http.application.ApplicationClassLoader;
import com.nginious.http.server.HttpTestRequest;

public class XmlDeserializerTestCase extends XMLTestCase {
	
	public XmlDeserializerTestCase() {
		super();
	}

	public XmlDeserializerTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		
	}
	
	public void testXmlDeserialization() throws Exception {
		HttpTestRequest request = new HttpTestRequest();
		request.setMethod(HttpMethod.GET);
		request.addHeader("Content-Type", "text/xml");
		
		String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	    content += "<serializable-bean>";
	    content += "<boolean-value>true</boolean-value>";
	    content += "<double-value>0.451</double-value>";
	    content += "<float-value>1.34</float-value>";
	    content += "<int-value>3400100</int-value>";
	    content += "<long-value>3400100200</long-value>";
	    content += "<short-value>32767</short-value>";
	    content += "<string-value>String</string-value>";
	    content += "<date-value>2011-08-24T08:50:23+02:00</date-value>";
	    content += "<calendar-value>2011-08-24T08:52:23+02:00</calendar-value>";
	    content += "</serializable-bean>";
		request.setContent(content.getBytes());
		request.addHeader("Accept", "text/xml");
		
		ApplicationClassLoader classLoader = new ApplicationClassLoader(Thread.currentThread().getContextClassLoader());
		DeserializerFactoryImpl deserializerFactory = new DeserializerFactoryImpl(classLoader);
		Deserializer<SerializableBean> deserializer = deserializerFactory.createDeserializer(SerializableBean.class, "text/xml");
		assertEquals("text/xml", deserializer.getMimeType());
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
	
	public void testXmlArrayDeserialization() throws Exception {
		HttpTestRequest request = new HttpTestRequest();
		request.setMethod(HttpMethod.GET);
		request.addHeader("Content-Type", "text/xml");
		
		String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	    content += "<serializable-array-bean>";
	    content += "<boolean-array-value><value>true</value><value>false</value></boolean-array-value>";
		content += "<double-array-value><value>1.1</value><value>1.2</value></double-array-value>";
		content += "<float-array-value><value>1.12</value><value>1.22</value></float-array-value>";
		content += "<int-array-value><value>2</value><value>3</value></int-array-value>";
		content += "<long-array-value><value>4</value><value>3</value></long-array-value>";
		content += "<short-array-value><value>6</value><value>7</value></short-array-value>";
		content += "<string-array-value><value>One</value><value>Two</value><value>Three</value></string-array-value>";
		content += "</serializable-array-bean>";
		
		request.setContent(content.getBytes());
		request.addHeader("Accept", "text/xml");
		
		ApplicationClassLoader classLoader = new ApplicationClassLoader(Thread.currentThread().getContextClassLoader());
		DeserializerFactoryImpl deserializerFactory = new DeserializerFactoryImpl(classLoader);
		Deserializer<SerializableArrayBean> deserializer = deserializerFactory.createDeserializer(SerializableArrayBean.class, "text/xml");
		assertEquals("text/xml", deserializer.getMimeType());
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
	
	public void testXmlDeserializationBadValues() throws Exception {
		HttpTestRequest request = new HttpTestRequest();
		request.setMethod(HttpMethod.GET);
		request.addHeader("Content-Type", "text/xml");
		
		ApplicationClassLoader classLoader = new ApplicationClassLoader(Thread.currentThread().getContextClassLoader());
		DeserializerFactoryImpl deserializerFactory = new DeserializerFactoryImpl(classLoader);
		Deserializer<SerializableBean> deserializer = deserializerFactory.createDeserializer(SerializableBean.class, "text/xml");
		assertEquals("text/xml", deserializer.getMimeType());
		
		String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	    content += "<serializable-bean>";
	    content += "<boolean-value>true</boolean-value>";
	    content += "<double-value>X</double-value>";
	    content += "<float-value>1.34</float-value>";
	    content += "<int-value>3400100</int-value>";
	    content += "<long-value>3400100200</long-value>";
	    content += "<short-value>32767</short-value>";
	    content += "<string-value>String</string-value>";
	    content += "<date-value>2011-08-24T08:50:23+02:00</date-value>";
	    content += "<calendar-value>2011-08-24T08:52:23+02:00</calendar-value>";
	    content += "</serializable-bean>";
		request.setContent(content.getBytes());
		request.addHeader("Accept", "text/xml");

		try {
			deserializer.deserialize(request);
			fail("Must not be possible to deserialize with bad double value");
		} catch(SerializerException e) {}
		
		content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	    content += "<serializable-bean>";
	    content += "<boolean-value>true</boolean-value>";
	    content += "<double-value>0.451</double-value>";
	    content += "<float-value>X</float-value>";
	    content += "<int-value>3400100</int-value>";
	    content += "<long-value>3400100200</long-value>";
	    content += "<short-value>32767</short-value>";
	    content += "<string-value>String</string-value>";
	    content += "<date-value>2011-08-24T08:50:23+02:00</date-value>";
	    content += "<calendar-value>2011-08-24T08:52:23+02:00</calendar-value>";
	    content += "</serializable-bean>";
		request.setContent(content.getBytes());
		
		try {
			deserializer.deserialize(request);
			fail("Must not be possible to deserialize with bad float value");
		} catch(SerializerException e) {}
		
		content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	    content += "<serializable-bean>";
	    content += "<boolean-value>true</boolean-value>";
	    content += "<double-value>0.451</double-value>";
	    content += "<float-value>1.34</float-value>";
	    content += "<int-value>X</int-value>";
	    content += "<long-value>3400100200</long-value>";
	    content += "<short-value>32767</short-value>";
	    content += "<string-value>String</string-value>";
	    content += "<date-value>2011-08-24T08:50:23+02:00</date-value>";
	    content += "<calendar-value>2011-08-24T08:52:23+02:00</calendar-value>";
	    content += "</serializable-bean>";
		request.setContent(content.getBytes());
		
		try {
			deserializer.deserialize(request);
			fail("Must not be possible to deserialize with bad int value");
		} catch(SerializerException e) {}
		
		content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	    content += "<serializable-bean>";
	    content += "<boolean-value>true</boolean-value>";
	    content += "<double-value>0.451</double-value>";
	    content += "<float-value>1.34</float-value>";
	    content += "<int-value>3400100</int-value>";
	    content += "<long-value>X</long-value>";
	    content += "<short-value>32767</short-value>";
	    content += "<string-value>String</string-value>";
	    content += "<date-value>2011-08-24T08:50:23+02:00</date-value>";
	    content += "<calendar-value>2011-08-24T08:52:23+02:00</calendar-value>";
	    content += "</serializable-bean>";
		request.setContent(content.getBytes());
		
		try {
			deserializer.deserialize(request);
			fail("Must not be possible to deserialize with bad long value");
		} catch(SerializerException e) {}
		
		content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	    content += "<serializable-bean>";
	    content += "<boolean-value>true</boolean-value>";
	    content += "<double-value>0.451</double-value>";
	    content += "<float-value>1.34</float-value>";
	    content += "<int-value>3400100</int-value>";
	    content += "<long-value>3400100200</long-value>";
	    content += "<short-value>65536</short-value>";
	    content += "<string-value>String</string-value>";
	    content += "<date-value>2011-08-24T08:50:23+02:00</date-value>";
	    content += "<calendar-value>2011-08-24T08:52:23+02:00</calendar-value>";
	    content += "</serializable-bean>";
		request.setContent(content.getBytes());

		try {
			deserializer.deserialize(request);
			fail("Must not be possible to deserialize with bad short value");
		} catch(SerializerException e) {}		

		content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	    content += "<serializable-bean>";
	    content += "<boolean-value>true</boolean-value>";
	    content += "<double-value>0.451</double-value>";
	    content += "<float-value>1.34</float-value>";
	    content += "<int-value>3400100</int-value>";
	    content += "<long-value>3400100200</long-value>";
	    content += "<short-value>32767</short-value>";
	    content += "<string-value>String</string-value>";
	    content += "<date-value>X</date-value>";
	    content += "<calendar-value>2011-08-24T08:52:23+02:00</calendar-value>";
	    content += "</serializable-bean>";
		request.setContent(content.getBytes());
		
		try {
			deserializer.deserialize(request);
			fail("Must not be possible to deserialize with bad date value");
		} catch(SerializerException e) {}
		
		content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	    content += "<serializable-bean>";
	    content += "<boolean-value>true</boolean-value>";
	    content += "<double-value>0.451</double-value>";
	    content += "<float-value>1.34</float-value>";
	    content += "<int-value>3400100</int-value>";
	    content += "<long-value>3400100200</long-value>";
	    content += "<short-value>32767</short-value>";
	    content += "<string-value>String</string-value>";
	    content += "<date-value>2011-08-24T08:50:23+02:00</date-value>";
	    content += "<calendar-value>X</calendar-value>";
	    content += "</serializable-bean>";
		request.setContent(content.getBytes());
		
		try {
			deserializer.deserialize(request);
			fail("Must not be possible to deserialize with bad calendar value");
		} catch(SerializerException e) {}
	}
	
	public void testXmlDeserializationNullValues() throws Exception {
		HttpTestRequest request = new HttpTestRequest();
		request.setMethod(HttpMethod.GET);
		request.addHeader("Content-Type", "text/xml");
		request.addHeader("Accept", "text/xml");
		
		ApplicationClassLoader classLoader = new ApplicationClassLoader(Thread.currentThread().getContextClassLoader());
		DeserializerFactoryImpl deserializerFactory = new DeserializerFactoryImpl(classLoader);
		Deserializer<SerializableBean> deserializer = deserializerFactory.createDeserializer(SerializableBean.class, "text/xml");
		SerializableBean bean = deserializer.deserialize(request);
		assertNull(bean);
		
		String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	    content += "<serializable-bean>";
	    content += "</serializable-bean>";
		request.setContent(content.getBytes());
		request.addHeader("Accept", "text/xml");
		request.setContent(content.getBytes());
		
		bean = deserializer.deserialize(request);
		assertNotNull(bean);
		
		assertEquals(false, bean.getBooleanValue());
		assertEquals(Double.NaN, bean.getDoubleValue());
		assertEquals(Float.NaN, bean.getFloatValue());
		assertEquals(0, bean.getIntValue());
		assertEquals(0L, bean.getLongValue());
		assertEquals(0, bean.getShortValue());
		assertNull(bean.getStringValue());
		assertNull(bean.getDateValue());
		assertNull(bean.getCalendarValue());
	}
	
	public void testXmlDeserializationAnnotations() throws Exception {
		HttpTestRequest request = new HttpTestRequest();
		request.setMethod(HttpMethod.GET);
		request.addHeader("Content-Type", "text/xml");

		String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	    content += "<xml-annotated-bean>";
	    content += "<first>one</first>";
	    content += "<second>two</second>";
	    content += "<third>three</third>";
	    content += "</xml-annotated-bean>";
		request.setContent(content.getBytes());
				
		ApplicationClassLoader classLoader = new ApplicationClassLoader(Thread.currentThread().getContextClassLoader());
		DeserializerFactoryImpl deserializerFactory = new DeserializerFactoryImpl(classLoader);
		Deserializer<XmlAnnotatedBean> deserializer = deserializerFactory.createDeserializer(XmlAnnotatedBean.class, "text/xml");
		assertEquals("text/xml", deserializer.getMimeType());
		XmlAnnotatedBean bean = deserializer.deserialize(request);
		
		assertNotNull(bean);
		assertNull(bean.getFirst());
		assertNull(bean.getSecond());
		assertEquals("Value is " + bean.getThird(), "three", bean.getThird());
	}
	
	public void testXmlDeserializationFactory() throws Exception {
		HttpTestRequest request = new HttpTestRequest();
		request.setMethod(HttpMethod.GET);
		request.addHeader("Content-Type", "text/xml");
		
		String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	    content += "<serializable-bean>";
	    content += "<boolean-value>true</boolean-value>";
	    content += "<double-value>0.451</double-value>";
	    content += "<float-value>1.34</float-value>";
	    content += "<int-value>3400100</int-value>";
	    content += "<long-value>3400100200</long-value>";
	    content += "<short-value>32767</short-value>";
	    content += "<string-value>String</string-value>";
	    content += "<date-value>2011-08-24T08:50:23+02:00</date-value>";
	    content += "<calendar-value>2011-08-24T08:52:23+02:00</calendar-value>";
	    content += "</serializable-bean>";
		request.setContent(content.getBytes());
		request.addHeader("Accept", "text/xml");
		
		ApplicationClassLoader classLoader = new ApplicationClassLoader(Thread.currentThread().getContextClassLoader());
		DeserializerFactoryImpl deserializerFactory = new DeserializerFactoryImpl(classLoader);
		Deserializer<InBean> deserializer = deserializerFactory.createDeserializer(InBean.class, "text/xml");
		Deserializer<InBean> deserializer2 = deserializerFactory.createDeserializer(InBean.class, "text/xml");
		assertTrue(deserializer == deserializer2);
		
		deserializer = deserializerFactory.createDeserializer(InBean.class, "nonexistent/contentType");
		assertNull(deserializer);
		
		try {
			deserializerFactory.createDeserializer(NotAnnotatedBean.class, "text/xml");
			fail("Must not be possible to create deserializer for bean not annotated as deserializable");
		} catch(SerializerFactoryException e) {}

		try {
			deserializerFactory.createDeserializer(NotDeserializableBean.class, "text/xml");
			fail("Must not be possible to create deserializer for bean where deserialize = false");
		} catch(SerializerFactoryException e) {}

		try {
			deserializerFactory.createDeserializer(NotXmlBean.class, "text/xml");
			fail("Must not be possible to create deserializer for bean where xml is missing in type annotation");
		} catch(SerializerFactoryException e) {}
	}
	
	private String formatDate(Date value) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
		String formatted = format.format(value);
		formatted = formatted.substring(0, formatted.length() - 2) + ":" + formatted.substring(formatted.length() - 2);
		return formatted;
	}
	
	public static Test suite() {
		return new TestSuite(XmlDeserializerTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
