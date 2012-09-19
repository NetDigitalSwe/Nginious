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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;

import com.nginious.http.HttpCookie;
import com.nginious.http.HttpMethod;
import com.nginious.http.HttpSession;
import com.nginious.http.common.FixedBuffer;
import com.nginious.http.server.HttpContext.HttpRequestHandler;
import com.nginious.http.server.HttpContext.HttpResponseHandler;
import com.nginious.http.session.HttpSessionManager;

/**
 * Handles writing of HTTP response to a client including headers and body.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
class HttpOutput {
	
	private static final int BUF_LEN = 4 * 1024;
	
	private HttpContext handler;
	
	private HttpRequestHandler request;
	
	private HttpResponseHandler response;
	
    private HttpConnection conn;
    
    private FixedBuffer buffer;
    
    private int bytesWritten;
    
    private boolean anythingWritten;
    
    private boolean bufferWritten;
    
    private boolean headersWritten;
    
    private boolean chunked;
    
    private boolean flushed;
    
    private boolean close;
    
    private boolean upgrade;
    
    private boolean head;
    
    /**
     * Constructs a new HTTP output for the specified HTTP context, HTTP response and HTTP request.
     * 
     * @param handler the HTTP context
     * @param response the HTTP response
     * @param request the HTTP request
     */
	HttpOutput(HttpContext handler, HttpResponseHandler response, HttpRequestHandler request) {
		super();
		this.close = false;
		this.handler = handler;
		this.conn = handler.getConnection();
		this.response = response;
		this.request = request;
		this.buffer = new FixedBuffer(BUF_LEN);
		HttpMethod method = handler.getMethod();
		this.head = method != null && method.equals(HttpMethod.HEAD);
	}
	
	/**
	 * Returns number of bytes currently written by this HTTP output.
	 * 
	 * @return number of bytes written
	 */
	int getBytesWritten() {
		return this.bytesWritten;
	}
	
	/**
	 * Handles writing of response line and headers including
	 * 
	 * <ul>
	 * <li>Response line with HTTP version, status code and status message. Response HTTP version depends on request
	 * 	HTTP version.</li>
	 * <li>Handling of Connection header depending on HTTP version and connection header in request.</li>
	 * <li>Writing of all headers set in HTTP response.</li>
	 * </ul>
	 * 
	 * @throws IOException if an I/O error occurs
	 */
	void writeHeaders() throws IOException {
		String version = handler.getVersion();
		
		if(version == null) {
			this.close = true;
		} else if(version.equals("HTTP/0.9")) {
			this.close = true;
			this.headersWritten = true;
			return;
		} else if(version.equals("HTTP/1.0")) {
			this.close = true;
		}
		
		buffer.put(HttpSnippet.VERSION.bytes);
		buffer.put(response.getStatus().getResponse().getBytes());
		buffer.put(HttpSnippet.CRLF.bytes);
		
		String value = request.getHeader("Connection");
		
		if(value != null && value.equals("close")) {
			this.close = true;
		} else if(value != null && value.equals("keep-alive")) {
			this.close = false;
		}
		
		String[] headerNames = response.getHeaderNames();
		
		if(response.getContentType() != null) {
			buffer.put("Content-Type".getBytes());
			buffer.put(HttpSnippet.HDEL.bytes);
			buffer.put(response.getContentType().getBytes());
			buffer.put(HttpSnippet.CRLF.bytes);
		}
		
		if(version != null && version.equals("HTTP/1.1")) {
			String date = response.getHeader("Date");
			
			if(date == null) {
				buffer.put("Date".getBytes());
				buffer.put(HttpSnippet.HDEL.bytes);
				buffer.put(Header.formatDate(new Date()).getBytes());
				buffer.put(HttpSnippet.CRLF.bytes);
			}
		}
		
		for(String headerName : headerNames) {
			boolean transferEncoding = headerName.toLowerCase().equals("transfer-encoding");
			boolean connection = headerName.toLowerCase().equals("connection");
			String[] headerValues = response.getHeaders(headerName);
			
			for(String headerValue : headerValues) {
				if(transferEncoding && headerValue.indexOf("chunked") > -1) {
					this.chunked = true;
				}
				
				if(connection) {
					if(headerValue.toLowerCase().equals("upgrade")) {
						this.upgrade = true;
					} else if(headerValue.equals("close")) {
						this.close = true;
					} else if(headerValue.equals("keep-alive")) {
						this.close = false;
					}
				} else {
					writeBuffer(headerName);
					writeBuffer(HttpSnippet.HDEL);
					writeBuffer(headerValue);
					writeBuffer(HttpSnippet.CRLF);
				}
			}
		}
		
		// Up to application to set content length correctly. Remove if chunking
		if(!this.chunked && response.getContentLengthSet()) {
			int contentLength = response.getContentLength();
			writeBuffer("Content-Length: ");
			writeBuffer(Integer.toString(contentLength));
			writeBuffer(HttpSnippet.CRLF);
		}
		
		if(this.upgrade){
			writeBuffer(HttpSnippet.CONNECTION_UPGRADE);
		} else if(this.close) {
			writeBuffer(HttpSnippet.CONNECTION_CLOSE);
		} else {
			writeBuffer(HttpSnippet.CONNECTION_KEEP_ALIVE);
		}
		
		HttpSession session = request.getUsedSession();
		
		if(session != null) {
			HttpSessionManager sessionManager = conn.getSessionManager();
			sessionManager.storeSession(request, response, session);
		}
		
		HttpCookie[] cookies = response.getCookies();
		
		if(cookies != null && cookies.length > 0) {
			writeBuffer(HttpSnippet.COOKIE);
			boolean first = true;
			
			for(HttpCookie cookie : cookies) {
				if(!first) {
					writeBuffer(HttpSnippet.CRLF);
				}
				
				value = HttpCookieConverter.format(cookie);
				writeBuffer(value);
				first = false;
			}
			
			writeBuffer(HttpSnippet.CRLF);
		}
		
		writeBuffer(HttpSnippet.SERVER);
		writeBuffer(HttpSnippet.CRLF);
		
		this.headersWritten = true;
		
		if(this.chunked) {
			ByteBuffer out = buffer.toByteBuffer(0);
			conn.queueWrite(out);
			this.buffer = new FixedBuffer(BUF_LEN);
			this.bufferWritten = true;
		}		
	}
	
	/**
	 * Write the specified byte to the response body.
	 * 
	 * @param b the byte to write
	 * @throws IOException if an I/O exception occurs while writing response body
	 */
	void writeContent(byte b) throws IOException {
		byte[] buff = new byte[1];
		buff[0] = b;
		writeContent(buff, 0, 1);
	}
	
	/**
	 * Writes the specified bytes starting at the specified start position with the specified length. If
	 * response line and headers have not been written prior to calling this method they are written prior
	 * to writing the response body.
	 * 
	 * @param buff the bytes to write
	 * @param start start position in bytes
	 * @param len number of bytes to write
	 * @throws IOException if an I/O exception occurs while writing response body.
	 */
	void writeContent(byte[] buff, int start, int len) throws IOException {
		if(!this.headersWritten) {
			writeHeaders();
		}
		
		if(this.head) {
			this.anythingWritten = true;
			return;
		}
		
		if(this.chunked) {
			if(len > 0) {
				String chunkPrefixStr = Integer.toHexString(len);
				byte[] chunkPrefix = chunkPrefixStr.getBytes();
				this.buffer = new FixedBuffer(len + chunkPrefix.length + HttpSnippet.CRLF.bytes.length + HttpSnippet.CRLF.bytes.length);
				buffer.put(chunkPrefix);
				buffer.put(HttpSnippet.CRLF.bytes);
				buffer.put(buff, start, len);
				buffer.put(HttpSnippet.CRLF.bytes);
				writeBuffer();
			}
		} else {
			writeBuffer(buff, start, len);
		}
		
		this.anythingWritten = true;
	}
	
	/**
	 * Writes the specified string value to the response body as a iso-8959-1 encoded bytes.
	 * 
	 * @param value the string to write
	 * @throws IOException if an I/O exception occurs while writing response body
	 */
	private void writeBuffer(String value) throws IOException {
		byte[] valueBytes = value.getBytes();
		writeBuffer(valueBytes, 0, valueBytes.length);
	}
	
	/**
	 * Writes the specified chunk to the internal output vuffer.
	 * 
	 * @param chunk the chunk to write
	 */
	private void writeBuffer(HttpSnippet chunk) throws IOException {
		writeBuffer(chunk.bytes, 0, chunk.bytes.length);
	}
	
	/**
	 * Writes the specified bytes starting at the specified start position to the internal output
	 * buffer first. If buffer fills up it is written to the client.
	 * 
	 * @param buff the bytes to write
	 * @param start start position in bytes
	 * @param len number of bytes to write
	 * @throws IOException if an I/O exception occurs while writing response body
	 */
	private void writeBuffer(byte[] buff, int start, int len) throws IOException {
		while(len > 0) {
			int outLen = buffer.put(buff, start, len);
			
			if(outLen < len) {
				writeBuffer();
			}
			
			len -= outLen;
			start += outLen;
		}
	}
	
	/**
	 * Writes the internal output response body buffer to the client.
	 * 
	 * @throws IOException if an I/O error occurs.
	 */
	private void writeBuffer() throws IOException {
		ByteBuffer out = buffer.toByteBuffer(0);
		
		this.bytesWritten += out.remaining();
		conn.queueWrite(out);
		this.buffer = new FixedBuffer(BUF_LEN);
		this.bufferWritten = true;
	}
	
	/**
	 * Returns whether or not anything has been written to this HTTP output.
	 * 
	 * @return <code>true</code> if anything has been written, <code>false</code> otherwise
	 */
	boolean isAnythingWritten() {
		return this.anythingWritten;
	}
	
	/**
	 * Returns whether or not anything from this HTTP output has been written to the client. Write
	 * to client starts once the internal output buffer is full.
	 * 
	 * @return <code>true</code> if anything has been written to the client, <code>false</code> otherwise
	 */
	boolean isBufferWritten() {
		return this.bufferWritten;
	}
	
	/**
	 * Writes any remaining response data in the internal buffers to the client.
	 * 
	 * @throws IOException if an I/O error occurs
	 */
	void flushContent() throws IOException {		
		if(!this.headersWritten) {
			writeHeaders();
		}

		if(buffer.remaining() != buffer.size() && !this.chunked) {
			writeBuffer();
		}
		
		if(this.chunked && !this.flushed) {
			conn.queueWrite(ByteBuffer.wrap(HttpSnippet.END_CHUNK.bytes));
			this.bytesWritten += HttpSnippet.END_CHUNK.bytes.length;
		}
		
		this.flushed = true;
	}
	
	/**
	 * Returns whether or not the underlying connection for this HTTP output is keep alive
	 * or not. A connection is determined to be keep alive depending on HTTP version, connection headers
	 * sent in request as well as headers sent in response.
	 * 
	 * @return <code>true</code> if connection is keep alive, <code>false</code> otherwise
	 */
	boolean isKeepAlive() {
		return !this.close;
	}
}
