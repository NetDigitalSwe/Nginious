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

import junit.framework.Test;
import junit.framework.TestSuite;

public class Suite extends TestSuite {

    public static Test suite() {
    	TestSuite suite = new TestSuite();
    	
    	suite.addTest(new ByteRangeTestCase("testByteRange"));
    	suite.addTest(new DateTestCase("testDate"));
    	suite.addTest(new DigestAuthenticationTestCase("testChallenge"));
    	suite.addTest(new DigestAuthenticationTestCase("testResponse"));
    	suite.addTest(new DigestAuthenticationTestCase("testCasing"));
    	suite.addTest(new DigestAuthenticationTestCase("testMissingFields"));
    	suite.addTest(new HeaderTestCase("testHeader"));
    	suite.addTest(new HeaderTestCase("testAuthorization"));
    	suite.addTest(new HttpCookieTestCase("testCookie"));
    	suite.addTest(new MimeTypesTestCase("testMimeTypes"));
    	suite.addTest(new URITestCase("testURI"));
    	
    	suite.addTest(new Http09MethodsTestCase("testGetRequest"));
    	suite.addTest(new Http09MethodsTestCase("testPostRequest"));
    	
    	suite.addTest(new Http10MethodsTestCase("testHeadRequest"));
    	suite.addTest(new Http10MethodsTestCase("testGetRequest"));
    	suite.addTest(new Http10MethodsTestCase("testPostRequest"));
    	suite.addTest(new Http10MethodsTestCase("testPutRequest"));
    	suite.addTest(new Http10MethodsTestCase("testDeleteRequest"));
    	
    	suite.addTest(new Http11MethodsTestCase("testHeadRequest"));
    	suite.addTest(new Http11MethodsTestCase("testGetRequest"));
    	suite.addTest(new Http11MethodsTestCase("testPostRequest"));
    	suite.addTest(new Http11MethodsTestCase("testPutRequest"));
    	suite.addTest(new Http11MethodsTestCase("testDeleteRequest"));
    	suite.addTest(new Http11MethodsTestCase("testWildcardOptionsRequest"));
    	suite.addTest(new Http11MethodsTestCase("testTrace"));
    	
    	suite.addTest(new Http11CharsetTestCase("testEncodings"));
    	suite.addTest(new Http11CharsetTestCase("testDefaultCharset"));
    	suite.addTest(new Http11CharsetTestCase("testBadEncoding"));
    	
    	suite.addTest(new Http11KeepAliveTestCase("testKeepAlive"));
    	
    	suite.addTest(new Http11ChunkedTestCase("testChunkedClientEncoding"));
    	suite.addTest(new Http11ChunkedTestCase("testFragmentedChunkedClientEncoding"));
    	suite.addTest(new Http11ChunkedTestCase("testEmptyChunkedClientEncoding"));
    	suite.addTest(new Http11ChunkedTestCase("testChunkedServerEncoding"));
    	
    	suite.addTest(new Http11URITestCase("testGetParameters"));
    	suite.addTest(new Http11URITestCase("testPostParameters"));
    	suite.addTest(new Http11URITestCase("testGetParametersEncoding"));
    	suite.addTest(new Http11URITestCase("testPostParametersEncoding"));
    	suite.addTest(new Http11URITestCase("testAbsoluteURI"));
    	suite.addTest(new Http11URITestCase("testAbsoluteBadHostnameURI"));
    	
    	suite.addTest(new Http10ContentTestCase("testContent"));
    	suite.addTest(new Http10ContentTestCase("testTooLargeContent"));
    	suite.addTest(new Http10ContentTestCase("testGetNoContent"));
    	suite.addTest(new Http10ContentTestCase("testPostNoContent"));
    	suite.addTest(new Http10ContentTestCase("testPostNoContentLength"));
    	
    	suite.addTest(new Http11ContentTestCase("testContent"));
    	suite.addTest(new Http11ContentTestCase("testTooLargeContent"));
    	suite.addTest(new Http11ContentTestCase("testGetNoContent"));
    	suite.addTest(new Http11ContentTestCase("testPostNoContent"));
    	suite.addTest(new Http11ContentTestCase("testTooLongContentLength"));
    	suite.addTest(new Http11ContentTestCase("testTooShortContentLength"));
    	suite.addTest(new Http11ContentTestCase("testMissingContentLength"));
    	
    	suite.addTest(new Http11MalformedTestCase("testMalformedMethod"));
    	suite.addTest(new Http11MalformedTestCase("testMalformedMethod2"));
    	suite.addTest(new Http11MalformedTestCase("testMalformedURIDelimiter"));
    	suite.addTest(new Http11MalformedTestCase("testFragmentedRequest"));
    	suite.addTest(new Http11MalformedTestCase("testMultiFragmentedRequest"));
    	
    	suite.addTest(new Http11MiscTestCase("testCaseInsensitiveHeaders"));
    	suite.addTest(new Http11MiscTestCase("testInvalidHttpVersion"));
    	suite.addTest(new Http11MiscTestCase("testAcceptLanguage1"));
    	suite.addTest(new Http11MiscTestCase("testAcceptLanguage2"));
    	suite.addTest(new Http11MiscTestCase("testStatusMessage"));
    	suite.addTest(new Http11MiscTestCase("testExpectationFailed"));
    	suite.addTest(new Http11MiscTestCase("testExpect100Continue"));
    	suite.addTest(new Http11MiscTestCase("testNoLineFeed"));
    	
    	suite.addTest(new Http11CookieTestCase("testCookie1"));
    	suite.addTest(new Http11CookieTestCase("testCookie2"));
    	
    	suite.addTest(new Http11AsyncResponseTestCase("testAsyncResponse1"));
    	suite.addTest(new Http11AsyncResponseTestCase("testAsyncResponse2"));
    	
    	suite.addTest(new Http11AccessLogTestCase("testHeadRequest"));
    	suite.addTest(new Http11AccessLogTestCase("testGetRequest"));
    	suite.addTest(new Http11AccessLogTestCase("testPostRequest"));
    	suite.addTest(new Http11AccessLogTestCase("testPutRequest"));
    	suite.addTest(new Http11AccessLogTestCase("testDeleteRequest"));
    	suite.addTest(new Http11AccessLogTestCase("testWildcardOptionsRequest"));
    	suite.addTest(new Http11AccessLogTestCase("testTrace"));
    	suite.addTest(new Http11AccessLogTestCase("testBadEncoding"));
    	
    	suite.addTest(new Http11TimeoutTestCase("testInitialTimeout"));
    	suite.addTest(new Http11TimeoutTestCase("testSubsequentTimeout"));
    	suite.addTest(new Http11TimeoutTestCase("testMultipleRequests"));
    	
    	suite.addTest(new Http11StressTestCase("testResource"));
    	suite.addTest(new Http11StressTestCase("testStress"));
    	
    	return suite;
    }
    
    public static void main(String[] argv) {
    	junit.textui.TestRunner.run(suite());
    }
}
