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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class HttpTestConnection {
	
	private Socket socket;
	
	public HttpTestConnection() throws IOException {
		this(2000);
	}
	
	public HttpTestConnection(int timeout) throws IOException {
		super();
		this.socket = new Socket("localhost", 9000);
		socket.setSoTimeout(timeout);
	}
	
	public Socket getSocket() {
		return this.socket;
	}
	
	public void write(String request) throws IOException {
		OutputStream out = socket.getOutputStream();
		
		SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		request = request.replaceFirst("<modified>", format.format(new Date()));
		
		out.write(request.getBytes());
		out.flush();		
	}
	
	public void write(byte[] request) throws IOException {
		OutputStream out = socket.getOutputStream();
		
		out.write(request);
		out.flush();
	}
	
	public void write(byte[] request, int start, int len) throws IOException {
		OutputStream out = socket.getOutputStream();
		out.write(request, start, len);
		out.flush();
	}
	
	public String readString() throws IOException {
		byte[] data = readBytes();
		return new String(data);
	}
	
	public String readString(String encoding) throws IOException {
		byte[] data = readBytes();
		return new String(data, encoding);
	}
	
	public boolean checkClosed() throws IOException {
		InputStream in = null;
		
		try {
			in = socket.getInputStream();
			return in.read() == -1;
		} catch(IOException e) {
			return true;
		} finally {
			if(in != null) {
				try { in.close(); } catch(IOException e) {}
			}
		}
	}
	
	public byte[] readBytes() throws IOException {
    	InputStream in = null;
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
    	
    	try {
    		in = socket.getInputStream();
    		byte[] buff = new byte[1024];
    		int len = 0;
    		
    		while((len = in.read(buff)) > 0) {
    			out.write(buff, 0, len);
    		}
    		
            byte[] data = out.toByteArray();
            return data;
    	} finally {
    		if(in != null) {
    			try { in.close(); } catch(IOException e) {}
    		}
        }
    }
    
	public String setHeaders(String response, String expectedResponse) throws IOException {
		int startIdx = response.indexOf("Date: ");
		int endIdx = response.indexOf("\n", startIdx);
		String date = response.substring(startIdx + 6, endIdx - 1);
		expectedResponse = expectedResponse.replaceFirst("<date>", date);			

		startIdx = response.indexOf("Last-Modified: ");
		endIdx = response.indexOf("\n", startIdx);
		String lastModified = response.substring(startIdx + 15, endIdx - 1);
		expectedResponse = expectedResponse.replaceFirst("<modified>", lastModified);			
		
		startIdx = response.indexOf("JSESSIONID=");
		
		if(startIdx > -1) {
			endIdx = response.indexOf(";", startIdx);
			String sessionId = response.substring(startIdx + 11, endIdx);
			expectedResponse = expectedResponse.replaceFirst("<sessionid>", sessionId);
		}
		
		startIdx = response.indexOf("Expires=");
		
		if(startIdx > -1) {
			endIdx = response.indexOf("\015\012", startIdx);
			String expires = response.substring(startIdx + 8, endIdx);
			expectedResponse = expectedResponse.replaceFirst("<expires>", expires);
		}
		
		return expectedResponse;
	}
	
	public String readBodyString(String encoding) throws IOException {
		byte[] data = readBody();
		return new String(data, encoding);
    }
	
	public byte[] readBody() throws IOException {
    	InputStream in = null;
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
    	
    	try {
    		in = socket.getInputStream();
    		byte[] buff = new byte[1024];
    		int len = 0;
    		
    		while((len = in.read(buff)) > 0) {
    			out.write(buff, 0, len);
    		}
    		
            byte[] data = out.toByteArray();
            int contentStart = findContentStart(data);
            byte[] contentData = new byte[data.length - contentStart];
            System.arraycopy(data, contentStart, contentData, 0, contentData.length);
            return contentData;
    	} finally {
    		if(in != null) {
    			try { in.close(); } catch(IOException e) {}
    		}
        }
    }
	
	public String readKeepAliveString() throws IOException {
		byte[] data = readKeepAliveBytes();
		return new String(data);
	}
	
	public byte[] readKeepAliveBody() throws IOException {
    	InputStream in = null;
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
    	
    	in = socket.getInputStream();
    	int bt = 0;
    	int delimiterCount = 0;
    	
    	while(delimiterCount < 4 && (bt = in.read()) != -1) {
    		if(bt == 13 && delimiterCount == 0) {
    			delimiterCount++;
    		} else if(bt == 10 && delimiterCount == 1) {
    			delimiterCount++;
    		} else if(bt == 13 && delimiterCount == 2) {
    			delimiterCount++;
    		} else if(bt == 10 && delimiterCount == 3) {
    			delimiterCount++;
    		} else {
    			delimiterCount = 0;
    		}
    		
    		out.write(bt);
    	}
    	
    	String headers = new String(out.toByteArray());
    	out.reset();
    	int startIdx = headers.indexOf("Content-Length: ");
    	int endIdx = headers.indexOf('\n', startIdx);
    	int length = Integer.parseInt(headers.substring(startIdx + 16, endIdx - 1));
    	byte[] buff = new byte[length];
    	int readLen = 0;
    	
    	while(length > 0) {
    		int read = in.read(buff, readLen, length);
    		readLen += read;
    		length -= read;
    	}
    	
    	out.write(buff, 0, buff.length);
    	byte[] data = out.toByteArray();
    	return data;		
	}
	
	public byte[] readKeepAliveBytes() throws IOException {
    	InputStream in = null;
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
    	
    	in = socket.getInputStream();
    	
    	int bt = 0;
    	int delimiterCount = 0;
    	
    	while(delimiterCount < 4 && (bt = in.read()) != -1) {
    		if(bt == 13 && delimiterCount == 0) {
    			delimiterCount++;
    		} else if(bt == 10 && delimiterCount == 1) {
    			delimiterCount++;
    		} else if(bt == 13 && delimiterCount == 2) {
    			delimiterCount++;
    		} else if(bt == 10 && delimiterCount == 3) {
    			delimiterCount++;
    		} else {
    			delimiterCount = 0;
    		}
    		
    		out.write(bt);
    	}
    	
    	String headers = new String(out.toByteArray());
    	int startIdx = headers.indexOf("Content-Length: ");
    	int endIdx = headers.indexOf('\n', startIdx);
    	int length = Integer.parseInt(headers.substring(startIdx + 16, endIdx - 1));
    	byte[] buff = new byte[length];
    	in.read(buff, 0, length);
    	out.write(buff, 0, length);
    	byte[] data = out.toByteArray();
    	return data;
	}
	
	public void close() {
		if(this.socket != null) {
			try { socket.close(); } catch(IOException e) {}
		}
	}
	
	public int findContentStart(byte[] data) {
		for(int i = 0; i < data.length - 3; i++) {
			if(data[i] == 13 && data[i + 1] == 10 && data[i + 2] == 13 && data[i + 3] == 10) {
				return i + 4;
			}
		}
		
		return -1;
	}
	
	public CombinedLogEntry getLastAccessLogLine() throws LogException, IOException {
		FileInputStream in = null;
		InputStreamReader inReader = null;
		BufferedReader reader = null;
		
		try {
			in = new FileInputStream("build/test-access.log");
			inReader = new InputStreamReader(in);
			reader = new BufferedReader(inReader);
			String line = null;
			String outLine = null;
			
			while((line = reader.readLine()) != null) {
				outLine = line;
			}
			
			if(outLine != null) {
				return new CombinedLogEntry(outLine);
			}
			
			return null;
		} finally {
			if(reader != null) {
				try { reader.close(); } catch(IOException e) {}
			}
			
			if(in != null) {
				try { in.close(); } catch(IOException e) {}
			}
		}
	}
}
