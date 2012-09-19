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

package com.nginious.http.upload;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;

/**
 * Reads an input file and adds multipart content formatting for sending to a HTTP server.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class MultipartInputStream extends InputStream {
	
	private String delimiter;
	
	private long contentLength;
	
	private InputStream in;
	
	private HashMap<String, String> headers;
	
	private boolean headerWritten;
	
	private int headerPos;
	
	private byte[] header;
	
	private boolean footerWritten;
	
	private int footerPos;
	
	private byte[] footer;
	
	private boolean contentWritten;
	
	/**
	 * Constructs a new multipart input stream that reads the specified file and uses the
	 * specified delimiter as boundary for the multipart content part.
	 * 
	 * @param delimiter the boundry delimiter
	 * @param file the file to read
	 * @throws IOException if unable to open input stream for file
	 */
	public MultipartInputStream(String delimiter, File file) throws IOException {
		this.delimiter = delimiter;
		this.in = new FileInputStream(file);
		this.contentLength = file.length();
		this.headers = new HashMap<String, String>();
		this.headerPos = -1;
		this.footerPos = -1;
	}
	
	/**
	 * Constructs a new multipart input stream that reads the specified inout stream and uses the
	 * specified delimiter as boundary for the multipart content part. The content length
	 * is set to the specified length.
	 * 
	 * @param delimiter the boundary delimiter
	 * @param in the input stream to read
	 * @param length the content length
	 */
	public MultipartInputStream(String delimiter, InputStream in, int length) {
		this.delimiter = delimiter;
		this.in = in;
		this.contentLength = length;
		this.headers = new HashMap<String, String>();
		this.headerPos = -1;
		this.footerPos = -1;
	}
	
	/**
	 * Sets a header with the specified name and value for writing in the multipart content
	 * part.
	 * 
	 * @param name the header name
	 * @param value the header value
	 */
	public void setHeader(String name, String value) {
		headers.put(name, value);
	}
	
	/**
	 * Returns the total length of the multipart content part created by this multipart input
	 * stream. The length includes the multipart content header, actual content and
	 * footer.
	 * 
	 * @return the total length
	 * @throws IOException if unable to calculate total length
	 */
	public long length() throws IOException {
		if(this.header == null) {
			prepare();
		}
		
		return header.length + this.contentLength + footer.length;
	}
	
	/**
	 * Returns number of bytes available for reading from this multipart input stream.
	 * 
	 * @return the number of bytes available for reading
	 */
	public int available() throws IOException {
		if(this.header == null) {
			prepare();
		}
		
		int available = 0;
		
		if(!this.headerWritten) {
			available += header.length - this.headerPos;
			available += in.available();
			return available;
		}
		
		return in.available();
	}

	/**
	 * Closes this multipart input stream.
	 * 
	 * @throws IOEception if unable to close
	 */
	public void close() throws IOException {
		in.close();
	}
	
	/**
	 * Mark is not supported.
	 */
	public synchronized void mark(int mark) {
		return;
	}

	/**
	 * Returns whether or not mark is supported.
	 * 
	 * @return <code>false</code>
	 */
	public boolean markSupported() {
		return false;
	}
	
	/**
	 * Reads one byte from this multipart input stream.
	 * 
	 * @return the read byte or -1 if end of stream is reached
	 * @throws IOException if unable to read from underlying input stream
	 */
	public int read() throws IOException {
		if(this.header == null) {
			prepare();
		}
		
		if(!this.headerWritten) {
			int b = header[this.headerPos++];
			
			if(this.headerPos == header.length) {
				this.headerWritten = true;
			}
			
			return b;
		}
		
		if(!this.contentWritten) {
			int b = in.read();
			
			if(b == -1) {
				this.contentWritten = true;
			} else {
				return b;
			}
		}
		
		if(!this.footerWritten) {
			int b = footer[this.footerPos++];
			
			if(this.footerPos == footer.length) {
				this.footerWritten = true;
			}
			
			return b;
		}
		
		return -1;
	}
	
	/**
	 * Reads len bytes into the specified byte array starting at the specified start position
	 * in the byte array.
	 * 
	 * @param b the byte array to read data into
	 * @param start the start position in the byte array
	 * @param len the number of bytes to read into the byte array
	 * @return the actual number of bytes read into the byte array
	 * @throws IOException if unable to read from underlying input stream
	 */
	public int read(byte[] b, int start, int len) throws IOException {
		if(this.header == null) {
			prepare();
		}
		
		if(this.headerWritten && this.contentWritten && this.footerWritten) {
			return -1;
		}
		
		int readLen = 0;
		
		if(!this.headerWritten) {
			int headerLen = len > header.length - this.headerPos ? header.length - this.headerPos : len;
			System.arraycopy(this.header, this.headerPos, b, start, headerLen);
			len -= headerLen;
			readLen += headerLen;
			start += headerLen;
			this.headerPos += headerLen;
			
			if(this.headerPos == header.length){
				this.headerWritten = true;
			}
		}
		
		if(!this.contentWritten && len > 0) {
			int contentLen = in.read(b, start, len);
			
			if(contentLen == -1 || contentLen < len) {
				this.contentWritten = true;
			}
			
			if(contentLen > 0) {
				start += contentLen;
				len -= contentLen;
				readLen += contentLen;
			}
		}
		
		if(!this.footerWritten && len > 0) {
			int footerLen = len > footer.length - this.footerPos ? footer.length - this.footerPos : len;
			System.arraycopy(this.footer, this.footerPos, b, start, footerLen);
			len -= footerLen;
			readLen += footerLen;
			start += footerLen;
			this.footerPos += footerLen;
			
			if(this.footerPos == footer.length){
				this.footerWritten = true;
			}			
		}
		
		return readLen;
	}
	
	/**
	 * Reads b.length bytes into the specified byte array.
	 * 
	 * @param b the byte array to read data into
	 * @return actual number of bytes read into the byte array
	 * @throws IOException if unable to read data from the underlying input stream
	 */
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}
	
	/**
	 * Resets this multipart input stream.
	 * 
	 * @throws IOException if unable to reset
	 */
	public synchronized void reset() throws IOException {
		if(this.header == null) {
			prepare();
		}
		
		in.reset();
		this.headerWritten = false;
		this.headerPos = 0;
		this.contentWritten = false;
		this.footerWritten = false;
		this.footerPos = 0;
	}
	
	/**
	 * Skip is not supported.
	 * 
	 * @throws IOException not thrown
	 */
	public long skip(long len) throws IOException {
		if(this.header == null) {
			prepare();
		}
		
		return 0L;
	}
	
	/**
	 * Prepares this multipart input stream by constructing the part header and footer.
	 * 
	 * @throws IOException if unable to construct header and footer
	 */
	private void prepare() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Collection<String> names = headers.keySet();
		
		out.write("--".getBytes());
		out.write(delimiter.getBytes());
		out.write("\015\012".getBytes());
		
		for(String name : names) {
			String value = headers.get(name);
			out.write(name.getBytes());
			out.write(": ".getBytes());
			out.write(value.getBytes());
			out.write("\015\012".getBytes());
		}
		
		out.write("\015\012".getBytes());
		this.header = out.toByteArray();
		
		out = new ByteArrayOutputStream();
		out.write("\015\012--".getBytes());
		out.write(delimiter.getBytes());
		out.write("--\015\012".getBytes());
		this.footer = out.toByteArray();
		
		this.headerPos = 0;
		this.footerPos = 0;
	}
}
