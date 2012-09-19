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
import junit.framework.TestSuite;

public class Suite extends TestSuite {

    public static Test suite() {
    	TestSuite suite = new TestSuite();
    	    	
    	suite.addTest(new XspParserTestCase("testXspParser1"));
    	suite.addTest(new XspParserTestCase("testXspParser2"));
    	suite.addTest(new XspParserTestCase("testBadAttribute"));
    	suite.addTest(new XspParserTestCase("testBadEndTag"));
    	
    	suite.addTest(new XspCompilerTestCase("testXspSimpleCompile"));
    	suite.addTest(new XspCompilerTestCase("testXspTagsCompile"));
    	suite.addTest(new XspCompilerTestCase("testXspOutput"));
    	suite.addTest(new XspCompilerTestCase("testXspTags"));
    	suite.addTest(new XspCompilerTestCase("testXspCharacter"));
    	suite.addTest(new XspCompilerTestCase("testXspAnnotations"));
    	suite.addTest(new XspCompilerTestCase("testXspDateTag"));
    	suite.addTest(new XspCompilerTestCase("testXspNumberTag"));
    	suite.addTest(new XspCompilerTestCase("testXspMessageTag"));
    	suite.addTest(new XspCompilerTestCase("testXspBadExpression"));
    	suite.addTest(new XspCompilerTestCase("testXspMissingTestExpression"));
    	
    	suite.addTest(new XspServiceTestCase("testXspService"));
    	suite.addTest(new XspServiceTestCase("testUncompiledXspService"));
    	
    	suite.addTest(new ExpressionParserTestCase("testIntegerComparisonUncompiled"));
    	suite.addTest(new ExpressionParserTestCase("testDoubleComparisonUncompiled"));
    	suite.addTest(new ExpressionParserTestCase("testStringComparisonUncompiled"));
    	suite.addTest(new ExpressionParserTestCase("testBooleanUncompiled"));
    	suite.addTest(new ExpressionParserTestCase("testIntegerArithmeticUncompiled"));
    	suite.addTest(new ExpressionParserTestCase("testDoubleArithmeticUncompiled"));
    	suite.addTest(new ExpressionParserTestCase("testIntegerFunctionsUncompiled"));
    	suite.addTest(new ExpressionParserTestCase("testDoubleFunctionsUncompiled"));
     	suite.addTest(new ExpressionParserTestCase("testStringFunctionsUncompiled"));
    	suite.addTest(new ExpressionParserTestCase("testPrecedenceUncompiled"));

     	suite.addTest(new ExpressionParserTestCase("testCompiled"));
     	suite.addTest(new ExpressionParserTestCase("testIntegerComparisonCompiled"));
    	suite.addTest(new ExpressionParserTestCase("testDoubleComparisonCompiled"));
    	suite.addTest(new ExpressionParserTestCase("testStringComparisonCompiled"));
    	suite.addTest(new ExpressionParserTestCase("testBooleanCompiled"));
    	suite.addTest(new ExpressionParserTestCase("testIntegerArithmeticCompiled"));
    	suite.addTest(new ExpressionParserTestCase("testDoubleArithmeticCompiled"));
    	suite.addTest(new ExpressionParserTestCase("testIntegerFunctionsCompiled"));
    	suite.addTest(new ExpressionParserTestCase("testDoubleFunctionsCompiled"));
    	suite.addTest(new ExpressionParserTestCase("testStringFunctionsCompiled"));
    	suite.addTest(new ExpressionParserTestCase("testPrecedenceCompiled"));
    	suite.addTest(new ExpressionParserTestCase("testAttributeCompiled"));
    	suite.addTest(new ExpressionParserTestCase("testBeanCompiled"));
    	
    	return suite;
    }
    
    public static void main(String[] argv) {
    	junit.textui.TestRunner.run(suite());
    }
}
