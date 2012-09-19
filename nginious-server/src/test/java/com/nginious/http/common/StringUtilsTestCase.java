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

import com.nginious.http.common.StringUtils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class StringUtilsTestCase extends TestCase {
	
	public StringUtilsTestCase() {
		super();
	}

	public StringUtilsTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testBytesToHex() throws Exception {
		for(int i = 1; i <= 1024; i++) {
			byte[] data = createData(i);
			String hex = StringUtils.asHexString(data);
			
			for(int j = 0; j < i; j++) {
				int start = j * 2;
				int end = start + 2;
				int val = Integer.parseInt(hex.substring(start, end), 16);
				assertEquals((int)data[j], val);
			}
		}
	}
		
	public void testHexToBytes() throws Exception {
		for(int i = 1; i <= 1024; i++) {
			String hex = createHexData(i);
			byte[] data = StringUtils.convertHexStringToBytes(hex);
			
			for(int j = 0; j < i; j++) {
				int start = j * 2;
				int end = start + 2;
				String part = hex.substring(start, end);
				int val = Integer.parseInt(part, 16);
				assertEquals("Failed hex=" + part + ", val=" + (byte)val + ", data=" + data[j] + ", j=" + j, data[j], (byte)val);				
			}
		}
	}
		
	private static final String hexChars = "0123456789abcdef";
	
	private static final String chars = "ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvxyz0123456789";
	
	private static String createHexData(int length) {
		StringBuffer hex = new StringBuffer();
		Random rnd = new Random();
		
		for(int i = 0; i < length; i++) {
			int idx = rnd.nextInt(hexChars.length());
			hex.append(hexChars.substring(idx, idx + 1));
			idx = rnd.nextInt(hexChars.length());
			hex.append(hexChars.substring(idx, idx + 1));
		}
		
		return hex.toString();
	}
	
	private static byte[] createData(int length) {
		byte[] data = new byte[length];
		Random rnd = new Random();
		
		for(int i = 0; i < length; i++) {
			int idx = rnd.nextInt(chars.length());
			data[i] = chars.substring(idx, idx + 1).getBytes()[0];
		}
		
		return data;
	}
	
	public static Test suite() {
		return new TestSuite(StringUtilsTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
