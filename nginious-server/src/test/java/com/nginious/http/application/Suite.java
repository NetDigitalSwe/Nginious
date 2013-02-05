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

package com.nginious.http.application;

import junit.framework.Test;
import junit.framework.TestSuite;

public class Suite extends TestSuite {

    public static Test suite() {
    	TestSuite suite = new TestSuite();
    	
    	suite.addTest(new Http10StaticContentTestCase("testStaticContent"));
    	suite.addTest(new Http10StaticContentTestCase("testInvalidMethod"));

    	suite.addTest(new Http10StaticContentTestCase("testIfModifiedSince1"));
    	suite.addTest(new Http10StaticContentTestCase("testIfModifiedSince2"));
    	suite.addTest(new Http10StaticContentTestCase("testIfModifiedSince3"));

    	suite.addTest(new Http11StaticContentTestCase("testStaticContent1"));
    	suite.addTest(new Http11StaticContentTestCase("testStaticContent2"));
    	suite.addTest(new Http11StaticContentTestCase("testInvalidMethod"));
    	
    	suite.addTest(new Http11StaticContentTestCase("testAccept1"));
    	suite.addTest(new Http11StaticContentTestCase("testAccept2"));
    	
    	suite.addTest(new Http11StaticContentTestCase("testRange1"));
    	suite.addTest(new Http11StaticContentTestCase("testRange2"));
    	suite.addTest(new Http11StaticContentTestCase("testRange3"));
    	suite.addTest(new Http11StaticContentTestCase("testRange4"));
    	suite.addTest(new Http11StaticContentTestCase("testRange5"));
    	suite.addTest(new Http11StaticContentTestCase("testRange6"));
    	suite.addTest(new Http11StaticContentTestCase("testRange7"));
    	
    	suite.addTest(new Http11StaticContentTestCase("testIfRange1"));
    	suite.addTest(new Http11StaticContentTestCase("testIfRange2"));
    	
    	suite.addTest(new Http11StaticContentTestCase("testAcceptEncoding1"));
    	suite.addTest(new Http11StaticContentTestCase("testAcceptEncoding2"));

    	suite.addTest(new Http11StaticContentTestCase("testIfModifiedSince1"));
    	suite.addTest(new Http11StaticContentTestCase("testIfModifiedSince2"));
    	suite.addTest(new Http11StaticContentTestCase("testIfModifiedSince3"));
    	
    	suite.addTest(new Http11StaticContentTestCase("testIfUnmodifiedSince1"));
    	suite.addTest(new Http11StaticContentTestCase("testIfUnmodifiedSince2"));

    	suite.addTest(new Http11StaticContentTestCase("testIfMatch1"));
    	suite.addTest(new Http11StaticContentTestCase("testIfMatch2"));
    	suite.addTest(new Http11StaticContentTestCase("testIfMatch3"));
    	
    	suite.addTest(new Http11StaticContentTestCase("testIfNoneMatch1"));
    	suite.addTest(new Http11StaticContentTestCase("testIfNoneMatch2"));
    	
    	suite.addTest(new Http11StaticContentTestCase("testStaticContentOptions"));
    	
    	suite.addTest(new Http11MethodsTestCase("testControllerOptions"));
    	suite.addTest(new Http11MethodsTestCase("testDefaultGet"));
    	suite.addTest(new Http11MethodsTestCase("testDefaultPost"));
    	suite.addTest(new Http11MethodsTestCase("testDefaultPut"));
    	suite.addTest(new Http11MethodsTestCase("testDefaultDelete"));
    	
    	suite.addTest(new Http11ControllerChainTestCase("testControllerChain1"));
    	suite.addTest(new Http11ControllerChainTestCase("testControllerChain2"));
    	
    	suite.addTest(new Http11DispatchTestCase("testHttpGetDispatch"));
    	suite.addTest(new Http11DispatchTestCase("testHttpPostDispatch"));
    	suite.addTest(new Http11DispatchTestCase("testHttpPutDispatch"));
    	suite.addTest(new Http11DispatchTestCase("testHttpDeleteDispatch"));
    	suite.addTest(new Http11DispatchTestCase("testHttpNotFoundDispatch"));
    	
    	suite.addTest(new Http11UnpackedTestCase("testHeadRequest"));
    	suite.addTest(new Http11UnpackedTestCase("testGetRequest"));
    	suite.addTest(new Http11UnpackedTestCase("testPostRequest"));
    	suite.addTest(new Http11UnpackedTestCase("testPutRequest"));
    	suite.addTest(new Http11UnpackedTestCase("testDeleteRequest"));
    	
    	suite.addTest(new ApplicationClassLoaderTestCase("testCrossReference"));
    	suite.addTest(new ApplicationClassLoaderTestCase("testClassLoading"));
    	suite.addTest(new ApplicationClassLoaderTestCase("testJarLoading"));
    	suite.addTest(new ApplicationClassLoaderTestCase("testJarResourceLoading"));
    	suite.addTest(new ApplicationClassLoaderTestCase("testClassResourceLoading"));
    	
    	suite.addTest(new Http11AdminTestCase("testPublish"));
    	suite.addTest(new Http11AdminTestCase("testRepublish"));
    	
    	suite.addTest(new Http11ReloadableControllerTestCase("testReloadableController"));
    	suite.addTest(new NullWebAppsDirTestCase("testNullWebAppsDir"));
    	
    	return suite;
    }
    
    public static void main(String[] argv) {
    	junit.textui.TestRunner.run(suite());
    }
}
