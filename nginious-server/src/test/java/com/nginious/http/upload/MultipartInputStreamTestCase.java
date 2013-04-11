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

package com.nginious.http.upload;

import java.io.ByteArrayInputStream;
import java.util.Random;

import com.nginious.http.upload.MultipartInputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class MultipartInputStreamTestCase extends TestCase {
	
	public MultipartInputStreamTestCase() {
		super();
	}

	public MultipartInputStreamTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testMultipartInputStream() throws Exception {
		for(int i = 1; i <= 131072; i += 537) {
			String data = createData(i);
			byte[] byteData = data.getBytes();
			String multi = createMultipartData(data);
			byte[] multiData = multi.getBytes();
			
			MultipartInputStream in = new MultipartInputStream("AaB03x", new ByteArrayInputStream(byteData), byteData.length);
			in.setHeader("Content-Disposition", "form-data; name=\"pics\"; filename=\"file.txt\"");
			in.setHeader("Content-Type", "text/plain");
			
			assertEquals(multiData.length, in.length());
			byte[] b = new byte[1024];
			int len = 0;
			int pos = 0;
			
			while((len = in.read(b)) > 0) {
				for(int j = 0; j < len; j++) {
					assertTrue(b[j] == multiData[j + pos]);
				}
				
				pos += len;
			}
			
			in.close();
		}
	}
		
	private static final String chars = "ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvxyz0123456789";
	
	private static String createData(int length) {
		StringBuffer data = new StringBuffer();
		Random rnd = new Random();
		
		for(int i = 0; i < length; i++) {
			int idx = rnd.nextInt(chars.length());
			data.append(chars.substring(idx, idx + 1));
		}
		
		return data.toString();
	}
	
	private static String createMultipartData(String data) {
		StringBuffer multi = new StringBuffer();
		multi.append("--AaB03x\015\012");
		multi.append("Content-Disposition: form-data; name=\"pics\"; filename=\"file.txt\"\015\012");
		multi.append("Content-Type: text/plain\015\012\015\012");
		multi.append(data);
		multi.append("\015\012--AaB03x--\015\012");
		return multi.toString();
	}
	
	public static Test suite() {
		return new TestSuite(MultipartInputStreamTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
