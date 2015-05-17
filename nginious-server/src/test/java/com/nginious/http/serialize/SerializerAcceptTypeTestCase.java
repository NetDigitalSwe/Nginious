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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.nginious.http.application.ApplicationClassLoader;

public class SerializerAcceptTypeTestCase extends TestCase {
	
	public SerializerAcceptTypeTestCase() {
		super();
	}

	public SerializerAcceptTypeTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		
	}
	
	public void testSerializerAcceptType() throws Exception {
		ApplicationClassLoader classLoader = new ApplicationClassLoader(Thread.currentThread().getContextClassLoader());
		
		SerializerFactoryImpl serializerFactory = new SerializerFactoryImpl(classLoader);
		Serializer<SerializableBean> serializer = serializerFactory.createSerializer(SerializableBean.class, null);
		assertEquals("application/json", serializer.getMimeType());

		serializer = serializerFactory.createSerializer(SerializableBean.class, "application/json");
		assertEquals("application/json", serializer.getMimeType());

		serializer = serializerFactory.createSerializer(SerializableBean.class, "text/xml");
		assertEquals("text/xml", serializer.getMimeType());

		serializer = serializerFactory.createSerializer(SerializableBean.class, "application/json; q=0.9, text/xml");
		assertEquals("text/xml", serializer.getMimeType());
	}
	
	public static Test suite() {
		return new TestSuite(SerializerAcceptTypeTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
