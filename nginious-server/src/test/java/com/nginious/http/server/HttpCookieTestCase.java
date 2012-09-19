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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.nginious.http.HttpCookie;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class HttpCookieTestCase extends TestCase {
	
	public HttpCookieTestCase() {
		super();
	}

	public HttpCookieTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testCookie() throws Exception {
		HttpCookie cookie = new HttpCookie();
		cookie.setName("Test");
		cookie.setValue("test'");
		cookie.setComment("Hello");
		cookie.setVersion(1);
		cookie.setDomain("test.com");
		cookie.setMaxAge(1000);
		cookie.setPath("/test");
		assertEquals("Test", cookie.getName());
		assertEquals("test\'", cookie.getValue());
		assertEquals("Hello", cookie.getComment());
		assertEquals(1, cookie.getVersion());
		assertEquals("test.com", cookie.getDomain());
		assertEquals(1000, cookie.getMaxAge());
		assertEquals("/test", cookie.getPath());
		String header = HttpCookieConverter.format(cookie);
		assertEquals("Test=test';Version=1;Comment=Hello;Path=/test;Domain=test.com;Max-Age=1000", header);
		
		header = "$Version=1; Part_Number=Riding_Rocket_0023; $Path=/acme/ammo; Part_Number=Rocket_Launcher_0001; $Path=/acme";
		HttpCookie[] cookies = HttpCookieConverter.parse(header);
		assertNotNull(cookies);
		assertEquals(2, cookies.length);
		
		assertEquals("Part_Number", cookies[0].getName());
		assertEquals("Riding_Rocket_0023", cookies[0].getValue());
		assertEquals("/acme/ammo", cookies[0].getPath());
		assertEquals(1, cookies[0].getVersion());
		assertNull(cookies[0].getDomain());
		assertEquals(0, cookies[0].getMaxAge());
		
		assertEquals("Part_Number", cookies[1].getName());
		assertEquals("Rocket_Launcher_0001", cookies[1].getValue());
		assertEquals("/acme", cookies[1].getPath());
		assertEquals(1, cookies[1].getVersion());
		assertNull(cookies[1].getDomain());
		assertEquals(0, cookies[1].getMaxAge());
		
		header = HttpCookieConverter.format(cookies[0]);
		assertEquals("Part_Number=Riding_Rocket_0023;Version=1;Path=/acme/ammo;Discard", header);
		header = HttpCookieConverter.format(cookies[1]);
		assertEquals("Part_Number=Rocket_Launcher_0001;Version=1;Path=/acme;Discard", header);
		
		header = "$Version=\"1\"; Part_Number=\"Riding_Rocket_0023\"; $Path=\"/acme/ammo\"; Part_Number=\"Rocket_Launcher_0001\"; $Path=\"/acme\"";
		cookies = HttpCookieConverter.parse(header);
		assertNotNull(cookies);
		assertEquals(2, cookies.length);
		
		// Check cookie 1
		assertEquals("Part_Number", cookies[0].getName());
		assertEquals("Riding_Rocket_0023", cookies[0].getValue());
		assertEquals("/acme/ammo", cookies[0].getPath());
		assertEquals(1, cookies[0].getVersion());
		assertNull(cookies[0].getDomain());
		assertEquals(0, cookies[0].getMaxAge());
		
		// Check cookie 2
		assertEquals("Part_Number", cookies[1].getName());
		assertEquals("Rocket_Launcher_0001", cookies[1].getValue());
		assertEquals("/acme", cookies[1].getPath());
		assertEquals(1, cookies[1].getVersion());
		assertNull(cookies[1].getDomain());
		assertEquals(0, cookies[1].getMaxAge());

		// Check formatting
		header = HttpCookieConverter.format(cookies[0]);
		assertEquals("Part_Number=Riding_Rocket_0023;Version=1;Path=/acme/ammo;Discard", header);
		header = HttpCookieConverter.format(cookies[1]);
		assertEquals("Part_Number=Rocket_Launcher_0001;Version=1;Path=/acme;Discard", header);
		
		header = "\"$Version\"=1; \"Part_Number\"=Riding_Rocket_0023; \"$Path\"=/acme/ammo; \"Part_Number\"=Rocket_Launcher_0001; \"$Path\"=/acme";
		cookies = HttpCookieConverter.parse(header);
		assertNotNull(cookies);
		assertEquals(2, cookies.length);
		
		// Check cookie 1
		assertEquals("Part_Number", cookies[0].getName());
		assertEquals("Riding_Rocket_0023", cookies[0].getValue());
		assertEquals("/acme/ammo", cookies[0].getPath());
		assertEquals(1, cookies[0].getVersion());
		assertNull(cookies[0].getDomain());
		assertEquals(0, cookies[0].getMaxAge());
		
		// Check cookie 2
		assertEquals("Part_Number", cookies[1].getName());
		assertEquals("Rocket_Launcher_0001", cookies[1].getValue());
		assertEquals("/acme", cookies[1].getPath());
		assertEquals(1, cookies[1].getVersion());
		assertNull(cookies[1].getDomain());
		assertEquals(0, cookies[1].getMaxAge());
		
		// Check formatting cookie 1
		cookies[0].setMaxAge(3000);
		SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		String dateStr = format.format(new Date(System.currentTimeMillis() + 3000 * 1000L));
		cookies[0].setVersion(0);
		header = HttpCookieConverter.format(cookies[0]);
		assertEquals("Part_Number=Riding_Rocket_0023;Path=/acme/ammo;Expires=" + dateStr + " GMT", header);
		
		// Check formatting cookie 2
		cookies[1].setMaxAge(4000);
		dateStr = format.format(new Date(System.currentTimeMillis() + 4000 * 1000L));
		cookies[1].setVersion(0);
		header = HttpCookieConverter.format(cookies[1]);
		assertEquals("Part_Number=Rocket_Launcher_0001;Path=/acme;Expires=" + dateStr + " GMT", header);
		
		header = "\"$Version\"=\"1\"; \"Part_Number\"=\"Riding_Rocket_0023\"; \"$Path\"=\"/acme/ammo\"; \"Part_Number\"=\"Rocket_Launcher_0001\"; \"$Path\"=\"/acme\"";
		cookies = HttpCookieConverter.parse(header);
		assertNotNull(cookies);
		assertEquals(2, cookies.length);
		
		// Check cookie 1
		assertEquals("Part_Number", cookies[0].getName());
		assertEquals("Riding_Rocket_0023", cookies[0].getValue());
		assertEquals("/acme/ammo", cookies[0].getPath());
		assertEquals(1, cookies[0].getVersion());
		assertNull(cookies[0].getDomain());
		assertEquals(0, cookies[0].getMaxAge());
		
		// Check cookie 2
		assertEquals("Part_Number", cookies[1].getName());
		assertEquals("Rocket_Launcher_0001", cookies[1].getValue());
		assertEquals("/acme", cookies[1].getPath());
		assertEquals(1, cookies[1].getVersion());
		assertNull(cookies[1].getDomain());
		assertEquals(0, cookies[1].getMaxAge());
		
		// Check formatting
		cookies[0].setMaxAge(1000);
		header = HttpCookieConverter.format(cookies[0]);
		assertEquals("Part_Number=Riding_Rocket_0023;Version=1;Path=/acme/ammo;Max-Age=1000", header);
		cookies[1].setMaxAge(2000);
		header = HttpCookieConverter.format(cookies[1]);
		assertEquals("Part_Number=Rocket_Launcher_0001;Version=1;Path=/acme;Max-Age=2000", header);

		header = "\"$Version\"=\"1\"; \"Part_Number\"=\"Riding_\"Rocket_0023\"; \"$Path\"=\"/acme/ammo\"; \"Part_Number\"=\"Rocket_Launcher_0001\"; \"$Path\"=\"/acme\"";
		cookies = HttpCookieConverter.parse(header);
		assertNotNull(cookies);
		assertEquals(2, cookies.length);
		String header2 = HttpCookieConverter.format(cookies[0]);
		assertEquals("Part_Number=\"Riding_\\\"Rocket_0023\";Version=1;Path=/acme/ammo;Discard", header2);
	}
	
	public static Test suite() {
		return new TestSuite(HttpCookieTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
