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

package com.nginious.http.common;

import java.util.Random;

import com.nginious.http.common.ExpandableBuffer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ExpandableBufferTestCase extends TestCase {
	
	public ExpandableBufferTestCase() {
		super();
	}

	public ExpandableBufferTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testData() throws Exception {
		for(int i = 1; i <= 131072; i *= 2) {
			byte[] data = createData(i);
			
			for(int j = 1; j < 8192; j *= 2) {
				int parts = (int)(i / j);
				ExpandableBuffer buffer = new ExpandableBuffer(2097152);
				
				for(int k = 0; k < parts; k++) {
					buffer.put(data, k * j, j);
				}
				
				if(i % j != 0) {
					buffer.put(data, parts * j, i <= j ? i : i % j);
				}
				
				byte[] outData = buffer.toByteArray();
				assertEquals(data, outData);
				outData = new byte[i];
				buffer.get(outData, 0, i);
				assertEquals(data, outData);
			}
		}
	}
	
	public void testGeneral() throws Exception {
		ExpandableBuffer buffer = new ExpandableBuffer(1000);
		
		// Test no data
		assertEquals(0, buffer.size());
		assertEquals(new byte[0], buffer.toByteArray());
		
		// Test fill up buffer
		byte[] data = createData(1000);
		buffer.put(data);
		assertEquals(1000, buffer.size());
		assertEquals(0, buffer.remaining());
		
		// Test overflow buffer
		buffer.reset();
		data = createData(1001);
		int len = buffer.put(data);
		assertEquals(len, 1000);
		
		// Test set index
		data = createData(1000);
		buffer.put(data);
		buffer.setIndex(0);
		assertEquals(0, buffer.getIndex());
		buffer.setIndex(999);
		assertEquals(999, buffer.getIndex());
		
		try {
			buffer.setIndex(1000);
			fail("Must not be possible to set index out of range");
		} catch(ArrayIndexOutOfBoundsException e) {
			assertEquals(999, buffer.getIndex());
		}
		
		try {
			buffer.setIndex(-1);
			fail("Must not be possible to set index out of range");
		} catch(ArrayIndexOutOfBoundsException e) {
			assertEquals(999, buffer.getIndex());
		}
		
		// Test get
		buffer.reset();
		buffer.put(data);
		
		for(int i = 0; i < 1000; i++) {
			assertEquals(i, buffer.getIndex());
			int val = buffer.get();
			assertEquals(data[i], val);
		}
		
		assertEquals(-1, buffer.get());
		
		// Test put at
		buffer.reset();
		buffer.put(data);
		data = createData(100);
		buffer.putAt(0, data);
		buffer.putAt(900, data);
		
		try {
			buffer.putAt(901, data);
			fail("Must not be possible to overflow buffer");
		} catch(ArrayIndexOutOfBoundsException e) {}
		
		// Test skip
		buffer.reset();
		data = createData(100);
		buffer.put(data);
		buffer.skip(100);
		buffer.put(data);
		int skipped = buffer.skip(1000);
		assertEquals(700, skipped);
	}
	
	private void assertEquals(byte[] first, byte[] second) {
		assertEquals(first.length, second.length);
		
		for(int i = 0 ; i< first.length; i++) {
			assertEquals("Failed i=" + i + ", len=" + first.length + ", first=" + first[i] + ", second=" + second[i], first[i], second[i]);
		}
	}
	
	private static final String chars = "ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvxyz0123456789";
	
	private static byte[] createData(int length) {
		byte[] data = new byte[length];
		Random rnd = new Random();
		
		for(int i = 0; i < length; i++) {
			int idx = rnd.nextInt(chars.length());
			data[i] = chars.substring(idx, idx + 1).getBytes()[0];
			// System.out.println("Data: " + data[i]);
		}
		
		return data;
	}
	
    public static Test suite() {
		return new TestSuite(ExpandableBufferTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
