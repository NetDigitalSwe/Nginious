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

import com.nginious.http.server.Header;
import com.nginious.http.server.HeaderParameters;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class HeaderTestCase extends TestCase {
	
	public HeaderTestCase() {
		super();
	}

	public HeaderTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testHeader() throws Exception {
		Header header = new Header("Content-Type", "text/html; charset=utf-8");
		assertEquals(header.getName(), "Content-Type");
		assertEquals(header.getValue(), "text/html; charset=utf-8");
		HeaderParameters parameters = header.getParameters();
		assertEquals("Content-Type", header.getName());
		assertEquals(1, parameters.size());
		assertEquals("text/html", parameters.get(0).getName());
		assertEquals("utf-8", parameters.get(0).getSubParameter("charset"));
		
		header = new Header("If-Match", "abc, def, ghi, jkl");
		parameters = header.getParameters();
		assertEquals("If-Match", header.getName());
		assertEquals(4, parameters.size());
		assertEquals("abc", parameters.get(0).getName());
		assertEquals("def", parameters.get(1).getName());
		assertEquals("ghi", parameters.get(2).getName());
		assertEquals("jkl", parameters.get(3).getName());
		
		header = new Header("Accept-Language", "sv-se,sv;q=0.8,en-us;q=0.5,en;q=0.3");
		parameters = header.getParameters();
		assertEquals("Accept-Language", header.getName());
		assertEquals(4, parameters.size());
		assertEquals("sv-se", parameters.get(0).getName());
		assertEquals(1.0d, parameters.get(0).getQuality());
		assertEquals("sv", parameters.get(1).getName());
		assertEquals(0.8d, parameters.get(1).getQuality());
		assertEquals("en-us", parameters.get(2).getName());
		assertEquals(0.5d, parameters.get(2).getQuality());
		assertEquals("en", parameters.get(3).getName());	
		assertEquals(0.3d, parameters.get(3).getQuality());
		
		header = new Header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		parameters = header.getParameters();
		assertEquals("Accept", header.getName());
		assertEquals(4, parameters.size());
		assertEquals("application/xhtml+xml", parameters.get(0).getName());
		assertEquals(1.0d, parameters.get(0).getQuality());
		assertEquals("text/html", parameters.get(1).getName());
		assertEquals(1.0d, parameters.get(1).getQuality());
		assertEquals("application/xml", parameters.get(2).getName());
		assertEquals(0.9d, parameters.get(2).getQuality());
		assertEquals("0.9", parameters.get(2).getSubParameter("q"));
		assertEquals("*/*", parameters.get(3).getName());
		assertEquals(0.8d, parameters.get(3).getQuality());
		assertEquals("0.8", parameters.get(3).getSubParameter("q"));
		assertTrue(header.isAcceptable("text/html"));

		header = new Header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9");
		assertTrue(header.isAcceptable("text/html"));
		assertFalse(header.isAcceptable("image/jpeg"));
		
		// Test multi parameters
		header = new Header("Content-Type", "text/html; bozo; charset=utf-8; test=5");
		assertEquals(header.getName(), "Content-Type");
		assertEquals(header.getValue(), "text/html; bozo; charset=utf-8; test=5");
		parameters = header.getParameters();
		assertEquals(1, parameters.size());
		assertEquals("text/html", parameters.get(0).getName());
		assertEquals("utf-8", parameters.get(0).getSubParameter("charset"));
		assertEquals("5", parameters.get(0).getSubParameter("test"));
		assertEquals(null, parameters.get(0).getSubParameter("bozo"));
		assertEquals("text/html; bozo; charset=utf-8; test=5", parameters.get(0).toString());
	}
	
	public void testAuthorization() throws Exception {
		Header header = new Header("Authorization", "Digest username=\"Mufasa\"," +
                 "realm=\"testrealm@host.com\"," +
                 "nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\"," +
                 "uri=\"/dir/index.html\"," +
                 "qop=auth," +
                 "nc=00000001," +
                 "cnonce=\"0a4f113b\"," +
                 "response=\"6629fae49393a05397450978507c4ef1\"," +
                 "opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"");
		HeaderParameters parameters = header.getParameters();
		
		assertEquals(10, parameters.size());
		assertEquals("Mufasa", parameters.getParameter("username").getValue());
		assertEquals("testrealm@host.com", parameters.getParameter("realm").getValue());
		assertEquals("dcd98b7102dd2f0e8b11d0f600bfb0c093", parameters.getParameter("nonce").getValue());
		assertEquals("/dir/index.html", parameters.getParameter("uri").getValue());
		assertEquals("auth", parameters.getParameter("qop").getValue());
		assertEquals("00000001", parameters.getParameter("nc").getValue());
		assertEquals("0a4f113b", parameters.getParameter("cnonce").getValue());
		assertEquals("6629fae49393a05397450978507c4ef1", parameters.getParameter("response").getValue());
		assertEquals("5ccc069c403ebaf9f0171e9517f40e41", parameters.getParameter("opaque").getValue());
	}
	
	public static Test suite() {
		return new TestSuite(HeaderTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
