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

package com.nginious.http.cmd;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.nginious.http.cmd.CommandLineArguments;
import com.nginious.http.cmd.CommandLineException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class CmdTestCase extends TestCase {
	
    public CmdTestCase() {
		super();
	}

	public CmdTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testShortNameCmd() throws Exception {
		TestBean bean = new TestBean();
		CommandLineArguments arguments = CommandLineArguments.createInstance(bean);
		String[] args = { "-b", "1", "-s", "2", "-i", "3", "-l", "4", "-f", "1.0", "-d", "2.0", "-t", "test", "-o" };
		
		arguments.parse(args);
		assertEquals(1, bean.getTestByte());
		assertEquals(2, bean.getTestShort());
		assertEquals(3, bean.getTestInteger());
		assertEquals(4, bean.getTestLong());
		assertEquals(1.0f, bean.getTestFloat());
		assertEquals(2.0d, bean.getTestDouble());
		assertEquals("test", bean.getTestString());
		assertTrue(bean.getTestBoolean());
	}
	
	public void testLongNameCmd() throws Exception {
		TestBean bean = new TestBean();
		CommandLineArguments arguments = CommandLineArguments.createInstance(bean);
		String[] args = { "--testByte=1", "--testShort=2", "--testInt=3", "--testLong=4", "--testFloat=1.0", "--testDouble=2.0", "--testString=test", "--testBoolean" };
		
		arguments.parse(args);
		assertEquals(1, bean.getTestByte());
		assertEquals(2, bean.getTestShort());
		assertEquals(3, bean.getTestInteger());
		assertEquals(4, bean.getTestLong());
		assertEquals(1.0f, bean.getTestFloat());
		assertEquals(2.0d, bean.getTestDouble());
		assertEquals("test", bean.getTestString());
		assertTrue(bean.getTestBoolean());
	}
	
	public void testInvalidCmd() throws Exception {
		TestBean bean = new TestBean();
		CommandLineArguments arguments = CommandLineArguments.createInstance(bean);
		
		try {
			String[] args1 = { "-b", "256" };
			arguments.parse(args1);;
			fail("Must not be possible to parse invalid byte");
		} catch(CommandLineException e) {}

		try {
			String[] args1 = { "-s", "65536" };
			arguments.parse(args1);;
			fail("Must not be possible to parse invalid short");
		} catch(CommandLineException e) {}
		
		try {
			String[] args1 = { "-i", "text" };
			arguments.parse(args1);;
			fail("Must not be possible to parse invalid int");
		} catch(CommandLineException e) {}
		
		try {
			String[] args1 = { "-l", "text" };
			arguments.parse(args1);;
			fail("Must not be possible to parse invalid long");
		} catch(CommandLineException e) {}
		
		try {
			String[] args1 = { "-f", "text" };
			arguments.parse(args1);;
			fail("Must not be possible to parse invalid float");
		} catch(CommandLineException e) {}
		
		try {
			String[] args1 = { "-d", "text" };
			arguments.parse(args1);;
			fail("Must not be possible to parse invalid double");
		} catch(CommandLineException e) {}
	}
	
	public void testHelp() throws Exception {
		TestBean bean = new TestBean();
		CommandLineArguments arguments = CommandLineArguments.createInstance(bean);
		StringWriter strWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(strWriter);
		arguments.help(writer);
		
		System.out.println(strWriter.toString());
	}
	
	public static Test suite() {
		return new TestSuite(CmdTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
