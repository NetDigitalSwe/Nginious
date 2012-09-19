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

package com.nginious.http.xsp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import com.nginious.http.common.FileUtils;
import com.nginious.http.xsp.DocumentPart;
import com.nginious.http.xsp.ForEachTagPart;
import com.nginious.http.xsp.IfTagPart;
import com.nginious.http.xsp.StaticPart;
import com.nginious.http.xsp.XspException;
import com.nginious.http.xsp.XspParser;
import com.nginious.http.xsp.XspPart;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class XspParserTestCase extends TestCase {
	
	private File tmpDir;
	
	public XspParserTestCase() {
		super();
	}

	public XspParserTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		this.tmpDir = new File(System.getProperty("java.io.tmpdir"));
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		
		File classDir = new File(this.tmpDir, "com");
		
		if(classDir.exists()) {
			FileUtils.deleteDir(classDir);
		}
	}
	
	public void testXspParser1() throws Exception {
		byte[] content = getContent("src/testweb/webapp/WEB-INF/xsp/XspCompilerTest.xsp");
		
		XspParser parser = new XspParser();
		File destFile = new File(this.tmpDir, "xsp/XspCompilerTest.xsp");
		DocumentPart document = parser.parse(destFile.getAbsolutePath(), content);
		
		assertNotNull(document);
		String packageName = document.getMetaContent("package");
		assertEquals("com.nginious.http.xsp", packageName);
		String contentType = document.getMetaContent("Content-Type");
		assertEquals("text/html; charset=utf-8", contentType);
		
		List<XspPart> parts = document.getContentParts();
		assertEquals(4, parts.size());
		
		XspPart part1 = parts.get(0);
		assertEquals("<html>\n  <head>\n    ", part1.toString());
		XspPart part2 = parts.get(3);
		assertEquals("\n    <title>XspCompilerTest</title>\n  </head>\n  <body>\n    <h1>XspCompilerTest</h1>\n  </body>\n</html>\n", part2.toString());
	}
	
	public void testXspParser2() throws Exception {
		byte[] content = getContent("src/testweb/webapp/WEB-INF/xsp/XspTagsCompileTest.xsp");
		
		XspParser parser = new XspParser();
		File destFile = new File(this.tmpDir, "xsp/XspTagsCompileTest.xsp");		
		DocumentPart document = parser.parse(destFile.getAbsolutePath(), content);
		
		assertNotNull(document);
		String packageName = document.getMetaContent("package");
		assertEquals("com.nginious.http.xsp", packageName);
		String contentType = document.getMetaContent("Content-Type");
		assertEquals("text/html; charset=utf-8", contentType);
		
		List<XspPart> parts = document.getContentParts();
		assertEquals(6, parts.size());

		XspPart part1 = parts.get(0);
		assertEquals("<html>\n  <head>\n    ", part1.toString());
		XspPart part2 = parts.get(3);
		assertEquals("\n    <title>XspTagsCompile</title>\n  </head>\n  <body>\n    <h1>XspTagsCompile</h1>\n    ", part2.toString());
		XspPart part3 = parts.get(4);
		assertTrue(part3 instanceof IfTagPart);
		IfTagPart ifPart = (IfTagPart)part3;
		StaticPart testPart = ifPart.getTestAttribute();
		assertEquals("${test == 'Hello world!'}", testPart.toString());
		
		List<XspPart> subParts = ifPart.getContentParts();
		assertEquals(3, subParts.size());
		XspPart subPart1 = subParts.get(1);
		assertTrue(subPart1.toString(), subPart1 instanceof ForEachTagPart);
		
		ForEachTagPart forPart = (ForEachTagPart)subPart1;
		subParts = forPart.getContentParts();
		assertEquals(5, subParts.size());
		
		part1 = subParts.get(0);
		assertEquals("\n\t<h2>", part1.toString());
		XspPart exprPart = subParts.get(1);
		assertEquals("${test}", exprPart.toString());
		part2 = subParts.get(2);
		assertEquals(" ", part2.toString());
		exprPart = subParts.get(3);
		assertEquals("${var}", exprPart.toString());
		part3 = subParts.get(4);
		assertEquals("</h2>\n      ", part3.toString());
		
		XspPart part4 = parts.get(5);
		assertEquals("\n  </body>\n</html>\n", part4.toString());
	}
	
	public void testBadAttribute() throws Exception {
		byte[] content = getContent("src/testweb/webapp/WEB-INF/xsp/XspBadAttribute.xsp");
		
		try {
			XspParser parser = new XspParser();
			File destFile = new File(this.tmpDir, "xsp/XspBadAttribute.xsp");
			parser.parse(destFile.getAbsolutePath(), content);
			fail("Must not be possible to compile xsp file with invalid xsp tag attribute");
		} catch(XspException e) {}
	}
	
	public void testBadEndTag() throws Exception {
		byte[] content = getContent("src/testweb/webapp/WEB-INF/xsp/XspBadEndTag.xsp");
		
		try {
			XspParser parser = new XspParser();
			File destFile = new File(this.tmpDir, "xsp/XspBadEndTag.xsp");
			parser.parse(destFile.getAbsolutePath(), content);
			fail("Must not be possible to compile xsp file with invalid xsp end tag");
		} catch(XspException e) {}
	}
	
	private byte[] getContent(String fileName) throws IOException {
		FileInputStream in = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		
		try {
			in = new FileInputStream(fileName);
			int len = 0;
			byte[] b = new byte[1024];
			
			while((len = in.read(b)) > 0) {
				out.write(b, 0, len);
			}
			
			return out.toByteArray();
		} finally {
			if(in != null) {
				try { in.close(); } catch(IOException e) {}
			}
		}
	}
	
    public static Test suite() {
		return new TestSuite(XspParserTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
