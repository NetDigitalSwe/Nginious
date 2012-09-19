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

import com.nginious.http.HttpMethod;
import com.nginious.http.server.DigestAuthentication;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class DigestAuthenticationTestCase extends TestCase {
	
	public DigestAuthenticationTestCase() {
		super();
	}

	public DigestAuthenticationTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testChallenge() throws Exception {
		DigestAuthentication auth = new DigestAuthentication();
		String challenge = auth.challenge("admin", "test");
		
		assertNotNull(challenge);
		assertTrue(challenge.indexOf("realm=\"admin\"") > -1);
		assertTrue(challenge.indexOf("domain=\"test\"") > -1);
		assertTrue(challenge.indexOf("algorithm=\"MD5\"") > -1);
		assertTrue(challenge.indexOf("qop=\"auth\"") > -1);
		assertTrue(challenge.indexOf("nonce") > -1);
	}
	
	public void testResponse() throws Exception {
		String credentials = "Digest username=\"admin\", " +
			"realm=\"admin\", " +
			"nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", " +
			"uri=\"/admin\", " +
			"qop=auth, " +
			"nc=00000001, " +
			"cnonce=\"0a4f113b\", " +
			"response=\"fae9315716e12851e61f9608eda5543f\", " +
			"opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"\015\012";
		
		DigestAuthentication auth = new DigestAuthentication();
		boolean result = auth.response(credentials, HttpMethod.GET, "admin");
		assertTrue(result);
		result = auth.response(credentials, HttpMethod.GET, "wrongpwd");
		assertFalse(result);
	}
	
	public void testCasing() throws Exception {
		String credentials = "Digest username=\"admin\", " +
			"realm=\"admin\", " +
			"nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", " +
			"uri=\"/admin\", " +
			"qop=auth, " +
			"nc=00000001, " +
			"cnonce=\"0a4f113b\", " +
			"response=\"FAE9315716E12851E61F9608EDA5543F\", " +
			"opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"\015\012";
		
		DigestAuthentication auth = new DigestAuthentication();
		boolean result = auth.response(credentials, HttpMethod.GET, "admin");
		assertTrue(result);
		result = auth.response(credentials, HttpMethod.GET, "wrongpwd");
		assertFalse(result);		
	}
	
	public void testMissingFields() throws Exception {
		String credentials = "Digest username=\"admin\", " +
			"nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", " +
			"uri=\"/admin\", " +
			"qop=auth, " +
			"nc=00000001, " +
			"cnonce=\"0a4f113b\", " +
			"response=\"FAE9315716E12851E61F9608EDA5543F\", " +
			"opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"\015\012";
		
		DigestAuthentication auth = new DigestAuthentication();
		boolean result = auth.response(credentials, HttpMethod.GET, "admin");
		assertFalse(result);
	}
	
	public static Test suite() {
		return new TestSuite(DigestAuthenticationTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
