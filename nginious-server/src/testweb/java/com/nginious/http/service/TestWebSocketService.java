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

import com.nginious.http.HttpException;
import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.application.Service;
import com.nginious.http.websocket.WebSocketBinaryMessage;
import com.nginious.http.websocket.WebSocketException;
import com.nginious.http.websocket.WebSocketService;
import com.nginious.http.websocket.WebSocketSession;
import com.nginious.http.websocket.WebSocketTextMessage;

@Service(path = "/websocket")
public class TestWebSocketService extends WebSocketService {
	
	public TestWebSocketService() {
		super();
	}
	
	public void executeOpen(HttpRequest request, HttpResponse response, WebSocketSession session) throws HttpException, IOException {
		return;
	}

	public void executeBinaryMessage(WebSocketBinaryMessage message, WebSocketSession session) throws WebSocketException, IOException {
		byte[] payload = message.getMessage();
		session.sendBinaryData(payload);
	}

	public void executeTextMessage(WebSocketTextMessage message, WebSocketSession session) throws WebSocketException, IOException {
		String payload = message.getMessage();
		session.sendTextData(payload);
	}

	public void executeClose(WebSocketSession session) throws WebSocketException {
		
	}
}
