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

package com.nginious.http.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.nginious.http.HttpException;
import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.annotation.Controller;
import com.nginious.http.annotation.Message;
import com.nginious.http.serialize.Deserializer;
import com.nginious.http.serialize.DeserializerFactory;
import com.nginious.http.serialize.Serializer;
import com.nginious.http.serialize.SerializerException;
import com.nginious.http.serialize.SerializerFactory;
import com.nginious.http.serialize.SerializerFactoryException;
import com.nginious.http.websocket.StatusCode;
import com.nginious.http.websocket.WebSocketBinaryMessage;
import com.nginious.http.websocket.WebSocketException;
import com.nginious.http.websocket.WebSocketOperation;
import com.nginious.http.websocket.WebSocketSession;
import com.nginious.http.websocket.WebSocketTextMessage;

@Controller(path = "/wsdeserialize")
public class TestWebSocketDeserializeController {
	
	public TestWebSocketDeserializeController() {
		super();
	}
	
	@Message(operations = { WebSocketOperation.OPEN} )
	public void executeOpen(HttpRequest request, HttpResponse response, WebSocketSession session) throws HttpException, IOException {
		return;
	}
	
	@Message(operations = { WebSocketOperation.BINARY })
	public void executeBinaryMessage(WebSocketBinaryMessage message, WebSocketSession session) throws WebSocketException, IOException {
		byte[] payload = message.getMessage();
		session.sendBinaryData(payload);
	}

	@Message(operations = { WebSocketOperation.TEXT })
	public String executeTextMessage(DeserializerFactory factory1, SerializerFactory factory2, WebSocketTextMessage message, WebSocketSession session) throws WebSocketException, IOException {
		try {
			Deserializer<TestBean1> deserializer = factory1.createDeserializer(TestBean1.class, "application/json");
			TestBean1 bean = deserializer.deserialize(message.getMessage());
			Serializer<TestBean1> serializer = factory2.createSerializer(TestBean1.class, "application/json");
			
			StringWriter writer = new StringWriter();
			PrintWriter prWriter = new PrintWriter(writer);
			serializer.serialize(prWriter, bean);
			prWriter.flush();
			return writer.toString();
		} catch(SerializerFactoryException e) {
			throw new WebSocketException(StatusCode.INTERNAL_SERVER_ERROR, "Failed");
		} catch(SerializerException e) {
			throw new WebSocketException(StatusCode.INTERNAL_SERVER_ERROR, "Failed");			
		}
	}
	
	@Message(operations = { WebSocketOperation.CLOSE })
	public void executeClose(WebSocketSession session) throws WebSocketException {
		return;
	}
}
