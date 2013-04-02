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

package com.nginious.http.serialize;

import junit.framework.Test;
import junit.framework.TestSuite;

public class Suite extends TestSuite {

    public static Test suite() {
    	TestSuite suite = new TestSuite();
    	
    	suite.addTest(new SerializationClassLoaderTestCase("testSerialization"));
    	
    	suite.addTest(new RestControllerSerializationTestCase("testRestControllerSerialization"));
    	suite.addTest(new RestControllerSerializationTestCase("testMissingContentTypeSerialization"));
    	suite.addTest(new RestControllerSerializationTestCase("testInvalidContentTypeSerialization"));
    	suite.addTest(new RestControllerSerializationTestCase("testMissingAcceptSerialization"));
    	suite.addTest(new RestControllerSerializationTestCase("testInvalidAcceptSerialization"));
    	suite.addTest(new RestControllerSerializationTestCase("testVoidServiceSerialization"));
    	suite.addTest(new TestAsyncResponseTestCase("testAsyncResponse"));
    	
    	suite.addTest(new SerializerTestCase("testInvalidAcceptSerializer"));
    	suite.addTest(new SerializerTestCase("testMissingAcceptSerializer"));
    	
    	suite.addTest(new JsonSerializerTestCase("testJsonSerializer"));
    	suite.addTest(new JsonSerializerTestCase("testEmptyJsonSerializer"));
    	
    	suite.addTest(new XmlSerializerTestCase("testXmlSerializer"));
    	suite.addTest(new XmlSerializerTestCase("testEmptyXmlSerializer"));

    	suite.addTest(new SerializerAcceptTypeTestCase("testSerializerAcceptType"));
    	
    	suite.addTest(new DeserializerTestCase("testInvalidContentTypeDeserialization"));
    	suite.addTest(new DeserializerTestCase("testMissingContentTypeDeserialization"));
    	
    	suite.addTest(new JsonDeserializerTestCase("testJsonDeserialization"));
    	suite.addTest(new JsonDeserializerTestCase("testJsonDeserializationBadValues"));
    	suite.addTest(new JsonDeserializerTestCase("testJsonDeserializationNullValues"));
    	suite.addTest(new JsonDeserializerTestCase("testJsonDeserializationFactory"));
    	suite.addTest(new JsonDeserializerTestCase("testJsonDeserializationAnnotations"));
    	
    	suite.addTest(new XmlDeserializerTestCase("testXmlDeserialization"));
    	suite.addTest(new XmlDeserializerTestCase("testXmlDeserializationBadValues"));
    	suite.addTest(new XmlDeserializerTestCase("testXmlDeserializationNullValues"));
    	suite.addTest(new XmlDeserializerTestCase("testXmlDeserializationFactory"));
    	suite.addTest(new XmlDeserializerTestCase("testXmlDeserializationAnnotations"));
    	
    	suite.addTest(new QueryDeserializerTestCase("testQueryDeserialization"));
    	suite.addTest(new QueryDeserializerTestCase("testQueryDeserializationBadValues"));
    	suite.addTest(new QueryDeserializerTestCase("testQueryDeserializationNullValues"));
    	suite.addTest(new QueryDeserializerTestCase("testQueryDeserializationFactory"));
    	suite.addTest(new QueryDeserializerTestCase("testQueryDeserializationAnnotations"));
    	
    	return suite;
    }
    
    public static void main(String[] argv) {
    	junit.textui.TestRunner.run(suite());
    }
}
