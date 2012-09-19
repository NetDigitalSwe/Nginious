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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class MultipartParserTestCase extends TestCase {
	
	public MultipartParserTestCase() {
		super();
	}

	public MultipartParserTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testMultipartParser1() throws Exception {
		MultipartParser parser = new MultipartParser();
		parser.setBoundary("AaB03x");
		
		String data = "--AaB03x\015\012" +
			"Content-Disposition: form-data; name=\"pics\"; filename=\"file.txt\"\015\012" +
			"Content-Type: text/plain\015\012\015\012" +
			"Test\015\012" +
			"--AaB03x--\015\012";
		
		boolean result = parser.parse(data.getBytes());
		assertTrue("Should be done after parsing", result);
		HashMap<String, FilePart> parts = parser.getFileParts();
		assertNotNull(parts);
		assertEquals(1, parts.size());
		
		FilePart part = parts.get("pics");
		assertNotNull(part);
		assertEquals("text/plain", part.getContentType());
		assertEquals("pics", part.getName());
		assertEquals("file.txt", part.getFilename());
		assertEquals("text/plain", part.getHeader("Content-Type"));
		assertEquals("form-data; name=\"pics\"; filename=\"file.txt\"", part.getHeader("Content-Disposition"));
		assertNull(part.getHeader("Nonexistent"));
		Collection<String> names = part.getHeaderNames();
		assertTrue(names.contains("Content-Type"));
		assertTrue(names.contains("Content-Disposition"));
		assertFalse(names.contains("Nonexistent"));
		assertEquals(4, part.getSize());
		InputStream in = part.getInputStream();
		byte[] b = new byte[1024];
		int len = in.read(b);
		String content = new String(b, 0, len);
		assertEquals("Test", content);
	}
	
	public void testMultipartParser2() throws Exception {
		MultipartParser parser = new MultipartParser();
		parser.setBoundary("AaB03x");
		
		String data = "--AaB03x\015\012" +
			"Content-Disposition: form-data; name=\"test1\"; filename=\"file1.txt\"\015\012" +
			"Content-Type: text/plain\015\012\015\012" +
			"Test1\015\012" +
			"--AaB03x\015\012" +
			"Content-Disposition: form-data; name=\"test2\"; filename=\"file2.txt\"\015\012" +
			"Content-Type: text/html\015\012\015\012" +
			"Test2\015\012" +
			"--AaB03x--\015\012";
		
		boolean result = parser.parse(data.getBytes());
		assertTrue("Should be done after parsing", result);
		HashMap<String, FilePart> parts = parser.getFileParts();
		assertNotNull(parts);
		assertEquals(2, parts.size());
		
		FilePart part = parts.get("test1");
		assertNotNull(part);
		assertEquals("text/plain", part.getContentType());
		assertEquals("test1", part.getName());
		assertEquals("file1.txt", part.getFilename());
		assertEquals(5, part.getSize());
		InputStream in = part.getInputStream();
		byte[] b = new byte[1024];
		int len = in.read(b);
		String content = new String(b, 0, len);
		assertEquals("Test1", content);

		part = parts.get("test2");
		assertNotNull(part);
		assertEquals("text/html", part.getContentType());
		assertEquals("test2", part.getName());
		assertEquals("file2.txt", part.getFilename());
		assertEquals(5, part.getSize());
		in = part.getInputStream();
		b = new byte[1024];
		len = in.read(b);
		content = new String(b, 0, len);
		assertEquals("Test2", content);
	}
	
	public void testMultipartParser3() throws Exception {
		MultipartParser parser = new MultipartParser();
		parser.setBoundary("AaB03x");
		
		String data = "--AaB03x\015\012" +
			"Content-Disposition: form-data; name=\"test1\"\015\012\015\012" +
			"Test1\015\012" +
			"--AaB03x\015\012" +
			"Content-Disposition: form-data; name=\"test2\"; filename=\"file2.txt\"\015\012" +
			"Content-Type: text/html\015\012\015\012" +
			"Test2\015\012" +
			"--AaB03x--\015\012";
		
		boolean result = parser.parse(data.getBytes());
		assertTrue("Should be done after parsing", result);
		HashMap<String, FieldPart> parts = parser.getFieldParts();
		assertNotNull(parts);
		assertEquals(1, parts.size());
		
		FieldPart part = parts.get("test1");
		assertNotNull(part);
		assertEquals("test1", part.getName());
		assertEquals("Test1", part.getValue());
		
		HashMap<String, FilePart> parts2 = parser.getFileParts();
		assertNotNull(parts2);
		assertEquals(1, parts2.size());
		FilePart part2 = parts2.get("test2");
		assertNotNull(part2);
		assertEquals("text/html", part2.getContentType());
		assertEquals("test2", part2.getName());
		assertEquals("file2.txt", part2.getFilename());
		assertEquals(5, part2.getSize());
		InputStream in = part2.getInputStream();
		byte[] b = new byte[1024];
		int len = in.read(b);
		String content = new String(b, 0, len);
		assertEquals("Test2", content);
	}
	
	public void testMultipartParser4() throws Exception {
		String data = "--AaB03x\015\012" +
			"Content-Disposition: form-data; name=\"test1\"; filename=\"file1.txt\"\015\012" +
			"Content-Type: text/plain\015\012\015\012" +
			"Test1\015\012" +
			"--AaB03x\015\012" +
			"Content-Disposition: form-data; name=\"test2\"; filename=\"file2.txt\"\015\012" +
			"Content-Type: text/html\015\012\015\012" +
			"Test2\015\012" +
			"--AaB03x--\015\012";
		
		boolean result = false;
		byte[] dataBytes = data.getBytes();
		
		for(int i = 3; i <= 10; i++) {
			MultipartParser parser = new MultipartParser();
			parser.setBoundary("AaB03x");

			for(int j = 0; j < dataBytes.length; j += i) {
				byte[] bytePart = new byte[i];
				System.arraycopy(dataBytes, j, bytePart, 0, dataBytes.length - j < i ? dataBytes.length - j : i);
				result = parser.parse(bytePart);
			}
			
			assertTrue("Should be done after parsing", result);
			HashMap<String, FilePart> parts = parser.getFileParts();
			assertNotNull(parts);
			assertEquals("i=" + i, 2, parts.size());
			
			FilePart part = parts.get("test1");
			assertNotNull(part);
			assertEquals("text/plain", part.getContentType());
			assertEquals("test1", part.getName());
			assertEquals("file1.txt", part.getFilename());
			// assertEquals("i=" + i, 5, part.getSize());
			InputStream in = part.getInputStream();
			byte[] b = new byte[1024];
			int len = in.read(b);
			String content = new String(b, 0, len);
			assertEquals("i=" + i, "Test1", content);

			part = parts.get("test2");
			assertNotNull(part);
			assertEquals("text/html", part.getContentType());
			assertEquals("test2", part.getName());
			assertEquals("file2.txt", part.getFilename());
			assertEquals("i=" + i, 5, part.getSize());
			in = part.getInputStream();
			b = new byte[1024];
			len = in.read(b);
			content = new String(b, 0, len);
			assertEquals("Test2", content);				
		}		
	}
	
	public void testMultipartParser5() throws Exception {
		MultipartParser parser = new MultipartParser();
		parser.setBoundary("AaB03x");
		
		String content = createData(8192);
		String data = "--AaB03x\015\012" +
			"Content-Disposition: form-data; name=\"test1\"\015\012\015\012" +
			"Test1\015\012" +
			"--AaB03x\015\012" +
			"Content-Disposition: form-data; name=\"test2\"; filename=\"file2.txt\"\015\012" +
			"Content-Type: text/plain\015\012\015\012" +
			content + "\015\012" +
			"--AaB03x--\015\012";
		
		boolean result = parser.parse(data.getBytes());
		assertTrue("Should be done after parsing", result);
		HashMap<String, FieldPart> parts = parser.getFieldParts();
		assertNotNull(parts);
		assertEquals(1, parts.size());
		
		FieldPart part = parts.get("test1");
		assertNotNull(part);
		assertEquals("test1", part.getName());
		assertEquals("Test1", part.getValue());
		
		HashMap<String, FilePart> parts2 = parser.getFileParts();
		assertNotNull(parts2);
		assertEquals(1, parts2.size());
		FilePart part2 = parts2.get("test2");
		assertNotNull(part2);
		assertEquals("text/plain", part2.getContentType());
		assertEquals("test2", part2.getName());
		assertEquals("file2.txt", part2.getFilename());
		assertEquals(8192, part2.getSize());
		InputStream in = part2.getInputStream();
		byte[] b = new byte[8192];
		int len = in.read(b);
		in.close();
		String realContent = new String(b, 0, len);
		assertEquals(content, realContent);
			
		File file = part2.getFile();
		assertEquals(file.length(), 8192);
		FileInputStream fIn = new FileInputStream(file);
		len = fIn.read(b);
		fIn.close();
		realContent = new String(b, 0, len);
		assertEquals(content, realContent);
				
		part2.delete();
	}
	
	public void testMultipartParser6() throws Exception {
		MultipartParser parser = new MultipartParser();
		parser.setBoundary("AaB03x");
		
		String content = createData(8193);
		String data = "--AaB03x\015\012" +
			"Content-Disposition: form-data; name=\"test1\"\015\012\015\012" +
			"Test1\015\012" +
			"--AaB03x\015\012" +
			"Content-Disposition: form-data; name=\"test2\"; filename=\"file2.txt\"\015\012" +
			"Content-Type: text/plain\015\012\015\012" +
			content + "\015\012" +
			"--AaB03x--\015\012";
		
		boolean result = parser.parse(data.getBytes());
		assertTrue("Should be done after parsing", result);
		HashMap<String, FieldPart> parts = parser.getFieldParts();
		assertNotNull(parts);
		assertEquals(1, parts.size());
		
		FieldPart part = parts.get("test1");
		assertNotNull(part);
		assertEquals("test1", part.getName());
		assertEquals("Test1", part.getValue());
		
		HashMap<String, FilePart> parts2 = parser.getFileParts();
		assertNotNull(parts2);
		assertEquals(1, parts2.size());
		FilePart part2 = parts2.get("test2");
		assertNotNull(part2);
		assertEquals("text/plain", part2.getContentType());
		assertEquals("test2", part2.getName());
		assertEquals("file2.txt", part2.getFilename());
		assertEquals(8193, part2.getSize());
		InputStream in = part2.getInputStream();
		byte[] b = new byte[8193];
		int len = in.read(b);
		String realContent = new String(b, 0, len);
		assertEquals(content, realContent);
		
		File file = part2.getFile();
		assertEquals(file.length(), 8193);
		FileInputStream fIn = new FileInputStream(file);
		len = fIn.read(b);
		fIn.close();
		realContent = new String(b, 0, len);
		assertEquals(content, realContent);
		
		part2.delete();
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
	
	public static Test suite() {
		return new TestSuite(MultipartParserTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
