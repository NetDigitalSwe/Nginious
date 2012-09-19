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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.nginious.http.xsp.expr.Expression;
import com.nginious.http.xsp.expr.ExpressionCompiler;
import com.nginious.http.xsp.expr.ExpressionException;
import com.nginious.http.xsp.expr.ExpressionParser;
import com.nginious.http.xsp.expr.TreeExpression;
import com.nginious.http.xsp.expr.Type;

public class ExpressionParserTestCase extends TestCase {
	
	public ExpressionParserTestCase() {
		super();
	}

	public ExpressionParserTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testIntegerComparisonUncompiled() throws Exception {
		ExpressionParser parser = new ExpressionParser();

		TreeExpression expr = parser.parse("2 == 1");		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));

		expr = parser.parse("1 == 1");		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));
		
		expr = parser.parse("1 == null");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));

		expr = parser.parse("1 != 1");		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));

		expr = parser.parse("1 != 2");		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));
		
		expr = parser.parse("1 != null");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));
		
		expr = parser.parse("1 > 2");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));		
		
		expr = parser.parse("3 > 2");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));		

		expr = parser.parse("3 > null");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));		

		expr = parser.parse("1 >= 2");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));		
		
		expr = parser.parse("2 >= 2");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));		

		expr = parser.parse("3 >= null");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));		

		expr = parser.parse("3 < 2");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));		
		
		expr = parser.parse("1 < 2");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));		
		
		expr = parser.parse("1 < null");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));		

		expr = parser.parse("3 <= 2");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));		
		
		expr = parser.parse("2 <= 2");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));

		expr = parser.parse("3 > null");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));		
	}
	
	public void testDoubleComparisonUncompiled() throws Exception {
		ExpressionParser parser = new ExpressionParser();

		TreeExpression expr = parser.parse("2.0 == 1");		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));

		expr = parser.parse("1.0001 == 1.0001");		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));

		expr = parser.parse("1.0001 == null");		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));

		expr = parser.parse("1.0 != 1.0");		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));

		expr = parser.parse("1.0 != 1.01");		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));
		
		expr = parser.parse("1.0 != null");		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));
		
		expr = parser.parse("1.0 > 1.0001");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));		
		
		expr = parser.parse("2.0001 > 2");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));		

		expr = parser.parse("2.0001 > null");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));		
		
		expr = parser.parse("1 >= 1.001");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));		
		
		expr = parser.parse("2.0 >= 2.0");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));		

		expr = parser.parse("2.0 >= null");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));		

		expr = parser.parse("2.0001 < 2");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));		
		
		expr = parser.parse("1.0 < 1.0001");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));		
		
		expr = parser.parse("1.0 < null");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));		
		
		expr = parser.parse("3.0 <= 2.999");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));		
		
		expr = parser.parse("2.001 <= 2.001");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));		
		
		expr = parser.parse("2.001 <= null");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));		
	}
	
	public void testStringComparisonUncompiled() throws Exception {
		ExpressionParser parser = new ExpressionParser();

		TreeExpression expr = parser.parse("'test1' == 'test2'");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));

		expr = parser.parse("'test1' == 'test1'");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));

		expr = parser.parse("'test1' == null");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));

		expr = parser.parse("'test1' != 'test1'");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));

		expr = parser.parse("'test1' != 'test2'");		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));
		
		expr = parser.parse("'test1' != null");		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));
		
		expr = parser.parse("'åäö' == 'åäö'");		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));
		
		try {
			expr = parser.parse("'test1' > 'test2'");
			fail("Must not be possible to use more than with strings");
		} catch(ExpressionException e) {}

		try {
			expr = parser.parse("'test1' >= 'test2'");
			fail("Must not be possible to use more than or equals with strings");
		} catch(ExpressionException e) {}

		try {
			expr = parser.parse("'test1' < 'test2'");
			fail("Must not be possible to use less than with strings");
		} catch(ExpressionException e) {}

		try {
			expr = parser.parse("'test1' <= 'test2'");
			fail("Must not be possible to use less than or equals with strings");
		} catch(ExpressionException e) {}
	}
	
	public void testBooleanUncompiled() throws Exception {
		ExpressionParser parser = new ExpressionParser();

		TreeExpression expr = parser.parse("2 == 1 || 3 == 1");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));

		expr = parser.parse("1 == 1 || 2 == 1");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));

		expr = parser.parse("1 == 1 || 2 == 2");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));
		

		expr = parser.parse("1 == 1 && 3 == 1");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));

		expr = parser.parse("1 == 1 && 2 == 2");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));

		expr = parser.parse("1 == 3 && 2 == 2");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));
	}
	
	public void testIntegerArithmeticUncompiled() throws Exception {
		ExpressionParser parser = new ExpressionParser();
		
		TreeExpression expr = parser.parse("2 + 1");		
		assertEquals(Type.INT, expr.getType());
		assertEquals(3, expr.evaluateInt(new TestVariables()));

		expr = parser.parse("2 + null");		
		assertEquals(Type.INT, expr.getType());
		assertEquals(2, expr.evaluateInt(new TestVariables()));

		expr = parser.parse("2 - 1");		
		assertEquals(Type.INT, expr.getType());
		assertEquals(1, expr.evaluateInt(new TestVariables()));

		expr = parser.parse("2 - null");		
		assertEquals(Type.INT, expr.getType());
		assertEquals(2, expr.evaluateInt(new TestVariables()));

		expr = parser.parse("2 * 2");		
		assertEquals(Type.INT, expr.getType());
		assertEquals(4, expr.evaluateInt(new TestVariables()));

		expr = parser.parse("2 * null");		
		assertEquals(Type.INT, expr.getType());
		assertEquals(0, expr.evaluateInt(new TestVariables()));
		
		expr = parser.parse("9 / 3");		
		assertEquals(Type.INT, expr.getType());
		assertEquals(3, expr.evaluateInt(new TestVariables()));
		
		expr = parser.parse("null / 3");		
		assertEquals(Type.INT, expr.getType());
		assertEquals(0, expr.evaluateInt(new TestVariables()));
		
		expr = parser.parse("9 % 3");		
		assertEquals(Type.INT, expr.getType());
		assertEquals(0, expr.evaluateInt(new TestVariables()));		

		expr = parser.parse("null % 3");		
		assertEquals(Type.INT, expr.getType());
		assertEquals(0, expr.evaluateInt(new TestVariables()));		

		expr = parser.parse("10 % 3");		
		assertEquals(Type.INT, expr.getType());
		assertEquals(1, expr.evaluateInt(new TestVariables()));		
	}
	
	public void testDoubleArithmeticUncompiled() throws Exception {
		ExpressionParser parser = new ExpressionParser();
		
		TreeExpression expr = parser.parse("2.1 + 1");		
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(3.1d, expr.evaluateDouble(new TestVariables()));

		expr = parser.parse("2.1 + null");		
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(2.1d, expr.evaluateDouble(new TestVariables()));

		expr = parser.parse("2 - 1.0");		
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(1.0d, expr.evaluateDouble(new TestVariables()));

		expr = parser.parse("2.0 - null");		
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(2.0d, expr.evaluateDouble(new TestVariables()));

		expr = parser.parse("2.1 * 2");		
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(4.2d, expr.evaluateDouble(new TestVariables()));

		expr = parser.parse("2.1 * null");		
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(0.0d, expr.evaluateDouble(new TestVariables()));

		expr = parser.parse("9.0 / 3");		
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(3.0d, expr.evaluateDouble(new TestVariables()));
		
		expr = parser.parse("null / 3.0");		
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(0.0d, expr.evaluateDouble(new TestVariables()));
		
		expr = parser.parse("9.0 % 3");		
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(0.0d, expr.evaluateDouble(new TestVariables()));		

		expr = parser.parse("null % 3.0");		
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(0.0d, expr.evaluateDouble(new TestVariables()));		
		
		expr = parser.parse("10 % 3.0");		
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(1.0d, expr.evaluateDouble(new TestVariables()));		
	}
	
	public void testIntegerFunctionsUncompiled() throws Exception {
		ExpressionParser parser = new ExpressionParser();
		
		TreeExpression expr = parser.parse("abs(-1)");
		assertEquals(Type.INT, expr.getType());
		assertEquals(1, expr.evaluateInt(new TestVariables()));
		
		expr = parser.parse("min(2, 5)");
		assertEquals(Type.INT, expr.getType());
		assertEquals(2, expr.evaluateInt(new TestVariables()));
		
		expr = parser.parse("min(2, null)");
		assertEquals(Type.INT, expr.getType());
		assertEquals(0, expr.evaluateInt(new TestVariables()));
		
		expr = parser.parse("max(2, 5)");
		assertEquals(Type.INT, expr.getType());
		assertEquals(5, expr.evaluateInt(new TestVariables()));		
		
		expr = parser.parse("max(null, 5)");
		assertEquals(Type.INT, expr.getType());
		assertEquals(5, expr.evaluateInt(new TestVariables()));		
	}
	
	public void testDoubleFunctionsUncompiled() throws Exception {
		ExpressionParser parser = new ExpressionParser();
		
		TreeExpression expr = parser.parse("abs(-0.51)");
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(0.51d, expr.evaluateDouble(new TestVariables()));
		
		expr = parser.parse("abs(null)");
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(0.0d, expr.evaluateDouble(new TestVariables()));
		
		expr = parser.parse("acos(1.0)");
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(0.0d, expr.evaluateDouble(new TestVariables()));
		
		expr = parser.parse("asin(0.0)");
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(0.0d, expr.evaluateDouble(new TestVariables()));
		
		expr = parser.parse("atan(0.0)");
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(0.0d, expr.evaluateDouble(new TestVariables()));
		
		expr = parser.parse("ceil(1.21)");
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(2.0d, expr.evaluateDouble(new TestVariables()));
		
		expr = parser.parse("cos(0.0)");
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(1.0d, expr.evaluateDouble(new TestVariables()));
		
		expr = parser.parse("exp(0.0)");
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(1.0d, expr.evaluateDouble(new TestVariables()));
		
		expr = parser.parse("floor(1.21)");
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(1.0d, expr.evaluateDouble(new TestVariables()));
		
		expr = parser.parse("log(1.0)");
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(0.0d, expr.evaluateDouble(new TestVariables()));
		
		expr = parser.parse("min(1.0, 2.0)");
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(1.0d, expr.evaluateDouble(new TestVariables()));

		expr = parser.parse("max(1.0, 2.0)");
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(2.0d, expr.evaluateDouble(new TestVariables()));

		expr = parser.parse("pow(2.0, 2.0)");
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(4.0d, expr.evaluateDouble(new TestVariables()));

		expr = parser.parse("rint(2.123)");
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(2.0d, expr.evaluateDouble(new TestVariables()));

		expr = parser.parse("sin(0.0)");
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(0.0d, expr.evaluateDouble(new TestVariables()));

		expr = parser.parse("sqrt(9.0)");
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(3.0d, expr.evaluateDouble(new TestVariables()));

		expr = parser.parse("tan(0.0)");
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(0.0d, expr.evaluateDouble(new TestVariables()));
	}
	
	public void testStringFunctionsUncompiled() throws Exception {
		ExpressionParser parser = new ExpressionParser();
		
		TreeExpression expr = parser.parse("left('test', 2)");
		assertEquals(Type.STRING, expr.getType());
		assertEquals("te", expr.evaluateString(new TestVariables()));
		
		expr = parser.parse("left(null, 2)");
		assertEquals(Type.STRING, expr.getType());
		assertNull(expr.evaluateString(new TestVariables()));
		
		expr = parser.parse("right('test', 2)");
		assertEquals(Type.STRING, expr.getType());
		assertEquals("st", expr.evaluateString(new TestVariables()));
		
		expr = parser.parse("right(null, 2)");
		assertEquals(Type.STRING, expr.getType());
		assertNull(expr.evaluateString(new TestVariables()));
		
		expr = parser.parse("substr('test', 1, 3)");
		assertEquals(Type.STRING, expr.getType());
		assertEquals("es", expr.evaluateString(new TestVariables()));
		
		expr = parser.parse("substr(null, 1, 3)");
		assertEquals(Type.STRING, expr.getType());
		assertNull(expr.evaluateString(new TestVariables()));
		
		expr = parser.parse("substr('test', -1, 3)");
		assertEquals(Type.STRING, expr.getType());
		assertEquals("tes", expr.evaluateString(new TestVariables()));
		
		expr = parser.parse("substr('test', 1, 10)");
		assertEquals(Type.STRING, expr.getType());
		assertEquals("est", expr.evaluateString(new TestVariables()));
		
		expr = parser.parse("substr('test', 2, 1)");
		assertEquals(Type.STRING, expr.getType());
		assertEquals("", expr.evaluateString(new TestVariables()));
		
		expr = parser.parse("substr('test', 10, 2)");
		assertEquals(Type.STRING, expr.getType());
		assertEquals("", expr.evaluateString(new TestVariables()));
		
		expr = parser.parse("length('test')");
		assertEquals(Type.INT, expr.getType());
		assertEquals(4, expr.evaluateInt(new TestVariables()));

		expr = parser.parse("length(null)");
		assertEquals(Type.INT, expr.getType());
		assertEquals(0, expr.evaluateInt(new TestVariables()));
	}
	
	public void testPrecedenceUncompiled() throws Exception {
		ExpressionParser parser = new ExpressionParser();
		
		TreeExpression expr = parser.parse("1 + 4 * 4");		
		assertEquals(Type.INT, expr.getType());
		assertEquals(17, expr.evaluateInt(new TestVariables()));
		
		expr = parser.parse("4 * 4 + 1");		
		assertEquals(Type.INT, expr.getType());
		assertEquals(17, expr.evaluateInt(new TestVariables()));

		expr = parser.parse("4 + 4 / 4");		
		assertEquals(Type.INT, expr.getType());
		assertEquals(5, expr.evaluateInt(new TestVariables()));
		
		expr = parser.parse("4 / 4 + 4");		
		assertEquals(Type.INT, expr.getType());
		assertEquals(5, expr.evaluateInt(new TestVariables()));

		expr = parser.parse("4 + 4 % 4");		
		assertEquals(Type.INT, expr.getType());
		assertEquals(4, expr.evaluateInt(new TestVariables()));
		
		expr = parser.parse("4 % 4 + 4");		
		assertEquals(Type.INT, expr.getType());
		assertEquals(4, expr.evaluateInt(new TestVariables()));
		
		expr = parser.parse("4 * 4 + 1 < 2 + 4 * 4");		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));
		
		expr = parser.parse("4 * 4 + 1 > 2 + 4 * 4");		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));
		
		expr = parser.parse("4 * 4 + 1 <= 2 + 4 * 4");		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));
		
		expr = parser.parse("4 * 4 + 1 >= 2 + 4 * 4");		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));
		
		expr = parser.parse("1 + 1 == 2 || 5 * 5 == 19 && 4 * 4 == 15");
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));
		
		expr = parser.parse("4 * 4 == 15 && 5 * 5 == 19 || 1 + 1 == 2");		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));

		expr = parser.parse("4 * 4 == 15 && (5 * 5 == 19 || 1 + 1 == 2)");		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));
	}
	
	public void testCompiled() throws Exception {
		ExpressionParser parser = new ExpressionParser();
		ExpressionCompiler compiler = new ExpressionCompiler("com.nginious.http.xsp.expr.TestExpression");
		
		TreeExpression uncompiled = parser.parse("left('test', 2)");
		Expression expr = compiler.compile(uncompiled);		
		assertEquals(Type.STRING, expr.getType());
		assertEquals("te", expr.evaluateString(new TestVariables()));
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));
		
		try {
			assertEquals(0.0d, expr.evaluateDouble(new TestVariables()));
			fail("Must not be possible to convert to double");
		} catch(NumberFormatException e) {}

		try {
			assertEquals(0.0d, expr.evaluateInt(new TestVariables()));
			fail("Must not be possible to convert to int");
		} catch(NumberFormatException e) {}
		
		uncompiled = parser.parse("2.0 * 2.0");
		expr = compiler.compile(uncompiled);
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(4.0, expr.evaluateDouble(new TestVariables()));
		assertEquals(4, expr.evaluateInt(new TestVariables()));
		assertEquals("4.0", expr.evaluateString(new TestVariables()));
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));

		uncompiled = parser.parse("2 * 2");
		expr = compiler.compile(uncompiled);
		assertEquals(Type.INT, expr.getType());
		assertEquals(4, expr.evaluateInt(new TestVariables()));
		assertEquals(4.0, expr.evaluateDouble(new TestVariables()));
		assertEquals("4", expr.evaluateString(new TestVariables()));
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));

		uncompiled = parser.parse("2 == 2");
		expr = compiler.compile(uncompiled);
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));
		assertEquals(1, expr.evaluateInt(new TestVariables()));
		assertEquals(1.0, expr.evaluateDouble(new TestVariables()));
		assertEquals("true", expr.evaluateString(new TestVariables()));
	}
	
	public void testIntegerComparisonCompiled() throws Exception {
		ExpressionParser parser = new ExpressionParser();
		ExpressionCompiler compiler = new ExpressionCompiler("com.nginious.http.xsp.expr.TestExpression");
		
		Expression expr = compiler.compile(parser.parse("2 == 1"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));

		expr = compiler.compile(parser.parse("1 == 1"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));

		expr = compiler.compile(parser.parse("1 == null"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));

		expr = compiler.compile(parser.parse("1 != 1"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));

		expr = compiler.compile(parser.parse("1 != 2"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));
		
		expr = compiler.compile(parser.parse("1 != null"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));
		
		expr = compiler.compile(parser.parse("1 > 2"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));		
		
		expr = compiler.compile(parser.parse("3 > 2"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));		

		expr = compiler.compile(parser.parse("3 > null"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));		

		expr = compiler.compile(parser.parse("1 >= 2"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));		
		
		expr = compiler.compile(parser.parse("2 >= 2"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));		

		expr = compiler.compile(parser.parse("2 >= null"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));		

		expr = compiler.compile(parser.parse("3 < 2"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));		
		
		expr = compiler.compile(parser.parse("1 < 2"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));		
		
		expr = compiler.compile(parser.parse("1 < null"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));		
		
		expr = compiler.compile(parser.parse("3 <= 2"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));		
		
		expr = compiler.compile(parser.parse("2 <= 2"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));		
		
		expr = compiler.compile(parser.parse("2 <= null"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));		
	}
	
	public void testDoubleComparisonCompiled() throws Exception {
		ExpressionParser parser = new ExpressionParser();
		ExpressionCompiler compiler = new ExpressionCompiler("com.nginious.http.xsp.expr.TestExpression");
		
		Expression expr = compiler.compile(parser.parse("2.0 == 1"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));

		expr = compiler.compile(parser.parse("1.0001 == 1.0001"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));

		expr = compiler.compile(parser.parse("1.0001 == null"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));

		expr = compiler.compile(parser.parse("1.0 != 1.0"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));

		expr = compiler.compile(parser.parse("1.0 != 1.01"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));
		
		expr = compiler.compile(parser.parse("1.0 != null"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));
		
		expr = compiler.compile(parser.parse("1.0 > 1.0001"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));		
		
		expr = compiler.compile(parser.parse("2.0001 > 2"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));		

		expr = compiler.compile(parser.parse("2.0001 > null"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));		

		expr = compiler.compile(parser.parse("1 >= 1.001"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));		
		
		expr = compiler.compile(parser.parse("2.0 >= 2.0"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));		

		expr = compiler.compile(parser.parse("2.0 >= null"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));		

		expr = compiler.compile(parser.parse("2.0001 < 2"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));		
		
		expr = compiler.compile(parser.parse("1.0 < 1.0001"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));		
		
		expr = compiler.compile(parser.parse("1.0 < null"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));		
		
		expr = compiler.compile(parser.parse("3.0 <= 2.999"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));		
		
		expr = compiler.compile(parser.parse("2.001 <= 2.001"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));		
		
		expr = compiler.compile(parser.parse("2.001 <= null"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));		
	}
	
	public void testStringComparisonCompiled() throws Exception {
		ExpressionParser parser = new ExpressionParser();
		ExpressionCompiler compiler = new ExpressionCompiler("com.nginious.http.xsp.expr.TestExpression");

		Expression expr = compiler.compile(parser.parse("'test1' == 'test2'"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));

		expr = compiler.compile(parser.parse("'test1' == 'test1'"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));

		expr = compiler.compile(parser.parse("'test1' == null"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));

		expr = compiler.compile(parser.parse("'test1' != 'test1'"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));

		expr = compiler.compile(parser.parse("'test1' != 'test2'"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));
		
		expr = compiler.compile(parser.parse("'test1' != null"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));
		
		try {
			expr = compiler.compile(parser.parse("'test1' > 'test2'"));
			fail("Must not be possible to use more than with strings");
		} catch(ExpressionException e) {}

		try {
			expr = compiler.compile(parser.parse("'test1' >= 'test2'"));
			fail("Must not be possible to use more than or equals with strings");
		} catch(ExpressionException e) {}

		try {
			expr = compiler.compile(parser.parse("'test1' < 'test2'"));
			fail("Must not be possible to use less than with strings");
		} catch(ExpressionException e) {}

		try {
			expr = compiler.compile(parser.parse("'test1' <= 'test2'"));
			fail("Must not be possible to use less than or equals with strings");
		} catch(ExpressionException e) {}
	}
	
	public void testBooleanCompiled() throws Exception {
		ExpressionParser parser = new ExpressionParser();
		ExpressionCompiler compiler = new ExpressionCompiler("com.nginious.http.xsp.expr.TestExpression");
		
		Expression expr = compiler.compile(parser.parse("2 == 1 || 3 == 1"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));

		expr = compiler.compile(parser.parse("1 == 1 || 2 == 1"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));

		expr = compiler.compile(parser.parse("1 == 1 || 2 == 2"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));
		
		expr = compiler.compile(parser.parse("1 == 1 && 3 == 1"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));

		expr = compiler.compile(parser.parse("1 == 1 && 2 == 2"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));

		expr = compiler.compile(parser.parse("1 == 3 && 2 == 2"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));
	}
	
	public void testIntegerArithmeticCompiled() throws Exception {
		ExpressionParser parser = new ExpressionParser();
		ExpressionCompiler compiler = new ExpressionCompiler("com.nginious.http.xsp.expr.TestExpression");
		
		Expression expr = compiler.compile(parser.parse("2 + 1"));		
		assertEquals(Type.INT, expr.getType());
		assertEquals(3, expr.evaluateInt(new TestVariables()));

		expr = compiler.compile(parser.parse("2 + null"));		
		assertEquals(Type.INT, expr.getType());
		assertEquals(2, expr.evaluateInt(new TestVariables()));

		expr = compiler.compile(parser.parse("2 - 1"));		
		assertEquals(Type.INT, expr.getType());
		assertEquals(1, expr.evaluateInt(new TestVariables()));

		expr = compiler.compile(parser.parse("2 - null"));		
		assertEquals(Type.INT, expr.getType());
		assertEquals(2, expr.evaluateInt(new TestVariables()));

		expr = compiler.compile(parser.parse("2 * 2"));
		assertEquals(Type.INT, expr.getType());
		assertEquals(4, expr.evaluateInt(new TestVariables()));

		expr = compiler.compile(parser.parse("2 * null"));
		assertEquals(Type.INT, expr.getType());
		assertEquals(0, expr.evaluateInt(new TestVariables()));

		expr = compiler.compile(parser.parse("9 / 3"));
		assertEquals(Type.INT, expr.getType());
		assertEquals(3, expr.evaluateInt(new TestVariables()));
		
		expr = compiler.compile(parser.parse("null / 3"));
		assertEquals(Type.INT, expr.getType());
		assertEquals(0, expr.evaluateInt(new TestVariables()));
		
		expr = compiler.compile(parser.parse("9 % 3"));
		assertEquals(Type.INT, expr.getType());
		assertEquals(0, expr.evaluateInt(new TestVariables()));		

		expr = compiler.compile(parser.parse("null % 3"));
		assertEquals(Type.INT, expr.getType());
		assertEquals(0, expr.evaluateInt(new TestVariables()));		

		expr = compiler.compile(parser.parse("10 % 3"));
		assertEquals(Type.INT, expr.getType());
		assertEquals(1, expr.evaluateInt(new TestVariables()));		
	}
	
	public void testDoubleArithmeticCompiled() throws Exception {
		ExpressionParser parser = new ExpressionParser();
		ExpressionCompiler compiler = new ExpressionCompiler("com.nginious.http.xsp.expr.TestExpression");
		
		Expression expr = compiler.compile(parser.parse("2.1 + 1"));		
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(3.1d, expr.evaluateDouble(new TestVariables()));

		expr = compiler.compile(parser.parse("2.1 + null"));		
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(2.1d, expr.evaluateDouble(new TestVariables()));

		expr = compiler.compile(parser.parse("2 - 1.0"));		
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(1.0d, expr.evaluateDouble(new TestVariables()));

		expr = compiler.compile(parser.parse("2.0 - null"));		
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(2.0d, expr.evaluateDouble(new TestVariables()));

		expr = compiler.compile(parser.parse("2.1 * 2"));		
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(4.2d, expr.evaluateDouble(new TestVariables()));

		expr = compiler.compile(parser.parse("2.1 * null"));		
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(0.0d, expr.evaluateDouble(new TestVariables()));

		expr = compiler.compile(parser.parse("9.0 / 3"));		
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(3.0d, expr.evaluateDouble(new TestVariables()));
		
		expr = compiler.compile(parser.parse("null / 3.0"));		
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(0.0d, expr.evaluateDouble(new TestVariables()));
		
		expr = compiler.compile(parser.parse("9.0 % 3"));		
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(0.0d, expr.evaluateDouble(new TestVariables()));		

		expr = compiler.compile(parser.parse("null % 3.0"));		
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(0.0d, expr.evaluateDouble(new TestVariables()));		

		expr = compiler.compile(parser.parse("10 % 3.0"));
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(1.0d, expr.evaluateDouble(new TestVariables()));		
	}
	
	public void testIntegerFunctionsCompiled() throws Exception {
		ExpressionParser parser = new ExpressionParser();
		ExpressionCompiler compiler = new ExpressionCompiler("com.nginious.http.xsp.expr.TestExpression");
		
		Expression expr = compiler.compile(parser.parse("abs(-1)"));
		assertEquals(Type.INT, expr.getType());
		assertEquals(1, expr.evaluateInt(new TestVariables()));
		
		expr = compiler.compile(parser.parse("min(2, 5)"));
		assertEquals(Type.INT, expr.getType());
		assertEquals(2, expr.evaluateInt(new TestVariables()));
		
		expr = compiler.compile(parser.parse("max(2, 5)"));
		assertEquals(Type.INT, expr.getType());
		assertEquals(5, expr.evaluateInt(new TestVariables()));		
	}
	
	public void testDoubleFunctionsCompiled() throws Exception {
		ExpressionParser parser = new ExpressionParser();
		ExpressionCompiler compiler = new ExpressionCompiler("com.nginious.http.xsp.expr.TestExpression");
		
		Expression expr = compiler.compile(parser.parse("abs(-0.51)"));
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(0.51d, expr.evaluateDouble(new TestVariables()));
		
		expr = compiler.compile(parser.parse("abs(null)"));
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(0.0d, expr.evaluateDouble(new TestVariables()));
		
		expr = compiler.compile(parser.parse("acos(1.0)"));
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(0.0d, expr.evaluateDouble(new TestVariables()));
		
		expr = compiler.compile(parser.parse("asin(0.0)"));
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(0.0d, expr.evaluateDouble(new TestVariables()));
		
		expr = compiler.compile(parser.parse("atan(0.0)"));
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(0.0d, expr.evaluateDouble(new TestVariables()));
		
		expr = compiler.compile(parser.parse("ceil(1.21)"));
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(2.0d, expr.evaluateDouble(new TestVariables()));
		
		expr = compiler.compile(parser.parse("cos(0.0)"));
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(1.0d, expr.evaluateDouble(new TestVariables()));
		
		expr = compiler.compile(parser.parse("exp(0.0)"));
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(1.0d, expr.evaluateDouble(new TestVariables()));
		
		expr = compiler.compile(parser.parse("floor(1.21)"));
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(1.0d, expr.evaluateDouble(new TestVariables()));
		
		expr = compiler.compile(parser.parse("log(1.0)"));
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(0.0d, expr.evaluateDouble(new TestVariables()));
		
		expr = compiler.compile(parser.parse("min(1.0, 2.0)"));
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(1.0d, expr.evaluateDouble(new TestVariables()));

		expr = compiler.compile(parser.parse("max(1.0, 2.0)"));
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(2.0d, expr.evaluateDouble(new TestVariables()));

		expr = compiler.compile(parser.parse("pow(2.0, 2.0)"));
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(4.0d, expr.evaluateDouble(new TestVariables()));

		expr = compiler.compile(parser.parse("rint(2.123)"));
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(2.0d, expr.evaluateDouble(new TestVariables()));

		expr = compiler.compile(parser.parse("sin(0.0)"));
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(0.0d, expr.evaluateDouble(new TestVariables()));

		expr = compiler.compile(parser.parse("sqrt(9.0)"));
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(3.0d, expr.evaluateDouble(new TestVariables()));

		expr = compiler.compile(parser.parse("tan(0.0)"));
		assertEquals(Type.DOUBLE, expr.getType());
		assertEquals(0.0d, expr.evaluateDouble(new TestVariables()));
	}
	
	public void testStringFunctionsCompiled() throws Exception {
		ExpressionParser parser = new ExpressionParser();
		ExpressionCompiler compiler = new ExpressionCompiler("com.nginious.http.xsp.expr.TestExpression");
		
		Expression expr = compiler.compile(parser.parse("left('test', 2)"));
		assertEquals(Type.STRING, expr.getType());
		assertEquals("te", expr.evaluateString(new TestVariables()));
		
		expr = compiler.compile(parser.parse("left(null, 2)"));
		assertEquals(Type.STRING, expr.getType());
		assertNull(expr.evaluateString(new TestVariables()));
		
		expr = compiler.compile(parser.parse("right('test', 2)"));
		assertEquals(Type.STRING, expr.getType());
		assertEquals("st", expr.evaluateString(new TestVariables()));
		
		expr = compiler.compile(parser.parse("right(null, 2)"));
		assertEquals(Type.STRING, expr.getType());
		assertNull(expr.evaluateString(new TestVariables()));
		
		expr = compiler.compile(parser.parse("substr('test', 1, 3)"));
		assertEquals(Type.STRING, expr.getType());
		assertEquals("es", expr.evaluateString(new TestVariables()));
		
		expr = compiler.compile(parser.parse("substr(null, 1, 3)"));
		assertEquals(Type.STRING, expr.getType());
		assertNull(expr.evaluateString(new TestVariables()));
		
		expr = compiler.compile(parser.parse("substr('test', -1, 3)"));
		assertEquals(Type.STRING, expr.getType());
		assertEquals("tes", expr.evaluateString(new TestVariables()));
		
		expr = compiler.compile(parser.parse("substr('test', 1, 10)"));
		assertEquals(Type.STRING, expr.getType());
		assertEquals("est", expr.evaluateString(new TestVariables()));
		
		expr = compiler.compile(parser.parse("substr('test', 2, 1)"));
		assertEquals(Type.STRING, expr.getType());
		assertEquals("", expr.evaluateString(new TestVariables()));
		
		expr = compiler.compile(parser.parse("substr('test', 10, 2)"));
		assertEquals(Type.STRING, expr.getType());
		assertEquals("", expr.evaluateString(new TestVariables()));
		
		expr = compiler.compile(parser.parse("length('test')"));
		assertEquals(Type.INT, expr.getType());
		assertEquals(4, expr.evaluateInt(new TestVariables()));
	}
	
	public void testPrecedenceCompiled() throws Exception {
		ExpressionParser parser = new ExpressionParser();
		ExpressionCompiler compiler = new ExpressionCompiler("com.nginious.http.xsp.expr.TestExpression");
		
		Expression expr = compiler.compile(parser.parse("1 + 4 * 4"));		
		assertEquals(Type.INT, expr.getType());
		assertEquals(17, expr.evaluateInt(new TestVariables()));
		
		expr = compiler.compile(parser.parse("4 * 4 + 1"));		
		assertEquals(Type.INT, expr.getType());
		assertEquals(17, expr.evaluateInt(new TestVariables()));

		expr = compiler.compile(parser.parse("4 + 4 / 4"));		
		assertEquals(Type.INT, expr.getType());
		assertEquals(5, expr.evaluateInt(new TestVariables()));
		
		expr = compiler.compile(parser.parse("4 / 4 + 4"));		
		assertEquals(Type.INT, expr.getType());
		assertEquals(5, expr.evaluateInt(new TestVariables()));

		expr = compiler.compile(parser.parse("4 + 4 % 4"));		
		assertEquals(Type.INT, expr.getType());
		assertEquals(4, expr.evaluateInt(new TestVariables()));
		
		expr = compiler.compile(parser.parse("4 % 4 + 4"));		
		assertEquals(Type.INT, expr.getType());
		assertEquals(4, expr.evaluateInt(new TestVariables()));
		
		expr = compiler.compile(parser.parse("4 * 4 + 1 < 2 + 4 * 4"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));
		
		expr = compiler.compile(parser.parse("4 * 4 + 1 > 2 + 4 * 4"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));
		
		expr = compiler.compile(parser.parse("4 * 4 + 1 <= 2 + 4 * 4"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));
		
		expr = compiler.compile(parser.parse("4 * 4 + 1 >= 2 + 4 * 4"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));
		
		expr = compiler.compile(parser.parse("1 + 1 == 2 || 5 * 5 == 19 && 4 * 4 == 15"));
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));

		expr = parser.parse("4 * 4 == 15 && 5 * 5 == 19 || 1 + 1 == 2");		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(true, expr.evaluateBoolean(new TestVariables()));

		expr = parser.parse("4 * 4 == 15 && (5 * 5 == 19 || 1 + 1 == 2)");		
		assertEquals(Type.BOOLEAN, expr.getType());
		assertEquals(false, expr.evaluateBoolean(new TestVariables()));
	}
	
	public void testAttributeCompiled() throws Exception {
		ExpressionParser parser = new ExpressionParser();
		ExpressionCompiler compiler = new ExpressionCompiler("com.nginious.http.xsp.expr.TestExpression");
		
		Expression expr = compiler.compile(parser.parse("test == 1"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		TestVariables vars = new TestVariables();
		vars.setVariable("test", "1");
		assertEquals(true, expr.evaluateBoolean(vars));
		
		expr = compiler.compile(parser.parse("test == 1"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		vars = new TestVariables();
		vars.setVariable("test", "2");
		assertEquals(false, expr.evaluateBoolean(vars));
		
		expr = compiler.compile(parser.parse("test == 1"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		vars = new TestVariables();
		assertEquals(false, expr.evaluateBoolean(vars));
		
		expr = compiler.compile(parser.parse("test != 1"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		vars = new TestVariables();
		vars.setVariable("test", "2");
		assertEquals(true, expr.evaluateBoolean(vars));
		
		expr = compiler.compile(parser.parse("test != 1"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		vars = new TestVariables();
		vars.setVariable("test", "1");
		assertEquals(false, expr.evaluateBoolean(vars));

		expr = compiler.compile(parser.parse("test != 1"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		vars = new TestVariables();
		assertEquals(true, expr.evaluateBoolean(vars));

		expr = compiler.compile(parser.parse("test > 1"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		vars = new TestVariables();
		vars.setVariable("test", "2");
		assertEquals(true, expr.evaluateBoolean(vars));
		
		expr = compiler.compile(parser.parse("test >= 1"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		vars = new TestVariables();
		vars.setVariable("test", "2");
		assertEquals(true, expr.evaluateBoolean(vars));
		
		expr = compiler.compile(parser.parse("test < 5"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		vars = new TestVariables();
		vars.setVariable("test", "2");
		assertEquals(true, expr.evaluateBoolean(vars));		
		
		expr = compiler.compile(parser.parse("test <= 5"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		vars = new TestVariables();
		vars.setVariable("test", "5");
		assertEquals(true, expr.evaluateBoolean(vars));
		
		expr = compiler.compile(parser.parse("test + 5"));		
		assertEquals(Type.INT, expr.getType());
		vars = new TestVariables();
		vars.setVariable("test", "5");
		assertEquals(10, expr.evaluateInt(vars));

		expr = compiler.compile(parser.parse("test + 5"));		
		assertEquals(Type.INT, expr.getType());
		vars = new TestVariables();
		assertEquals(5, expr.evaluateInt(vars));

		expr = compiler.compile(parser.parse("min(test,5)"));		
		assertEquals(Type.INT, expr.getType());
		vars = new TestVariables();
		vars.setVariable("test", "2");
		assertEquals(2, expr.evaluateInt(vars));
		
		expr = compiler.compile(parser.parse("max(test,5)"));		
		assertEquals(Type.INT, expr.getType());
		vars = new TestVariables();
		assertEquals(5, expr.evaluateInt(vars));
		
		expr = compiler.compile(parser.parse("test + 5.0"));		
		assertEquals(Type.DOUBLE, expr.getType());
		vars = new TestVariables();
		vars.setVariable("test", "5");
		assertEquals(10.0, expr.evaluateDouble(vars));		

		expr = compiler.compile(parser.parse("test + 5.0"));		
		assertEquals(Type.DOUBLE, expr.getType());
		vars = new TestVariables();
		assertEquals(5.0, expr.evaluateDouble(vars));		
		
		expr = compiler.compile(parser.parse("left(test, 2)"));
		assertEquals(Type.STRING, expr.getType());
		vars = new TestVariables();
		vars.setVariable("test", "test");
		assertEquals("te", expr.evaluateString(vars));

		expr = compiler.compile(parser.parse("left(test, 2)"));
		assertEquals(Type.STRING, expr.getType());
		vars = new TestVariables();
		assertNull(expr.evaluateString(vars));

		expr = compiler.compile(parser.parse("test == 'åäö'"));
		assertEquals(Type.BOOLEAN, expr.getType());
		vars = new TestVariables();
		vars.setVariable("test", "åäö");
		assertEquals(true, expr.evaluateBoolean(vars));

		assertEquals(null, null);
	}
	
	public void testBeanCompiled() throws Exception {
		ExpressionParser parser = new ExpressionParser();
		ExpressionCompiler compiler = new ExpressionCompiler("com.nginious.http.xsp.expr.TestExpression");
		
		TestBean bean = new TestBean();
		bean.setTest1("test");
		bean.setTest2(1);
		bean.setTest3(1.0d);
		
		Expression expr = compiler.compile(parser.parse("test.test2 == 1"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		TestVariables vars = new TestVariables();
		vars.setVariable("test", bean);
		assertEquals(true, expr.evaluateBoolean(vars));
		
		expr = compiler.compile(parser.parse("test.test2 == 2"));
		assertEquals(Type.BOOLEAN, expr.getType());
		vars = new TestVariables();
		vars.setVariable("test", bean);
		assertEquals(false, expr.evaluateBoolean(vars));
		
		expr = compiler.compile(parser.parse("test.test2 != 2"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		vars = new TestVariables();
		vars.setVariable("test", bean);
		assertEquals(true, expr.evaluateBoolean(vars));
		
		expr = compiler.compile(parser.parse("test.test2 > 0"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		vars = new TestVariables();
		vars.setVariable("test", bean);
		assertEquals(true, expr.evaluateBoolean(vars));
		
		expr = compiler.compile(parser.parse("test.test2 >= 1"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		vars = new TestVariables();
		vars.setVariable("test", bean);
		assertEquals(true, expr.evaluateBoolean(vars));
		
		expr = compiler.compile(parser.parse("test.test2 < 5"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		vars = new TestVariables();
		vars.setVariable("test", bean);
		assertEquals(true, expr.evaluateBoolean(vars));		
		
		expr = compiler.compile(parser.parse("test.test2 <= 5"));		
		assertEquals(Type.BOOLEAN, expr.getType());
		vars = new TestVariables();
		vars.setVariable("test", bean);
		assertEquals(true, expr.evaluateBoolean(vars));
		
		expr = compiler.compile(parser.parse("test.test2 + 5"));		
		assertEquals(Type.INT, expr.getType());
		vars = new TestVariables();
		vars.setVariable("test", bean);
		assertEquals(6, expr.evaluateInt(vars));

		expr = compiler.compile(parser.parse("min(test.test2,5)"));		
		assertEquals(Type.INT, expr.getType());
		vars = new TestVariables();
		vars.setVariable("test", bean);
		assertEquals(1, expr.evaluateInt(vars));
		
		expr = compiler.compile(parser.parse("test.test3 + 5.0"));		
		assertEquals(Type.DOUBLE, expr.getType());
		vars = new TestVariables();
		vars.setVariable("test", bean);
		assertEquals(6.0, expr.evaluateDouble(vars));		

		expr = compiler.compile(parser.parse("left(test.test1, 2)"));
		assertEquals(Type.STRING, expr.getType());
		vars = new TestVariables();
		vars.setVariable("test", bean);
		assertEquals("te", expr.evaluateString(vars));

		expr = compiler.compile(parser.parse("left(test.test1, 2)"));
		assertEquals(Type.STRING, expr.getType());
		bean.setTest1(null);
		vars = new TestVariables();
		vars.setVariable("test", bean);
		assertNull(expr.evaluateString(vars));
	}
	
	public static Test suite() {
		return new TestSuite(ExpressionParserTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
