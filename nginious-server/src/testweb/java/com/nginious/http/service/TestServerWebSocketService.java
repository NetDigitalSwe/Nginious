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
import java.util.Random;

import com.nginious.http.HttpException;
import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.application.Service;
import com.nginious.http.websocket.StatusCode;
import com.nginious.http.websocket.WebSocketBinaryMessage;
import com.nginious.http.websocket.WebSocketException;
import com.nginious.http.websocket.WebSocketService;
import com.nginious.http.websocket.WebSocketSession;
import com.nginious.http.websocket.WebSocketTextMessage;

@Service(path = "/serversocket")
public class TestServerWebSocketService extends WebSocketService {
	
	private WebSocketSession session;
	
	private Thread senderThread;
	
	private Sender sender;
	
	public TestServerWebSocketService() {
		super();
	}
	
	public void executeOpen(HttpRequest request, HttpResponse response, WebSocketSession session) throws HttpException, IOException {
		this.session = session;
		this.sender = new Sender("binary");
		this.senderThread = new Thread(this.sender);
		senderThread.start();
	}

	public void executeBinaryMessage(WebSocketBinaryMessage message, WebSocketSession session) throws WebSocketException, IOException {
		this.sender = new Sender("text");
		this.senderThread = new Thread(this.sender);
		senderThread.start();
	}

	public void executeTextMessage(WebSocketTextMessage message, WebSocketSession session) throws WebSocketException, IOException {
		String msg = message.getMessage();
		this.sender = new Sender(msg);
		this.senderThread = new Thread(this.sender);
		senderThread.start();
	}

	public void executeClose(WebSocketSession session) throws WebSocketException {
		this.session = null;
	}
	
	private class Sender implements Runnable {
		
		private String type;
		
		Sender(String type) {
			this.type = type;
		}
		
		public void run() {
			try {
				Thread.sleep(1000L);
				
				if(type.equals("close")) {
					session.sendClose(StatusCode.NORMAL_CLOSURE, "Normal");
				} else if(type.equals("ping")) {
					Random rnd = new Random();
					byte[] data = new byte[64];
					rnd.nextBytes(data);
					session.sendPing(data);
				} else if(type.equals("text")) {
					session.sendTextData("Hello world!");
				} else if(type.equals("binary")) {
					Random rnd = new Random();
					byte[] data = new byte[64];
					rnd.nextBytes(data);
					session.sendBinaryData(data);
				}
			} catch(Exception e) {
				
			}
		}
	}
}
