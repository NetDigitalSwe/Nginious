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

package com.nginious.http.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.nginious.http.HttpMethod;
import com.nginious.http.HttpStatus;

/**
 * A HTTP client for executing HTTP requests.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class HttpClient {
	
	private String host;
	
	private int port;
	
	private Socket socket;
	
	private OutputStream out;
	
	private InputStream in;
	
	/**
	 * Constructs a new HTTP client that sends HTTP requests to the specified host and port.
	 * 
	 * @param host the host to connect to
	 * @param port the port to connec to on the host
	 */
	public HttpClient(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	/**
	 * Executes the specified HTTP request.
	 * 
	 * @param request the HTTP request
	 * @return the response for the HTTP request
	 * @throws HttpClientException if a HTTP error occurs while executing request
	 * @throws IOException if a I/O error occurs
	 */
	public HttpClientResponse request(HttpClientRequest request) throws HttpClientException, IOException {
		return request(request, (byte[])null);
	}
	
	/**
	 * Executes the specified HTTP request using the specified content is used as body content for the HTTP request.
	 * 
	 * @param request the HTTP request
	 * @param content the body content
	 * @return the response for the HTTP request
	 * @throws HttpClientException if a HTTP error occurs while executing request
	 * @throws IOException if an I/O error occurs
	 */
	public HttpClientResponse request(HttpClientRequest request, byte[] content) throws HttpClientException, IOException {
		return request(request, content != null ? new ByteArrayInputStream(content) : null);
	}
	
	/**
	 * Executes the specified HTTP request using the specified input stream as body content for the HTTP request.
	 * 
	 * @param request the HTTP request
	 * @param in the input stream
	 * @return the response for the HTTP request
	 * @throws HttpClientException if a HTTP error occurs while executing request
	 * @throws IOException if an I/O error occurs
	 */
	public HttpClientResponse request(HttpClientRequest request, InputStream in) throws HttpClientException, IOException {
		if(this.socket == null) {
			this.socket = new Socket(this.host, this.port);
			this.out = socket.getOutputStream();
			this.in = socket.getInputStream();
		}
		
		String method = createMethod(request);
		String headers = createHeaders(request);
		
		out.write(method.getBytes("iso-8859-1"));
		out.write(headers.getBytes("iso-8859-1"));
		
		if(in != null) {
			byte[] b = new byte[1024];
			int len = 0;
			
			while((len = in.read(b)) > 0) {
				out.write(b, 0, len);
			}
		}
		
		out.flush();
		
		HttpClientResponse response = new HttpClientResponse();
		read(request, response);
		
		if(!isKeepAlive(request)) {
			close();
		}
		
		return response;		
	}
	
	/**
	 * Returns whether or not if this HTTP clients connection to the server is keep alive.
	 * 
	 * @return <code>true</code> if underlying connection is keep alive, <code>false</code> otherwise
	 */
	public boolean isKeepAlive() {
		return this.socket != null;
	}
	
	/**
	 * Closes the underlying connection for this HTTP client,
	 * 
	 * @throws IOException if an I/O error occurs while closing connection
	 */
	public void close() throws IOException {
		if(this.socket != null) {
			socket.close();
			this.socket = null;
			this.in = null;
			this.out = null;
		}
	}
	
	/**
	 * Reads and parses response from HTTP server for specified HTTP request. The specified HTTP response is filled in
	 * with the read information. 
	 * 
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @throws HttpClientException if the read response data is invalid
	 * @throws IOException if an I/O error occurs while reading response data
	 */
	private void read(HttpClientRequest request, HttpClientResponse response) throws HttpClientException, IOException {
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
    	byte[] line = null;
    	boolean first = true;
    	int contentLength = 0;
    	
    	int bt = 0;
    	int delimiterCount = 0;
    	
    	while(delimiterCount < 4 && (bt = in.read()) != -1) {
    		if(bt == 13 && delimiterCount == 0) {
    			line = out.toByteArray();
    			delimiterCount++;
    		} else if(bt == 10 && delimiterCount == 1) {
    			delimiterCount++;
    			out.reset();
    			
    			if(first) {
    				String method = new String(line);
    				String[] versionStatusMsg = method.split(" ", 3);
    				
    				if(versionStatusMsg.length == 3) {
    					HttpStatus status = HttpStatus.getHttpStatus(Integer.parseInt(versionStatusMsg[1]));
    					response.setStatus(status);
    					response.setStatusMessage(versionStatusMsg[2]);
    					first = false;
    				} else {
    					throw new HttpClientException("Invalid method response '" + method + "'");
    				}
    			} else {
    				String header = new String(line);
    				String[] nameValue = header.split(":", 2);
    				
    				if(nameValue.length == 2) {
    					String name = nameValue[0].trim();
    					String value = nameValue[1].trim();
    					response.addHeader(name, value);
    					
    					if(name.equals("Content-Length")) {
    						contentLength = Integer.parseInt(value);
    					}
    				} else {
    					throw new HttpClientException("Invalid header in response '" + header + "'");
    				}
    			}
    		} else if(bt == 13 && delimiterCount == 2) {
    			delimiterCount++;
    		} else if(bt == 10 && delimiterCount == 3) {
    			delimiterCount++;
    		} else {
    			delimiterCount = 0;
    		}
    			
    		out.write(bt);    		
    	}
    	
    	response.setContentLength(contentLength);
    	
    	if(request.getMethod().equals(HttpMethod.HEAD)) {
    		contentLength = 0;
    	}
    	
    	byte[] buff = new byte[contentLength];
    	int readLen = 0;
    	
    	while(contentLength > 0) {
    		int read = in.read(buff, readLen, contentLength);
    		readLen += read;
    		contentLength -= read;
    	}
    	
    	out.reset();
    	out.write(buff, 0, buff.length);
    	byte[] data = out.toByteArray();
    	response.setContent(data);
	}
	
	/**
	 * Returns whether or not the specified HTTP request contains a <code>Connection</code> header with value
	 * <code>keep-alive</code>.
	 * 
	 * @param request the HTTP request
	 * @return <code>true</code> if HTTP request is keep alive, <code>false</code> otherwise
	 */
	private boolean isKeepAlive(HttpClientRequest request) {
		String value = request.getHeader("Connection");
		return value != null && value.equals("keep-alive");
	}
	
	/**
	 * Creates a HTTP request method line based on information from the specified HTTP request.
	 * 
	 * @param request the HTTP request
	 * @return a method line created from data in HTTP request 
	 * @throws HttpClientException if any of the HTTP request data is invalid
	 */
	private String createMethod(HttpClientRequest request) throws HttpClientException {
		StringBuffer methodLine = new StringBuffer();
		
		HttpMethod method = request.getMethod();
		
		if(method == null) {
			throw new HttpClientException("Request method is missing");
		}
		
		methodLine.append(method.toString());
		
		String path = request.getPath();
		
		if(path == null) {
			throw new HttpClientException("Request path is missing");
		}
		
		methodLine.append(' ');
		methodLine.append(path);
		
		methodLine.append(' ');
		methodLine.append("HTTP/1.1\015\012");
		return methodLine.toString();
	}
	
	/**
	 * Creates a formatted string containing all headers from the specified HTTP request suitable for sending
	 * to a HTTP server.
	 * 
	 * @param request the HTTP request
	 * @return a formatted string containing HTTP request headers
	 * @throws HttpClientException if any of the indata is invalid
	 */
	private String createHeaders(HttpClientRequest request) throws HttpClientException {
		StringBuffer headers = new StringBuffer();
		String[] names = request.getHeaderNames();
		
		for(String name : names) {
			String[] values = request.getHeaders(name);
			
			for(String value : values) {
				headers.append(name);
				headers.append(": ");
				headers.append(value);
				headers.append("\015\012");
			}
		}
		
		headers.append("\015\012");
		return headers.toString();
	}
}
