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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.nginious.http.HttpMethod;
import com.nginious.http.HttpStatus;
import com.nginious.http.server.HttpTestRequest;
import com.nginious.http.server.HttpTestResponse;

public class ControllerTestCase extends TestCase {
	
    public ControllerTestCase() {
		super();
	}

	public ControllerTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testController() throws Exception {
		TestController controller = new TestController();
		ApplicationClassLoader classLoader = new ApplicationClassLoader(Thread.currentThread().getContextClassLoader());
		ControllerServiceFactory factory = new ControllerServiceFactory(classLoader);
		ControllerService invoker = factory.createControllerService(controller);
		HttpTestRequest request = new HttpTestRequest();
		request.setMethod(HttpMethod.GET);
		request.addHeader("Content-Type", "application/x-www-form-urlencoded");
		request.addHeader("Accept", "application/json");
		request.setParameter("one", "one");
		request.setParameter("two", "2");
		HttpTestResponse response = new HttpTestResponse();
		invoker.invoke(request, response);
		assertEquals(HttpStatus.OK, response.getStatus());
		System.out.println(new String(response.getContent()));
	}
	
	public static Test suite() {
		return new TestSuite(ControllerTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
