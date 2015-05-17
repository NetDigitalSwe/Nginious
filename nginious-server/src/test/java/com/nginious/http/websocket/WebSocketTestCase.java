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

package com.nginious.http.websocket;

import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.json.JSONObject;

import com.nginious.http.application.Application;
import com.nginious.http.application.ApplicationManager;
import com.nginious.http.common.StringUtils;
import com.nginious.http.server.HttpServer;
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;
import com.nginious.http.server.HttpTestConnection;
import com.nginious.http.service.TestServerWebSocketController;
import com.nginious.http.service.TestWebSocketController;
import com.nginious.http.service.TestWebSocketDeserializeController;
import com.nginious.http.service.TestWebSocketSerializeController;

public class WebSocketTestCase extends TestCase {
	
	private HttpServer server;
	
	public WebSocketTestCase() {
		super();
	}

	public WebSocketTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		HttpServerConfiguration config = new HttpServerConfiguration();
		config.setWebappsDir(null);
		config.setServerLogPath("build/test-server.log");
		config.setAccessLogPath("build/test-access.log");
		config.setPort(9000);
		HttpServerFactory factory = HttpServerFactory.getInstance();
		this.server = factory.create(config);
		ApplicationManager manager = server.getApplicationManager();
		Application application = manager.createApplication("test");
		application.addController(new TestWebSocketController());
		application.addController(new TestServerWebSocketController());
		application.addController(new TestWebSocketSerializeController());
		application.addController(new TestWebSocketDeserializeController());
		manager.publish(application);
		server.start();
	}

	protected void tearDown() throws Exception {
		if(this.server != null) {
			server.stop();
		}
	}
	
	//0                   1                   2                   3
	//0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	//+-+-+-+-+-------+-+-------------+-------------------------------+
	//|F|R|R|R| opcode|M| Payload len |    Extended payload length    |
	//|I|S|S|S|  (4)  |A|     (7)     |             (16/64)           |
	//|N|V|V|V|       |S|             |   (if payload len==126/127)   |
	//| |1|2|3|       |K|             |                               |
	//+-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - +
	//|     Extended payload length continued, if payload len == 127  |
	//+ - - - - - - - - - - - - - - - +-------------------------------+
	//|                               |Masking-key, if MASK set to 1  |
	//+-------------------------------+-------------------------------+
	//| Masking-key (continued)       |          Payload Data         |
	//+-------------------------------- - - - - - - - - - - - - - - - +
	//:                     Payload Data continued ...                :
	//+ - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
	//|                     Payload Data continued ...                |
	//+---------------------------------------------------------------+
	public void testHandshake() throws Exception {
		String request = "GET /test/websocket HTTP/1.1\015\012" +
        	"Host: server.example.com\015\012" +
        	"Upgrade: websocket\015\012" +
        	"Connection: Upgrade\015\012" +
        	"Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==\015\012" +
        	"Origin: http://example.com\015\012" +
        	"Sec-WebSocket-Protocol: chat, superchat\015\012" +
        	"Sec-WebSocket-Version: 13\015\012\015\012";
		
		String expectedResponse = "HTTP/1.1 101 Switching Protocols\015\012" + 
			"Date: <date>\015\012" +
			"Upgrade: websocket\015\012" + 
			"Sec-Websocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=\015\012" +
			"Content-Length: 0\015\012" + 
			"Connection: Upgrade\015\012" + 
			"Server: Nginious/1.0.0\015\012\015\012";
		
		HttpTestConnection conn = null;
		WebSocketTestConnection wsConn = null;
		
		try {
			conn = new HttpTestConnection();
			conn.write(request);
			
			String response = conn.readKeepAliveString();
			expectedResponse = conn.setHeaders(response, expectedResponse);	
			assertEquals(expectedResponse, response);
			
			wsConn = new WebSocketTestConnection(conn.getSocket());
			byte[] closeFrame = { (byte)0x88, (byte)0x82, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0x00, (byte)0x00 };
			wsConn.write(closeFrame);
			
			// Check response close frame
			byte[] closeResp = wsConn.read(4);
			assertNotNull(closeResp);
			assertEquals(4, closeResp.length);
			byte flags = closeResp[0];
			assertTrue((flags & 0x80) > 0); // Check for final frame flag set
			assertTrue((flags & 0x0F) == 8); // Check for opcode close
			byte len = closeResp[1];
			assertEquals(len, 2);
			byte payload1 = closeResp[2];
			assertEquals(payload1, (byte)(0xFF ^ 0x00));
			byte payload2 = closeResp[3];
			assertEquals(payload2, (byte)(0xFF ^ 0x00));
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
		
	}
	
	public void testBadRsvBits() throws Exception {
		WebSocketTestConnection wsConn = null;
		
		try {
			wsConn = handshake("websocket");
			
			byte[] payload = new byte[10];
			byte[] header = { (byte)0xF9, (byte)0x8A, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF };
			byte[] pingFrame = new byte[header.length + payload.length];
			System.arraycopy(header, 0, pingFrame, 0, header.length);
			System.arraycopy(payload, 0, pingFrame, header.length, payload.length);
			wsConn.write(pingFrame);
			
			// Check response close frame
			byte[] closeResp = wsConn.readFrame();
			assertNotNull(closeResp);
			byte flags = closeResp[0];
			assertTrue((flags & 0x80) > 0); // Check for final frame flag set
			assertTrue((flags & 0x0F) == 0x08); // Check for opcode close
			int statusCode = (int)(closeResp[2] << 8) + (int)(closeResp[3] & 0xFF);
			assertEquals(StatusCode.PROTOCOL_ERROR.value, statusCode);
		} finally {
			if(wsConn != null) {
				wsConn.close();
			}
		}		
	}
	
	public void testBadOpcode() throws Exception {
		WebSocketTestConnection wsConn = null;
		
		try {
			wsConn = handshake("websocket");
			
			byte[] payload = new byte[10];
			byte[] header = { (byte)0x8B, (byte)0x8B, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF };
			byte[] pingFrame = new byte[header.length + payload.length];
			System.arraycopy(header, 0, pingFrame, 0, header.length);
			System.arraycopy(payload, 0, pingFrame, header.length, payload.length);
			wsConn.write(pingFrame);
			
			// Check response close frame
			byte[] closeResp = wsConn.readFrame();
			assertNotNull(closeResp);
			byte flags = closeResp[0];
			assertTrue((flags & 0x80) > 0); // Check for final frame flag set
			assertTrue((flags & 0x0F) == 0x08); // Check for opcode close
			int statusCode = (int)(closeResp[2] << 8) + (int)(closeResp[3] & 0xFF);
			assertEquals(StatusCode.PROTOCOL_ERROR.value, statusCode);
		} finally {
			if(wsConn != null) {
				wsConn.close();
			}
		}		
	}
	
	public void testPing() throws Exception {
		WebSocketTestConnection wsConn = null;
		
		try {
			wsConn = handshake("websocket");
			
			byte[] payload = new byte[10];
			byte[] header = { (byte)0x89, (byte)0x8A, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF };
			byte[] pingFrame = new byte[header.length + payload.length];
			System.arraycopy(header, 0, pingFrame, 0, header.length);
			System.arraycopy(payload, 0, pingFrame, header.length, payload.length);
			wsConn.write(pingFrame);
			
			// Check response pong frame
			byte[] pongResp = wsConn.read(12);
			assertNotNull(pongResp);
			assertEquals(12, pongResp.length);
			byte flags = pongResp[0];
			assertTrue((flags & 0x80) > 0); // Check for final frame flag set
			assertTrue((flags & 0x0F) == 0x0A); // Check for opcode pong
			byte len = pongResp[1];
			assertEquals(len, 10);
			byte payload1 = pongResp[2];
			assertEquals(payload1, (byte)(0xFF ^ 0x00));
			byte payload2 = pongResp[3];
			assertEquals(payload2, (byte)(0xFF ^ 0x00));
		} finally {
			if(wsConn != null) {
				wsConn.close();
			}
		}		
	}
	
	public void testServerMessages() throws Exception {
		WebSocketTestConnection wsConn = null;
		
		try {
			wsConn = handshake("serversocket");
			
			// Receive binary message
			byte[] binaryFrame = wsConn.readFrame();
			assertNotNull(binaryFrame);
			assertEquals(66, binaryFrame.length);
			byte binaryFlags = binaryFrame[0];
			assertTrue((binaryFlags & 0x80) > 0); // Check for final frame flag set
			assertTrue((binaryFlags & 0x0F) == 0x02); // Check for opcode binary message
			byte len = binaryFrame[1];
			assertEquals(len, 64);
			
			// Echo binary message
			byte[] binaryPayload = new byte[64];
			System.arraycopy(binaryFrame, 2, binaryPayload, 0, 64);
			byte[] binaryHeader = { (byte)0x82, (byte)0xC0, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF };
			binaryFrame = new byte[binaryHeader.length + binaryPayload.length];
			System.arraycopy(binaryHeader, 0, binaryFrame, 0, binaryHeader.length);
			System.arraycopy(binaryPayload, 0, binaryFrame, binaryHeader.length, binaryPayload.length);
			wsConn.write(binaryFrame);
			
			// Receive text message
			byte[] textFrame = wsConn.readFrame();
			assertNotNull(textFrame);
			assertEquals(14, textFrame.length);
			byte textFlags = textFrame[0];
			assertTrue((textFlags & 0x80) > 0); // Check for final frame flag set
			assertTrue((textFlags & 0x0F) == 0x01); // Check for opcode binary message
			len = textFrame[1];
			assertEquals(len, 12);
			
			// Send ping text message
			byte[] pingTextMask = { (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF };
			String pingTextStr = "ping";
			byte[] pingTextPayload = generateMaskedString(pingTextStr, pingTextMask);			
			byte[] pingTextHeader = { (byte)0x81, (byte)0x84, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF };
			byte[] pingTextFrame = new byte[pingTextHeader.length + pingTextPayload.length];
			System.arraycopy(pingTextHeader, 0, pingTextFrame, 0, pingTextHeader.length);
			System.arraycopy(pingTextPayload, 0, pingTextFrame, pingTextHeader.length, pingTextPayload.length);
			wsConn.write(pingTextFrame);
			
			// Receive ping message
			byte[] pingFrame = wsConn.readFrame();
			assertNotNull(pingFrame);
			byte pingFlags = pingFrame[0];
			assertTrue((pingFlags & 0x80) > 0); // Check for final frame flag set
			assertTrue((pingFlags & 0x0F) == 0x09); // Check for opcode ping message
			assertEquals(66, pingFrame.length);
			len = pingFrame[1];
			assertEquals(len, 64);
			
			// Send pong message
			byte[] pongPayload = new byte[64];
			System.arraycopy(pingFrame, 2, pongPayload, 0, 64);
			byte[] pongHeader = { (byte)0x8A, (byte)0xC0, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF };
			byte[] pongFrame = new byte[pongHeader.length + pongPayload.length];
			System.arraycopy(pongHeader, 0, pongFrame, 0, pongHeader.length);
			System.arraycopy(pongPayload, 0, pongFrame, pongHeader.length, pongPayload.length);
			wsConn.write(pongFrame);
			
			// Send close text message
			byte[] closeTextMask = { (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF };
			String closeTextStr = "close";
			byte[] closeTextPayload = generateMaskedString(closeTextStr, closeTextMask);			
			byte[] closeTextHeader = { (byte)0x81, (byte)0x85, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF };
			byte[] closeTextFrame = new byte[closeTextHeader.length + closeTextPayload.length];
			System.arraycopy(closeTextHeader, 0, closeTextFrame, 0, closeTextHeader.length);
			System.arraycopy(closeTextPayload, 0, closeTextFrame, closeTextHeader.length, closeTextPayload.length);
			wsConn.write(closeTextFrame);
			
			// Receive close message
			byte[] closeFrame = wsConn.readFrame();
			assertNotNull(closeFrame);
			byte closeFlags = closeFrame[0];
			assertTrue((closeFlags & 0x80) > 0); // Check for final frame flag set
			assertTrue((closeFlags & 0x0F) == 0x08); // Check for opcode ping message
			assertEquals(10, closeFrame.length);
			len = closeFrame[1];
			assertEquals(len, 8); // Close code + "Normal" text
			int statusCode = (closeFrame[2] & 0xFF) << 8;
			statusCode += closeFrame[3] & 0xFF;
			assertEquals(StatusCode.NORMAL_CLOSURE.value, statusCode);
			String desc = new String(closeFrame, 4, closeFrame.length - 4);
			assertEquals("Normal", desc);
		} finally {
			if(wsConn != null) {
				wsConn.close();
			}
		}		
	}
	
	public void testSmallBinaryMessages() throws Exception {
		WebSocketTestConnection wsConn = null;
		
		try {
			wsConn = handshake("websocket");
			
			for(int i = 10; i < 126; i++) {
				byte len = (byte)(0x80 + i);
				byte[] mask = generateRandomBytes(4);
				byte[] payload = generateMaskedBytes(i, mask);
				byte[] header = { (byte)0x82, len, mask[0], mask[1], mask[2], mask[3] };
				
				byte[] frame = new byte[header.length + payload.length];
				System.arraycopy(header, 0, frame, 0, header.length);
				System.arraycopy(payload, 0, frame, header.length, payload.length);
				wsConn.write(frame);
				
				// Check response frame
				byte[] respFrame = wsConn.read(frame.length - 4);
				assertFrame(respFrame, 0x02, true, frame.length - header.length);
				assertEquals(frame, header.length, respFrame, mask);
			}
		} finally {
			if(wsConn != null) {
				wsConn.close();
			}
		}		
	}
	
	public void testMediumBinaryMessages() throws Exception {
		WebSocketTestConnection wsConn = null;
		
		try {
			wsConn = handshake("websocket");
			
			for(int i = 127; i < 65536; i += 1000) {
				byte len1 = (byte)(0xFE);
				byte len2 = (byte)((i & 0x0000FF00) >> 8);
				byte len3 = (byte)(i & 0x000000FF);
				byte[] mask = generateRandomBytes(4);
				byte[] payload = generateMaskedBytes(i, mask);
				byte[] header = { (byte)0x82, len1, len2, len3, mask[0], mask[1], mask[2], mask[3] };
				
				byte[] frame = new byte[header.length + payload.length];
				System.arraycopy(header, 0, frame, 0, header.length);
				System.arraycopy(payload, 0, frame, header.length, payload.length);
				wsConn.write(frame);
				
				int numFrames = (int)(frame.length / 8192)  + 1;
				
				for(int j = 0; j < numFrames; j++) {
					// Check response frame
					byte[] respFrame = wsConn.readFrame();
					byte flags = respFrame[0];
					assertNotNull(respFrame);
					
					if(numFrames == 1) {
						assertFrame(respFrame, 0x02, true, frame.length - header.length);
						assertEquals(frame, header.length, respFrame, mask);						
					} else {
						if(j == 0) {
							assertTrue((flags & 0x0F) == 0x02); // Check for opcode binary message							
						} else {
							assertTrue((flags & 0x0F) == 0x00); // Check for opcode continuation message							
						}
						
						if(j < numFrames - 1) {
							assertFrame(respFrame, j == 0 ? 0x02 : 0x00, false, 8188);
						} else {
							int lastLen = (frame.length - header.length) - (8188 * j);
							assertFrame(respFrame, 0x00, true, lastLen);
						}
						
						assertEquals(frame, 8 + (8188 * j), respFrame, mask);							
					}
				}				
			}
		} finally {
			if(wsConn != null) {
				wsConn.close();
			}
		}		
	}
	
	public void testLargeBinaryMessages() throws Exception {
		WebSocketTestConnection wsConn = null;
		
		try {
			wsConn = handshake("websocket");
			
			int len = 131072;
			byte len1 = (byte)(0xFF);
			byte len2 = 0;
			byte len3 = 0;
			byte len4 = 0;
			byte len5 = 0;
			byte len6 = (byte)((len & 0xFF000000) >> 24);
			byte len7 = (byte)((len & 0x00FF0000) >> 16);
			byte len8 = (byte)((len & 0x0000FF00) >> 8);
			byte len9 = (byte)(len & 0x000000FF);
			byte[] mask = generateRandomBytes(4);
			byte[] payload = generateMaskedBytes(len, mask);
			byte[] header = { (byte)0x82, len1, len2, len3, len4, len5, len6, len7, len8, len9, mask[0], mask[1], mask[2], mask[3] };
			
			byte[] frame = new byte[header.length + payload.length];
			System.arraycopy(header, 0, frame, 0, header.length);
			System.arraycopy(payload, 0, frame, header.length, payload.length);
			wsConn.write(frame);
			
			int numFrames = (int)(frame.length / 8192)  + 1;
			
			for(int i = 0; i < numFrames; i++) {
				// Check response frame
				byte[] respFrame = wsConn.readFrame();
				
				if(i == 0) {
					assertFrame(respFrame, 0x02, false, 8188);
				} else if(i == numFrames - 1) {
					assertFrame(respFrame, 0x00, true, 64);
				} else {
					assertFrame(respFrame, 0x00, false, 8188);
				}
				
				assertEquals(frame, 14 + (8188 * i), respFrame, mask);							
			}
		} finally {
			if(wsConn != null) {
				wsConn.close();
			}
		}		
	}
	
	public void testSmallTextMessages() throws Exception {
		WebSocketTestConnection wsConn = null;
		
		try {
			wsConn = handshake("websocket");
			
			for(int i = 10; i < 126; i++) {
				byte len = (byte)(0x80 + i);
				byte[] mask = generateRandomBytes(4);
				String str = StringUtils.generatePassword(i);
				byte[] payload = generateMaskedString(str, mask);
				byte[] header = { (byte)0x81, len, mask[0], mask[1], mask[2], mask[3] };
				
				byte[] frame = new byte[header.length + payload.length];
				System.arraycopy(header, 0, frame, 0, header.length);
				System.arraycopy(payload, 0, frame, header.length, payload.length);
				wsConn.write(frame);
				
				// Check response frame
				byte[] respFrame = wsConn.read(frame.length - 4);
				assertNotNull(respFrame);
				assertEquals(frame.length - 4, respFrame.length);
				
				byte flags = respFrame[0];
				assertTrue((flags & 0x80) > 0); // Check for final frame flag set
				assertTrue("i=" + i + ", flags=" + flags, (flags & 0x0F) == 0x01); // Check for opcode text message
				len = respFrame[1];
				assertEquals(i, len);
				String respStr = new String(respFrame, 2, len);
				assertEquals(str, respStr);
			}
		} finally {
			if(wsConn != null) {
				wsConn.close();
			}
		}		
	}
	
	public void testDeserializableBeans() throws Exception {
		WebSocketTestConnection wsConn = null;
		
		try {
			wsConn = handshake("wsdeserialize");
			
			byte[] mask = generateRandomBytes(4);
			String str = "{ \"testBean1\": { \"first\": \"true\" } }"; 
			byte len = (byte)(0x80 + str.length());
			byte[] payload = generateMaskedString(str, mask);
			byte[] header = { (byte)0x81, len, mask[0], mask[1], mask[2], mask[3] };
			
			byte[] frame = new byte[header.length + payload.length];
			System.arraycopy(header, 0, frame, 0, header.length);
			System.arraycopy(payload, 0, frame, header.length, payload.length);
			wsConn.write(frame);
				
			// Check response frame
			byte[] respFrame = wsConn.readFrame();
			assertNotNull(respFrame);
			
			byte flags = respFrame[0];
			assertTrue((flags & 0x80) > 0); // Check for final frame flag set
			assertTrue("i=" + str.length() + ", flags=" + flags, (flags & 0x0F) == 0x01); // Check for opcode text message
			len = respFrame[1];
			
			String respStr = new String(respFrame, 2, len);
			JSONObject testBean = new JSONObject(respStr);
			assertTrue(testBean.has("testBean1"));
			JSONObject testBean2 = testBean.getJSONObject("testBean1");
			assertNotNull(testBean2);
			assertTrue(testBean2.getBoolean("first"));
		} finally {
			if(wsConn != null) {
				wsConn.close();
			}
		}			
	}
	
	public void testSerializableBeans() throws Exception {
		WebSocketTestConnection wsConn = null;
		
		try {
			wsConn = handshake("wsserialize");
			
			byte[] mask = generateRandomBytes(4);
			String str = "{ \"testBean1\": { \"first\": \"true\" } }"; 
			byte len = (byte)(0x80 + str.length());
			byte[] payload = generateMaskedString(str, mask);
			byte[] header = { (byte)0x81, len, mask[0], mask[1], mask[2], mask[3] };
			
			byte[] frame = new byte[header.length + payload.length];
			System.arraycopy(header, 0, frame, 0, header.length);
			System.arraycopy(payload, 0, frame, header.length, payload.length);
			wsConn.write(frame);
				
			// Check response frame
			byte[] respFrame = wsConn.readFrame();
			assertNotNull(respFrame);
			
			byte flags = respFrame[0];
			assertTrue((flags & 0x80) > 0); // Check for final frame flag set
			assertTrue("i=" + str.length() + ", flags=" + flags, (flags & 0x0F) == 0x01); // Check for opcode text message
			len = respFrame[1];
			
			String respStr = new String(respFrame, 2, len);
			JSONObject testBean = new JSONObject(respStr);
			assertTrue(testBean.has("testBean1"));
			JSONObject testBean2 = testBean.getJSONObject("testBean1");
			assertNotNull(testBean2);
			assertTrue(testBean2.getBoolean("first"));
		} finally {
			if(wsConn != null) {
				wsConn.close();
			}
		}		
	}
	
	private void assertFrame(byte[] frame, int opcode, boolean finalFrame, int expectedLength) {
		assertNotNull(frame);
		byte flags = frame[0];
		
		assertEquals((flags &  0x80) != 0, finalFrame);
		assertEquals((flags & 0x0F), opcode);
		byte len1 = frame[1];
		int length = 0;
		
		if(len1 == 127) {
			fail("Server should not return frames with payload larger than 8188 bytes");
		} else if(len1 == 126) {
			// 2 more bytes
			byte len2 = frame[2];
			byte len3 = frame[3];
			length = (len2 << 8) + (len3 & 0xFF);
		} else {
			length = len1;			
		}
		
		assertEquals(expectedLength, length);
	}
	
	private void assertEquals(byte[] srcFrame, int srcIdx, byte[] respFrame, byte[] mask) {
		// assertEquals(src.length - srcIdx, dest.length - destIdx);
		int respIdx = respFrame[1] == 126 ? 4 : 2;
		int len = respFrame.length - respIdx;
		
		for(int i = 0; i < len; i++) {
			assertEquals("i=" + i + ", srcIdx=" + srcIdx + ", destIdx=" + respIdx, srcFrame[srcIdx + i], respFrame[respIdx + i] ^ mask[i % 4]);
		}
	}
	
	private WebSocketTestConnection handshake(String uri) throws Exception {
		String request = "GET /test/" + uri + " HTTP/1.1\015\012" +
    		"Host: server.example.com\015\012" +
    		"Upgrade: websocket\015\012" +
    		"Connection: Upgrade\015\012" +
    		"Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==\015\012" +
    		"Origin: http://example.com\015\012" +
    		"Sec-WebSocket-Protocol: chat, superchat\015\012" +
    		"Sec-WebSocket-Version: 13\015\012\015\012";
		
		String expectedResponse = "HTTP/1.1 101 Switching Protocols\015\012" + 
			"Date: <date>\015\012" +
			"Upgrade: websocket\015\012" + 
			"Sec-Websocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=\015\012" +
			"Content-Length: 0\015\012" + 
			"Connection: Upgrade\015\012" + 
			"Server: Nginious/1.0.0\015\012\015\012";
		
		HttpTestConnection conn = null;
		WebSocketTestConnection wsConn = null;
		
		conn = new HttpTestConnection();
		conn.write(request);
		
		String response = conn.readKeepAliveString();
		expectedResponse = conn.setHeaders(response, expectedResponse);	
		assertEquals(expectedResponse, response);
		
		wsConn = new WebSocketTestConnection(conn.getSocket());
		return wsConn;
	}
	
	private byte[] generateMaskedBytes(int len, byte[] mask) {
		byte[] bytes = new byte[len];
		Random rnd = new Random();
		rnd.nextBytes(bytes);
		
		for(int i = 0; i < len; i++) {
			bytes[i] = (byte)(bytes[i] ^ mask[i % 4]);
		}
		
		return bytes;
	}
	
	private byte[] generateMaskedString(String str, byte[] mask) {
		byte[] bytes = str.getBytes();
		
		for(int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte)(bytes[i] ^ mask[i % 4]);
		}
		
		return bytes;
	}
	
	private byte[] generateRandomBytes(int len) {
		byte[] bytes = new byte[len];
		Random rnd = new Random();
		rnd.nextBytes(bytes);
		return bytes;
	}
	
	public static Test suite() {
		return new TestSuite(WebSocketTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
