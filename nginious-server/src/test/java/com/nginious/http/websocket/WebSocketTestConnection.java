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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class WebSocketTestConnection {
	
	private Socket socket;
	
	public WebSocketTestConnection(Socket socket) throws IOException {
		this.socket = socket;
		socket.setTcpNoDelay(true);
	}
	
	public void write(byte[] frame) throws IOException {
		OutputStream out = socket.getOutputStream();
		out.write(frame);
		out.flush();		
	}
	
	public byte[] read(int numBytes) throws IOException {
    	InputStream in = null;
    	
    	in = socket.getInputStream();
    	byte[] buff = new byte[numBytes];
    	int pos = 0;
    	
    	while(pos < numBytes) {
    		int val = in.read();
    		
    		if(val > -1) {
    			buff[pos++] = (byte)val;
    		} else {
    			break;
    		}
    	}
    	
    	return pos == numBytes ? buff : null;
	}
	
	public byte[] readFrame() throws IOException {
    	InputStream in = null;
    	
    	in = socket.getInputStream();
    	int val = 0;
    	
    	byte flags = (byte)in.read();
    	int len = in.read();
    	byte[] buff = null;
    	int headerLen = 0;
    	
    	if(len == 126) {
    		int len1 = in.read();
    		int len2 = in.read();
    		len = (len1 << 8) + (len2 & 0xFF);
    		buff = new byte[len + 4];
    		buff[0] = flags;
    		buff[1] = 126;
    		buff[2] = (byte)len1;
    		buff[3] = (byte)len2;
    		headerLen = 4;
    	} else {
    		buff = new byte[len + 2];
        	buff[0] = flags;
        	buff[1] = (byte)len;
        	headerLen = 2;
    	}
    	
    	int pos = 0;
    	
    	while(pos < len) {
    		val = in.read();
    		
    		if(val > -1) {
    			buff[pos + headerLen] = (byte)val;
    			pos++;
    		} else {
    			break;
    		}
    	}
    	
    	return pos == len ? buff : null;
	}
	
	public void close() {
		try { socket.close(); } catch(IOException e) {}
	}
	
	public boolean isClosed() {
		return socket.isClosed();
	}
}
