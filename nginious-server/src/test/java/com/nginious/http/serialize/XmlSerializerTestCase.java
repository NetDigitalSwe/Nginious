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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.custommonkey.xmlunit.XMLTestCase;

import com.nginious.http.application.ApplicationClassLoader;
import com.nginious.http.serialize.Serializer;
import com.nginious.http.serialize.SerializerFactoryImpl;

public class XmlSerializerTestCase extends XMLTestCase {
	
	public XmlSerializerTestCase() {
		super();
	}

	public XmlSerializerTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		
	}
	
	public void testXmlSerializer() throws Exception {
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
		
		ApplicationClassLoader classLoader = new ApplicationClassLoader(Thread.currentThread().getContextClassLoader());
		SerializerFactoryImpl serializerFactory = new SerializerFactoryImpl(classLoader);
		Serializer<SerializableBean> serializer = serializerFactory.createSerializer(SerializableBean.class, "text/xml, application/json; q=0.9");
		assertEquals("text/xml", serializer.getMimeType());
		
		StringWriter strWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(strWriter);
		serializer.serialize(writer, bean);
		writer.flush();
		String xml = strWriter.toString();
		
		assertXpathExists("/serializable-bean", xml);
        assertXpathEvaluatesTo("true", "/serializable-bean/boolean-value", xml);
        assertXpathEvaluatesTo("0.451", "/serializable-bean/double-value", xml);
        assertXpathEvaluatesTo("1.34", "/serializable-bean/float-value", xml);
        assertXpathEvaluatesTo("3400100", "/serializable-bean/int-value", xml);
        assertXpathEvaluatesTo("3400100200", "/serializable-bean/long-value", xml);
        assertXpathEvaluatesTo("32767", "/serializable-bean/short-value", xml);
        assertXpathEvaluatesTo("String", "/serializable-bean/string-value", xml);
        assertXpathEvaluatesTo("2011-08-24T08:50:23+02:00", "/serializable-bean/date-value", xml);
        assertXpathEvaluatesTo("2011-08-24T08:52:23+02:00", "/serializable-bean/calendar-value", xml);
		
		assertXpathExists("/serializable-bean/bean-value", xml);
		assertXpathExists("/serializable-bean/bean-value/in-bean", xml);
        assertXpathEvaluatesTo("true", "/serializable-bean/bean-value/in-bean/first", xml);
        assertXpathEvaluatesTo("0.567", "/serializable-bean/bean-value/in-bean/second", xml);
        assertXpathEvaluatesTo("0.342", "/serializable-bean/bean-value/in-bean/third", xml);
        assertXpathEvaluatesTo("100", "/serializable-bean/bean-value/in-bean/fourth", xml);
        assertXpathEvaluatesTo("3400200100", "/serializable-bean/bean-value/in-bean/fifth", xml);
        assertXpathEvaluatesTo("32767", "/serializable-bean/bean-value/in-bean/sixth", xml);
        assertXpathEvaluatesTo("String", "/serializable-bean/bean-value/in-bean/seventh", xml);
        assertXpathEvaluatesTo("2011-08-25T08:50:23+02:00", "/serializable-bean/bean-value/in-bean/eight", xml);
        assertXpathEvaluatesTo("2011-08-25T08:52:23+02:00", "/serializable-bean/bean-value/in-bean/ninth", xml);
        
		assertXpathExists("/serializable-bean/string-list-value", xml);
		assertXpathEvaluatesTo("One", "/serializable-bean/string-list-value/value[1]", xml);
		assertXpathEvaluatesTo("Two", "/serializable-bean/string-list-value/value[2]", xml);
		assertXpathEvaluatesTo("Three", "/serializable-bean/string-list-value/value[3]", xml);

		assertXpathExists("/serializable-bean/bean-list-value", xml);
        assertXpathEvaluatesTo("true", "/serializable-bean/bean-list-value/in-bean[1]/first", xml);
        assertXpathEvaluatesTo("0.567", "/serializable-bean/bean-list-value/in-bean[1]/second", xml);
        assertXpathEvaluatesTo("0.342", "/serializable-bean/bean-list-value/in-bean[1]/third", xml);
        assertXpathEvaluatesTo("100", "/serializable-bean/bean-list-value/in-bean[1]/fourth", xml);
        assertXpathEvaluatesTo("3400200100", "/serializable-bean/bean-list-value/in-bean[1]/fifth", xml);
        assertXpathEvaluatesTo("32767", "/serializable-bean/bean-list-value/in-bean[1]/sixth", xml);
        assertXpathEvaluatesTo("String", "/serializable-bean/bean-list-value/in-bean[1]/seventh", xml);
        assertXpathEvaluatesTo("2011-08-25T08:50:23+02:00", "/serializable-bean/bean-list-value/in-bean[1]/eight", xml);
        assertXpathEvaluatesTo("2011-08-25T08:52:23+02:00", "/serializable-bean/bean-list-value/in-bean[1]/ninth", xml);

        assertXpathEvaluatesTo("true", "/serializable-bean/bean-list-value/in-bean[2]/first", xml);
        assertXpathEvaluatesTo("0.567", "/serializable-bean/bean-list-value/in-bean[2]/second", xml);
        assertXpathEvaluatesTo("0.342", "/serializable-bean/bean-list-value/in-bean[2]/third", xml);
        assertXpathEvaluatesTo("100", "/serializable-bean/bean-list-value/in-bean[2]/fourth", xml);
        assertXpathEvaluatesTo("3400200100", "/serializable-bean/bean-list-value/in-bean[2]/fifth", xml);
        assertXpathEvaluatesTo("32767", "/serializable-bean/bean-list-value/in-bean[2]/sixth", xml);
        assertXpathEvaluatesTo("String", "/serializable-bean/bean-list-value/in-bean[2]/seventh", xml);
        assertXpathEvaluatesTo("2011-08-25T08:50:23+02:00", "/serializable-bean/bean-list-value/in-bean[2]/eight", xml);
        assertXpathEvaluatesTo("2011-08-25T08:52:23+02:00", "/serializable-bean/bean-list-value/in-bean[2]/ninth", xml);
	}
	
	public void testNamedXmlSerializer() throws Exception {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
		NamedBean bean = new NamedBean();
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
		
		ApplicationClassLoader classLoader = new ApplicationClassLoader(Thread.currentThread().getContextClassLoader());
		SerializerFactoryImpl serializerFactory = new SerializerFactoryImpl(classLoader);
		Serializer<NamedBean> serializer = serializerFactory.createSerializer(NamedBean.class, "text/xml, application/json; q=0.9");
		assertEquals("text/xml", serializer.getMimeType());
		
		StringWriter strWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(strWriter);
		serializer.serialize(writer, bean);
		writer.flush();
		String xml = strWriter.toString();
		
		System.out.println(xml);
		
		assertXpathExists("/test-named-bean", xml);
        assertXpathEvaluatesTo("true", "/test-named-bean/test-boolean-value", xml);
        assertXpathEvaluatesTo("0.451", "/test-named-bean/test-double-value", xml);
        assertXpathEvaluatesTo("1.34", "/test-named-bean/test-float-value", xml);
        assertXpathEvaluatesTo("3400100", "/test-named-bean/test-int-value", xml);
        assertXpathEvaluatesTo("3400100200", "/test-named-bean/test-long-value", xml);
        assertXpathEvaluatesTo("32767", "/test-named-bean/test-short-value", xml);
        assertXpathEvaluatesTo("String", "/test-named-bean/test-string-value", xml);
        assertXpathEvaluatesTo("2011-08-24T08:50:23+02:00", "/test-named-bean/test-date-value", xml);
        assertXpathEvaluatesTo("2011-08-24T08:52:23+02:00", "/test-named-bean/test-calendar-value", xml);
		
		assertXpathExists("/test-named-bean/test-bean-value", xml);
		assertXpathExists("/test-named-bean/test-bean-value/in-bean", xml);
        assertXpathEvaluatesTo("true", "/test-named-bean/test-bean-value/in-bean/first", xml);
        assertXpathEvaluatesTo("0.567", "/test-named-bean/test-bean-value/in-bean/second", xml);
        assertXpathEvaluatesTo("0.342", "/test-named-bean/test-bean-value/in-bean/third", xml);
        assertXpathEvaluatesTo("100", "/test-named-bean/test-bean-value/in-bean/fourth", xml);
        assertXpathEvaluatesTo("3400200100", "/test-named-bean/test-bean-value/in-bean/fifth", xml);
        assertXpathEvaluatesTo("32767", "/test-named-bean/test-bean-value/in-bean/sixth", xml);
        assertXpathEvaluatesTo("String", "/test-named-bean/test-bean-value/in-bean/seventh", xml);
        assertXpathEvaluatesTo("2011-08-25T08:50:23+02:00", "/test-named-bean/test-bean-value/in-bean/eight", xml);
        assertXpathEvaluatesTo("2011-08-25T08:52:23+02:00", "/test-named-bean/test-bean-value/in-bean/ninth", xml);
        
		assertXpathExists("/test-named-bean/test-string-list-value", xml);
		assertXpathEvaluatesTo("One", "/test-named-bean/test-string-list-value/value[1]", xml);
		assertXpathEvaluatesTo("Two", "/test-named-bean/test-string-list-value/value[2]", xml);
		assertXpathEvaluatesTo("Three", "/test-named-bean/test-string-list-value/value[3]", xml);

		assertXpathExists("/test-named-bean/test-bean-list-value", xml);
        assertXpathEvaluatesTo("true", "/test-named-bean/test-bean-list-value/in-bean[1]/first", xml);
        assertXpathEvaluatesTo("0.567", "/test-named-bean/test-bean-list-value/in-bean[1]/second", xml);
        assertXpathEvaluatesTo("0.342", "/test-named-bean/test-bean-list-value/in-bean[1]/third", xml);
        assertXpathEvaluatesTo("100", "/test-named-bean/test-bean-list-value/in-bean[1]/fourth", xml);
        assertXpathEvaluatesTo("3400200100", "/test-named-bean/test-bean-list-value/in-bean[1]/fifth", xml);
        assertXpathEvaluatesTo("32767", "/test-named-bean/test-bean-list-value/in-bean[1]/sixth", xml);
        assertXpathEvaluatesTo("String", "/test-named-bean/test-bean-list-value/in-bean[1]/seventh", xml);
        assertXpathEvaluatesTo("2011-08-25T08:50:23+02:00", "/test-named-bean/test-bean-list-value/in-bean[1]/eight", xml);
        assertXpathEvaluatesTo("2011-08-25T08:52:23+02:00", "/test-named-bean/test-bean-list-value/in-bean[1]/ninth", xml);

        assertXpathEvaluatesTo("true", "/test-named-bean/test-bean-list-value/in-bean[2]/first", xml);
        assertXpathEvaluatesTo("0.567", "/test-named-bean/test-bean-list-value/in-bean[2]/second", xml);
        assertXpathEvaluatesTo("0.342", "/test-named-bean/test-bean-list-value/in-bean[2]/third", xml);
        assertXpathEvaluatesTo("100", "/test-named-bean/test-bean-list-value/in-bean[2]/fourth", xml);
        assertXpathEvaluatesTo("3400200100", "/test-named-bean/test-bean-list-value/in-bean[2]/fifth", xml);
        assertXpathEvaluatesTo("32767", "/test-named-bean/test-bean-list-value/in-bean[2]/sixth", xml);
        assertXpathEvaluatesTo("String", "/test-named-bean/test-bean-list-value/in-bean[2]/seventh", xml);
        assertXpathEvaluatesTo("2011-08-25T08:50:23+02:00", "/test-named-bean/test-bean-list-value/in-bean[2]/eight", xml);
        assertXpathEvaluatesTo("2011-08-25T08:52:23+02:00", "/test-named-bean/test-bean-list-value/in-bean[2]/ninth", xml);
	}
	
	public void testEmptyXmlSerializer() throws Exception {
		SerializableBean bean = new SerializableBean();
		ApplicationClassLoader classLoader = new ApplicationClassLoader(Thread.currentThread().getContextClassLoader());
		SerializerFactoryImpl serializerFactory = new SerializerFactoryImpl(classLoader);
		Serializer<SerializableBean> serializer = serializerFactory.createSerializer(SerializableBean.class, "text/xml");
		assertEquals("text/xml", serializer.getMimeType());
		
		StringWriter strWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(strWriter);
		serializer.serialize(writer, bean);
		writer.flush();
		String xml = strWriter.toString();
		
		assertXpathExists("/serializable-bean", xml);
        assertXpathEvaluatesTo("false", "/serializable-bean/boolean-value", xml);
        assertXpathEvaluatesTo("0.0", "/serializable-bean/double-value", xml);
        assertXpathEvaluatesTo("0.0", "/serializable-bean/float-value", xml);
        assertXpathEvaluatesTo("0", "/serializable-bean/int-value", xml);
        assertXpathEvaluatesTo("0", "/serializable-bean/long-value", xml);
        assertXpathEvaluatesTo("0", "/serializable-bean/short-value", xml);
        assertXpathNotExists("/serializable-bean/string-value", xml);
        assertXpathNotExists("/serializable-bean/date-value", xml);
        assertXpathNotExists("/serializable-bean/calendar-value", xml);
        assertXpathNotExists("/serializable-bean/bean-value", xml);
        assertXpathNotExists("/serializable-bean/bean-list-value", xml);
        assertXpathNotExists("/serializable-bean/string-list-value", xml);
	}
	
	public static Test suite() {
		return new TestSuite(XmlSerializerTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
