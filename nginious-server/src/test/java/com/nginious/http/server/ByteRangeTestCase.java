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

import com.nginious.http.server.ByteRange;
import com.nginious.http.server.Header;
import com.nginious.http.server.HeaderException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ByteRangeTestCase extends TestCase {
	
	public ByteRangeTestCase() {
		super();
	}

	public ByteRangeTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testByteRange() throws Exception {
		Header header = new Header("Range", "10-19");
		ByteRange[] ranges = header.createByteRanges(20);
		assertEquals(1, ranges.length);
		assertEquals(ranges[0].getStart(), 10);
		assertEquals(ranges[0].getEnd(), 19);
		
		header = new Header("Range", "10-");
		ranges = header.createByteRanges(20);
		assertEquals(1, ranges.length);
		assertEquals(ranges[0].toString(), "10-");
		assertEquals(ranges[0].getStart(), 10);
		assertEquals(ranges[0].getEnd(), 19);
		assertTrue(ranges[0].includes(19));
		assertTrue(ranges[0].includes(10));
		assertFalse(ranges[0].includes(9));
		assertFalse(ranges[0].includes(20));
		
		header = new Header("Range", "-10");
		ranges = header.createByteRanges(20);
		assertEquals(1, ranges.length);
		assertEquals(ranges[0].getStart(), 10);
		assertEquals(ranges[0].getEnd(), 19);
		
		header = new Header("Range", "0-30");
		ranges = header.createByteRanges(20);
		assertEquals(1, ranges.length);
		assertEquals(ranges[0].getStart(), 0);
		assertEquals(ranges[0].getEnd(), 19);
		
		header = new Header("Range", "10");
		ranges = header.createByteRanges(20);
		assertEquals(1, ranges.length);
		assertEquals(ranges[0].getStart(), 10);
		assertEquals(ranges[0].getEnd(), 10);		
		
		header = new Header("Range", "19-30");
		ranges = header.createByteRanges(20);
		assertEquals(1, ranges.length);
		assertEquals(ranges[0].getStart(), 19);
		assertEquals(ranges[0].getEnd(), 19);		
		
		header = new Header("Range", "30-40");
		ranges = header.createByteRanges(20);
		assertEquals(1, ranges.length);
		assertEquals(ranges[0].getStart(), 30);
		assertEquals(ranges[0].getEnd(), 40);		
		
		try {
			header = new Header("Range", "invalid");
			ranges = header.createByteRanges(20);
			fail("Must not be possible to create byte ranges with invalid values");
		} catch(HeaderException e) {}

		try {
			header = new Header("Range", "20-10");
			ranges = header.createByteRanges(20);
			fail("Must not be possible to create byte ranges with invalid range");
		} catch(HeaderException e) {}
	}
	
	public static Test suite() {
		return new TestSuite(ByteRangeTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
