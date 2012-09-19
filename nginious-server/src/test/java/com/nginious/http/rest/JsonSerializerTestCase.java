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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.json.JSONArray;
import org.json.JSONObject;

import com.nginious.http.rest.Serializer;
import com.nginious.http.rest.SerializerFactory;

public class JsonSerializerTestCase extends TestCase {
	
	public JsonSerializerTestCase() {
		super();
	}

	public JsonSerializerTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		
	}
	
	public void testJsonSerializer() throws Exception {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
		SerializableBean bean = new SerializableBean();
		bean.setBooleanValue(true);
		bean.setDoubleValue(0.451);
		bean.setFloatValue(1.34f);
		bean.setIntValue(3400100);
		bean.setLongValue(3400100200L);
		bean.setShortValue((short)32767);
		bean.setStringValue("String");		
		bean.setDateValue(format.parse("2011-08-24T08:50:23+0200"));
		Date calDate = format.parse("2011-08-24T08:52:23+0200");
		Calendar cal = Calendar.getInstance();
		cal.setTime(calDate);
		bean.setCalendarValue(cal);
		bean.setObjectValue(TimeZone.getDefault());
		
		InBean inBean = new InBean();
		inBean.setFirst(true);
		inBean.setSecond(0.567d);
		inBean.setThird(0.342f);
		inBean.setFourth(100);
		inBean.setFifth(3400200100L);
		inBean.setSixth((short)32767);
		inBean.setSeventh("String");
		inBean.setEight(format.parse("2011-08-25T08:50:23+0200"));
		cal = Calendar.getInstance();
		calDate = format.parse("2011-08-25T08:52:23+0200");
		cal.setTime(calDate);
		inBean.setNinth(cal);
		bean.setBeanValue(inBean);
		
		List<InBean> beanList = new ArrayList<InBean>();
		beanList.add(inBean);
		beanList.add(inBean);
		bean.setBeanListValue(beanList);
		
		List<String> stringList = new ArrayList<String>();
		stringList.add("One");
		stringList.add("Two");
		stringList.add("Three");
		bean.setStringListValue(stringList);
		
		Serializer<SerializableBean> serializer = SerializerFactory.getInstance().createSerializer(SerializableBean.class, "application/json");
		assertEquals("application/json", serializer.getMimeType());
		
		StringWriter strWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(strWriter);
		
		serializer.serialize(writer, bean);
		writer.flush();
		JSONObject json = new JSONObject(strWriter.toString());
		assertNotNull(json);
		assertTrue(json.has("serializableBean"));
		json = json.getJSONObject("serializableBean");
		
		assertEquals(true, json.getBoolean("booleanValue"));
		assertEquals(0.451, json.getDouble("doubleValue"));
		assertEquals(1.34f, (float)json.getDouble("floatValue"));
		assertEquals(3400100, json.getInt("intValue"));
		assertEquals(3400100200L, json.getLong("longValue"));
		assertEquals(32767, (short)json.getInt("shortValue"));
		assertEquals("String", json.getString("stringValue"));
		assertEquals("2011-08-24T08:50:23+02:00", json.getString("dateValue"));
		assertEquals("2011-08-24T08:52:23+02:00", json.getString("calendarValue"));
		assertEquals(TimeZone.getDefault().toString(), json.getString("objectValue"));

		assertTrue(json.has("beanValue"));
		JSONObject inBeanJson = json.getJSONObject("beanValue");
		assertTrue(inBeanJson.has("inBean"));
		inBeanJson = inBeanJson.getJSONObject("inBean");
		assertEquals(true, inBeanJson.getBoolean("first"));
		assertEquals(0.567, inBeanJson.getDouble("second"));
		assertEquals(0.342f, (float)inBeanJson.getDouble("third"));
		assertEquals(100, inBeanJson.getInt("fourth"));
		assertEquals(3400200100L, inBeanJson.getLong("fifth"));
		assertEquals(32767, (short)inBeanJson.getInt("sixth"));
		assertEquals("String", inBeanJson.getString("seventh"));
		assertEquals("2011-08-25T08:50:23+02:00", inBeanJson.getString("eight"));
		assertEquals("2011-08-25T08:52:23+02:00", inBeanJson.getString("ninth"));

		assertTrue(json.has("stringListValue"));
		JSONArray stringListJson = json.getJSONArray("stringListValue");
		assertEquals("One", stringListJson.get(0));
		assertEquals("Two", stringListJson.get(1));
		assertEquals("Three", stringListJson.get(2));
		
		assertTrue(json.has("beanListValue"));
		JSONArray beanListJson = json.getJSONArray("beanListValue");
		
		inBeanJson = beanListJson.getJSONObject(0);
		assertTrue(inBeanJson.has("inBean"));
		inBeanJson = inBeanJson.getJSONObject("inBean");
		assertEquals(true, inBeanJson.getBoolean("first"));
		assertEquals(0.567, inBeanJson.getDouble("second"));
		assertEquals(0.342f, (float)inBeanJson.getDouble("third"));
		assertEquals(100, inBeanJson.getInt("fourth"));
		assertEquals(3400200100L, inBeanJson.getLong("fifth"));
		assertEquals(32767, (short)inBeanJson.getInt("sixth"));
		assertEquals("String", inBeanJson.getString("seventh"));
		assertEquals("2011-08-25T08:50:23+02:00", inBeanJson.getString("eight"));
		assertEquals("2011-08-25T08:52:23+02:00", inBeanJson.getString("ninth"));

		inBeanJson = beanListJson.getJSONObject(1);
		assertTrue(inBeanJson.has("inBean"));
		inBeanJson = inBeanJson.getJSONObject("inBean");
		assertEquals(true, inBeanJson.getBoolean("first"));
		assertEquals(0.567, inBeanJson.getDouble("second"));
		assertEquals(0.342f, (float)inBeanJson.getDouble("third"));
		assertEquals(100, inBeanJson.getInt("fourth"));
		assertEquals(3400200100L, inBeanJson.getLong("fifth"));
		assertEquals(32767, (short)inBeanJson.getInt("sixth"));
		assertEquals("String", inBeanJson.getString("seventh"));
		assertEquals("2011-08-25T08:50:23+02:00", inBeanJson.getString("eight"));
		assertEquals("2011-08-25T08:52:23+02:00", inBeanJson.getString("ninth"));
	}
	
	public void testEmptyJsonSerializer() throws Exception {
		SerializableBean bean = new SerializableBean();
		Serializer<SerializableBean> serializer = SerializerFactory.getInstance().createSerializer(SerializableBean.class, "application/json");
		assertEquals("application/json", serializer.getMimeType());
		
		StringWriter strWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(strWriter);
		serializer.serialize(writer, bean);
		writer.flush();
		JSONObject json = new JSONObject(strWriter.toString());
		assertNotNull(json);
		assertTrue(json.has("serializableBean"));
		json = json.getJSONObject("serializableBean");
		
		assertEquals(false, json.getBoolean("booleanValue"));
		assertEquals(0.0d, json.getDouble("doubleValue"));
		assertEquals(0.0f, (float)json.getDouble("floatValue"));
		assertEquals(0, json.getInt("intValue"));
		assertEquals(0L, json.getLong("longValue"));
		assertEquals(0, (short)json.getInt("shortValue"));
		assertFalse(json.has("stringValue"));
		assertFalse(json.has("dateValue"));
		assertFalse(json.has("calendarValue"));
		assertFalse(json.has("objectValue"));
	}
	
	public static Test suite() {
		return new TestSuite(JsonSerializerTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
