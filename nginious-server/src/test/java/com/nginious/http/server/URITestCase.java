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

import java.util.HashMap;
import java.util.List;

import com.nginious.http.server.URI;
import com.nginious.http.server.URIException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class URITestCase extends TestCase {
	
	public URITestCase() {
		super();
	}

	public URITestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
	}
	
	public void testURI() throws Exception {
		URI uri = new URI("http://testuser@www.test.com:8080/test/test2?test1=1&test2=2#test");
		uri.parse();
		assertFalse(uri.isWildcard());
		assertEquals(uri.getScheme(), "http");
		assertEquals(uri.getUserinfo(), "testuser");
		assertEquals(uri.getHost(), "www.test.com");
		assertEquals(uri.getPort(), 8080);
		assertEquals(uri.getPath(), "/test/test2");
		assertEquals(uri.getQuery(), "test1=1&test2=2");
		
		HashMap<String, List<String>> params = new HashMap<String, List<String>>();
		uri.decodeQuery(params, "utf-8");		
		assertEquals(params.get("test1").get(0), "1");
		assertEquals(params.get("test2").get(0), "2");
		
		assertEquals(uri.getFragment(), "test");
		
		// Test wildcard URI
		uri = new URI("*");
		uri.parse();
		assertTrue(uri.isWildcard());
		assertNull(uri.getScheme());
		assertNull(uri.getUserinfo());
		assertNull(uri.getHost());
		assertEquals(uri.getPort(), 0);
		assertNull(uri.getPath());
		assertNull(uri.getQuery());
		
		// Test random text
		try {
			uri = new URI("xyz");
			uri.parse();
			fail("Must not be possible to create URI witn invalid scheme only");
		} catch(URIException e) {
			// Should get here
		}
		
		// Test URI with scheme only
		try {
			uri = new URI("http://");
			uri.parse();
			fail("Must not be possible to create URI with only scheme");
		} catch(URIException e) {
			// Should get here
		}
		
		// Test URI with scheme and userinfo
		try {
			uri = new URI("http://testuser@");
			uri.parse();
			fail("Must not be possible to create URI with scheme and userinfo only");
		} catch(URIException e) {
			// Should get here
		}
		
		// Test URI with scheme and host
		uri = new URI("http://www.test.com");
		uri.parse();
		assertEquals(uri.getScheme(), "http");
		assertEquals(uri.getHost(), "www.test.com");
		
		// Test URI with scheme, host and port
		uri = new URI("http://www.test.com:8080");
		uri.parse();
		assertEquals(uri.getScheme(), "http");
		assertEquals(uri.getHost(), "www.test.com");
		
		// Test URI with bad port
		try {
			uri = new URI("http://www.test.com:80d4");
			uri.parse();
			fail("Must not be possible to create URI with non numeric chars in port");
		} catch(URIException e) {
			// Should get here
		}
		
		// Test URI with iso-8859-1 pct encoded query value
		uri = new URI("http://testuser@www.test.com:8080/test/test2?test1=1%202&test2=2%e5#test");
		uri.parse();
		assertEquals(uri.getScheme(), "http");
		assertEquals(uri.getUserinfo(), "testuser");
		assertEquals(uri.getHost(), "www.test.com");
		assertEquals(uri.getPort(), 8080);
		assertEquals(uri.getPath(), "/test/test2");
		assertEquals(uri.getQuery(), "test1=1%202&test2=2%e5");
		
		params = new HashMap<String, List<String>>();
		uri.decodeQuery(params, "iso-8859-1");		
		assertEquals(params.get("test1").get(0), "1 2");
		assertEquals(params.get("test2").get(0), "2å");
		
		// Test URI with default port, path and query
		uri = new URI("http://testuser@www.test.com/test/test2?test1=1");
		uri.parse();
		assertEquals(uri.getScheme(), "http");
		assertEquals(uri.getUserinfo(), "testuser");
		assertEquals(uri.getHost(), "www.test.com");
		assertEquals(uri.getPort(), 0);
		assertEquals(uri.getPath(), "/test/test2");
		assertEquals(uri.getQuery(), "test1=1");
		
		// Test URI with default port and fragment
		uri = new URI("http://testuser@www.test.com#testfragment");
		uri.parse();
		assertEquals(uri.getScheme(), "http");
		assertEquals(uri.getUserinfo(), "testuser");
		assertEquals(uri.getHost(), "www.test.com");
		assertEquals(uri.getPort(), 0);
		assertNull(uri.getPath());
		assertNull(uri.getQuery());
		assertEquals("testfragment", uri.getFragment());
		
		// Test URI with utf-8 pct encoded query value
		uri = new URI("http://testuser@www.test.com:8080/test/test2?test1=1%202&test2=2%c3%a5#test");
		uri.parse();
		assertEquals(uri.getScheme(), "http");
		assertEquals(uri.getUserinfo(), "testuser");
		assertEquals(uri.getHost(), "www.test.com");
		assertEquals(uri.getPort(), 8080);
		assertEquals(uri.getPath(), "/test/test2");
		assertEquals(uri.getQuery(), "test1=1%202&test2=2%c3%a5");
		
		params = new HashMap<String, List<String>>();
		uri.decodeQuery(params, "utf-8");		
		assertEquals(params.get("test1").get(0), "1 2");
		assertEquals(params.get("test2").get(0), "2å");
		
		// Test mailto scheme
		uri = new URI("mailto:bojan@netdigital.se");
		uri.parse();
		assertEquals(uri.getScheme(), "mailto");
		assertEquals(uri.getUserinfo(), "bojan");
		assertEquals(uri.getHost(), "netdigital.se");
		
		// Test start with path
		uri = new URI("/test/test2?test1=1&test2=2#test");
		uri.parse();
		assertNull(uri.getScheme());
		assertNull(uri.getUserinfo());
		assertNull(uri.getHost());
		assertEquals(uri.getPort(), 0);
		assertEquals(uri.getPath(), "/test/test2");
		assertEquals(uri.getQuery(), "test1=1&test2=2");
		
		params = new HashMap<String, List<String>>();
		uri.decodeQuery(params, "utf-8");		
		assertEquals(params.get("test1").get(0), "1");
		assertEquals(params.get("test2").get(0), "2");
		
		assertEquals(uri.getFragment(), "test");
		
		// Test parameters only
		uri = new URI("?test1=1&test2=2");
		uri.parse();
		assertNull(uri.getScheme());
		assertNull(uri.getUserinfo());
		assertNull(uri.getHost());
		assertEquals(uri.getPort(), 0);
		assertNull(uri.getPath());
		assertEquals(uri.getQuery(), "test1=1&test2=2");
		
		params = new HashMap<String, List<String>>();
		uri.decodeQuery(params, "utf-8");		
		assertEquals(params.get("test1").get(0), "1");
		assertEquals(params.get("test2").get(0), "2");

		// Test absolute path and fragment only
		uri = new URI("/testpath#testfragment");
		uri.parse();
		assertNull(uri.getScheme());
		assertNull(uri.getUserinfo());
		assertNull(uri.getHost());
		assertEquals(uri.getPort(), 0);
		assertEquals("/testpath", uri.getPath());
		assertNull(uri.getQuery());
		assertEquals("testfragment", uri.getFragment());

		// Test relative path and fragment only
		uri = new URI("testpath#testfragment");
		uri.parse();
		assertNull(uri.getScheme());
		assertNull(uri.getUserinfo());
		assertNull(uri.getHost());
		assertEquals(uri.getPort(), 0);
		assertEquals("testpath", uri.getPath());
		assertNull(uri.getQuery());
		assertEquals("testfragment", uri.getFragment());

		// Test relative path and fragment only
		uri = new URI("testpath?test=1");
		uri.parse();
		assertNull(uri.getScheme());
		assertNull(uri.getUserinfo());
		assertNull(uri.getHost());
		assertEquals(uri.getPort(), 0);
		assertEquals("testpath", uri.getPath());
		assertEquals(uri.getQuery(), "test=1");
		assertNull(uri.getFragment());
	}
	
	public static Test suite() {
		return new TestSuite(URITestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
