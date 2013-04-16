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
import junit.framework.TestSuite;

import org.custommonkey.xmlunit.XMLTestCase;

import com.nginious.http.server.HttpServer;
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;


public class NullWebAppsDirTestCase extends XMLTestCase {
	
	public NullWebAppsDirTestCase() {
		super();
	}

	public NullWebAppsDirTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testNullWebAppsDir() throws Exception {
		HttpServer server = null;
		
		HttpServerConfiguration config = new HttpServerConfiguration();
		config.setAdminPwd("admin");
		config.setWebappsDir(null);
		config.setServerLogPath("build/test-server.log");
		config.setAccessLogPath("build/test-access.log");
		config.setPort(9000);
		HttpServerFactory factory = HttpServerFactory.getInstance();
		server = factory.create(config);
		server.start();
		
		Thread.sleep(2000L);
		
		server.stop();
	}
	
	public static Test suite() {
		return new TestSuite(NullWebAppsDirTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
