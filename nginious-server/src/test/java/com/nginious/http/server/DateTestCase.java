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

package com.nginious.http.server;

import java.util.Date;

import com.nginious.http.server.Header;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class DateTestCase extends TestCase {
	
	public DateTestCase() {
		super();
	}

	public DateTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testDate() throws Exception {
		Date date = Header.parseDate("Sun, 06 Nov 1994 08:49:37 GMT");
		assertNotNull(date);

		date = Header.parseDate("Sunday, 06-Nov-94 08:49:37 GMT");
		assertNotNull(date);

		date = Header.parseDate("Sun Nov  6 08:49:37 1994");
		assertNotNull(date);
	}
	
	public static Test suite() {
		return new TestSuite(DateTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
