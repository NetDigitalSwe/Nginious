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
import java.util.HashSet;

import com.nginious.http.HttpException;
import com.nginious.http.HttpMethod;
import com.nginious.http.HttpStatus;
import com.nginious.http.application.ApplicationManagerImpl;
import com.nginious.http.common.Buffer;
import com.nginious.http.common.ExpandableBuffer;
import com.nginious.http.common.FixedBuffer;
import com.nginious.http.upload.MultipartParser;

/**
 * Parses HTTP requests. Accepts request data in fragments to support on the fly parsing as it is received from
 * the client. A {@link HttpContext} is constructed and filled with request data as the request is being parsed.
 * 
 * <p>
 * The following parts of the HTTP protocol are supported.
 * </p>
 * 
 * <p>
 * Versions:
 * 
 * <ul>
 * <li>HTTP/0.9 - parsing of HTTP/0.9 request line.</li>
 * <li>HTTP/1.0 - parsing of HTTP/1.0 request line, headers and body.</li>
 * <li>HTTP/1.1 - parsing of HTTP/1.1 request line, headers and body.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Content encodings:
 * 
 * <ul>
 * <li>Chunked encoding - parsing of chunked encoded body data.</li>
 * <li>Multipart data - parsing of multipart data. Uses {@link com.nginious.http.upload.MultipartParser} to parse 
 * 	multipart content.</li>
 * <li>Standard - reading of body content.</li>
 * </ul>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
class HttpParser {
	
	private enum State {
		
		// Method states
		START(11),
		
		METHOD(12),
		
		METHOD_URL_SPACE(13),
		
		URL(14),
		
		URL_VERSION_SPACE(15),
		
		VERSION(16),
		
		END_METHOD(17),
		
		// Header states
		HEADER(21),
		
		HEADER_NAME(22),
		
		HEADER_VALUE(23),
		
		END_HEADERS(24),
		
		// Content states
		CONTENT(31),
		
		MULTIPART_CONTENT(32),
		
		CHUNKED_CONTENT(33),
		
		END(34);
		
		int value;
		
		private State(int value) {
			this.value = value;
		}
	}
	
	private static final int UNKNOWN_TRANSFER_ENCODING = -2;
	
	private static final int CHUNKED_TRANSFER_ENCODING = -1;
    
	private static final int NO_TRANSFER_ENCODING = 0;
	
	private ApplicationManagerImpl manager;
	
	private HttpConnection connection;
	
    private HttpContext context;
    
    private State state;
    
    private byte previousCh;
    
    private long contentLength;
    
    private long contentPosition;
    
    private int savedHeaderLength;
    
    private int markIndex;
    
    private Buffer content;
    
    private MultipartParser multipartParser;
    
    private ChunkedParser chunkedParser;
    
    private String headerName;
    
    private String headerValue;
    
    private HashSet<String> hostnames;
    
    private int pos;
    
    private boolean trace;
    
    private boolean sendExpect100Continue;
    
    private boolean sendExpectationFailed;
    
    private Buffer traceBuffer;
    
    /**
     * Constructs a new HTTP request parser for the specified application manager and HTTP connection.
     * 
     * @param manager the application manager
     * @param connection the HTTP connection
     * @param hostnames
     */
	HttpParser(ApplicationManagerImpl manager, HttpConnection connection, HashSet<String> hostnames) {
		super();
		this.manager = manager;
		this.connection = connection;
		this.hostnames = hostnames;
		reset();
	}
	
	/**
	 * Resets this parser which prepares it for parsing a new HTTP request.
	 */
	void reset() {
		this.state = State.START;
		this.context = new HttpContext(this.manager, this.connection, this.hostnames);
		this.previousCh = HttpToken.NONE;
		this.contentLength = NO_TRANSFER_ENCODING;
		this.contentPosition = 0;
		this.savedHeaderLength = -1;
		this.markIndex = -1;
		this.content = null;
		this.headerName = null;
		this.headerValue = null;
		this.pos = 0;
	}
	
	/**
	 * Returns HTTP context associated with this HTTP parser.
	 * 
	 * @return the HTTP context
	 */
	HttpContext getContext() {
		return this.context;
	}
	
	/**
	 * Returns whether or not a expect 100 continue should be sent to the client.
	 * 
	 * @return <code>true</code> if expect 100 continue should be sent, <code>false</code> otherwise
	 */
	boolean sendExpect100Continue() {
		return this.sendExpect100Continue;
	}
	
	/**
	 * Returns whether or not a expectation failed should be sent to the client.
	 * 
	 * @return <code>true</code> if expectation failed should be sent, <code>false</code> otherwise
	 */
	boolean sendExpectationFailed() {
		return this.sendExpectationFailed;
	}
	
	/**
	 * Parser part of a HTTP request contained in the specified data.
	 * 
	 * @param data the provided request data
	 * @return <code>true</code> if parser has reached end of request body, <code>false</code> otherwise
	 * @throws IOException if an I/O error occurs
	 * @throws HttpException if request data is invalid causing a HTTP error
	 */
	boolean parse(ByteBuffer data) throws IOException, HttpException {
		int savePos = data.position();
		data.position(this.pos);
		int lastElementPos = 0;
		
		if(this.state != State.END) {
			if(this.state.value < State.END_METHOD.value) {
				lastElementPos = parseMethod(data, this.pos, savePos);
			}
			
			if(this.state.value >= State.HEADER.value && this.state.value < State.END_HEADERS.value) {
				lastElementPos = parseHeaders(data, this.pos, savePos);
			}
			
			if(this.state.value >= State.END_HEADERS.value && this.state.value < State.END.value) {
				lastElementPos = parseBody(data, this.pos, savePos);
			}
		}
		
		if(lastElementPos == savePos) {
			if(this.trace) {
				data.rewind();
				byte[] traceBuff = new byte[savePos];
				data.get(traceBuff);
				traceBuffer.put(traceBuff);
			}
			
			data.rewind();
			this.pos = 0;
		} else {
			data.position(savePos);
		}
		
		return this.state.value == State.END.value;
	}
	
	/**
	 * Parses request method line from specified request data starting at specified position and length.
	 * 
	 * @param data the request data to parse
	 * @param pos start position in data
	 * @param length length in data
	 * @return state of parser after parsing request data
	 * @throws HttpException if request method line is invalid causing a HTTP error
	 */
	@SuppressWarnings("incomplete-switch")
	int parseMethod(ByteBuffer data, int pos, int length) throws HttpException {
		int lastElementPos = 0;
		
		while(this.state.value < State.END_METHOD.value && pos < length) {
			byte ch = data.get(pos++);
			this.previousCh = HttpToken.NONE;
			
			switch(this.state) {
			case START:
				this.contentLength = UNKNOWN_TRANSFER_ENCODING;
				
				if(HttpToken.isPrintable(ch)) {
					this.markIndex = pos - 1;
					this.state = State.METHOD;
				}
				
				break;
				
			case METHOD:
				if(ch == HttpToken.SPACE) {
					byte[] b = new byte[pos - 1 - this.markIndex];
					data.position(this.markIndex);
					data.get(b);
					String method = new String(b);
					this.trace = method.equals("TRACE");
					
					if(this.trace) {
						this.traceBuffer = new ExpandableBuffer(2097152);
						context.trace(this.traceBuffer);
					}
					
					HttpMethod httpMethod = null;
					
					try {
						httpMethod = HttpMethod.valueOf(method);
					} catch(IllegalArgumentException e) {
						// Continue if HTTP method is unknown, problem will be handled later
					}
					
					context.setMethod(httpMethod, method);
					lastElementPos = pos - 1;
					this.state = State.METHOD_URL_SPACE;
					continue;
				} else if(HttpToken.isControl(ch)) {
					throw new HttpException(HttpStatus.BAD_REQUEST, "invalid method");
				}
				
				break;
				
			case METHOD_URL_SPACE:
				if(HttpToken.isPrintable(ch)) {
					this.markIndex = pos - 1;
					this.state = State.URL;
				} else if(HttpToken.isControl(ch)) {
					throw new HttpException(HttpStatus.BAD_REQUEST, "invalid url delimiter");
				}
				
				break;
				
			case URL:
				boolean extractUri = false;
				
				if (ch == HttpToken.SPACE) {
					extractUri = true;
					this.state = State.URL_VERSION_SPACE;
				} else if(HttpToken.isControl(ch)) {
					extractUri = true;
					context.setVersion("HTTP/0.9");
					this.state = State.END;
				}
				
				if(extractUri) {
					byte[] b = new byte[pos - 1 - this.markIndex];
					data.position(this.markIndex);
					data.get(b);
					String uri = new String(b);
					context.setUri(uri);
					lastElementPos = pos - 1;					
				}
				break;
				
			case URL_VERSION_SPACE:
				if(HttpToken.isPrintable(ch)) {
					this.markIndex = pos - 1;
					this.state = State.VERSION;
				} else if(HttpToken.isControl(ch)) {
 					context.setVersion("HTTP/0.9");
					this.state = State.END;
				}
				break;
				
			case VERSION:
				if(ch == HttpToken.CARRIAGE_RETURN || ch == HttpToken.LINE_FEED) {
					byte[] b = new byte[pos - 1 - this.markIndex];
					data.position(this.markIndex);
					data.get(b);
					String version = new String(b);
					lastElementPos = pos - 1;
					context.setVersion(version);
					
					this.previousCh = ch;
					this.state = State.HEADER;
					continue;
				}
				break;
			}
		}
		
		this.pos = pos;
		return lastElementPos;
	}
	
	/**
	 * Parses request headers from specified request data starting at specified position and length.
	 *  
	 * @param data the request data
	 * @param pos start position in request data
	 * @param length length to parse
	 * @return state of parser after parsing request data
	 * @throws IOException if an I/O exception occurs
	 * @throws HttpException if any of the request headers are invalid causing a HTTP error
	 */
	@SuppressWarnings("incomplete-switch")
	int parseHeaders(ByteBuffer data, int pos, int length) throws IOException, HttpException {
		int lastElementPos = pos;
		
		while(this.state.value < State.END_HEADERS.value && pos < length) {
			byte ch = data.get(pos++);
			
			if(this.previousCh == HttpToken.CARRIAGE_RETURN && ch == HttpToken.LINE_FEED) {
				this.previousCh = HttpToken.LINE_FEED;
				continue;
			}
			
			this.previousCh = HttpToken.NONE;
			
			switch(this.state) {
			case HEADER:
				switch(ch) {
				case HttpToken.COLON:
				case HttpToken.SPACE:
				case HttpToken.TAB:
					this.savedHeaderLength = -1;
					this.state = State.HEADER_VALUE;
					break;
				
				default:
					HttpParserHeader parserHeader = HttpParserHeader.getHttpParserHeader(this.headerName);
					
					if(parserHeader != null) {
						switch(parserHeader) {
						case CONTENT_TYPE:
							handleContentTypeHeader(headerValue);
							break;
							
						case CONTENT_LENGTH:
							handleContentLengthHeader(headerValue);
							break;
							
						case TRANSFER_ENCODING:
							handleTransferEncodingHeader(headerValue);
							break;
						
						case EXPECT:
							handleExpectHeader(headerValue);
							break;
						}
					}
					
                	if(ch == HttpToken.CARRIAGE_RETURN || ch == HttpToken.LINE_FEED) {
						// End of header
						
						if(this.contentLength == UNKNOWN_TRANSFER_ENCODING) {
							this.contentLength = NO_TRANSFER_ENCODING;
						}
						
						if(this.contentLength > 2097152) {
							throw new HttpException(HttpStatus.REQUEST_ENTITY_TOO_LARGE, "content length > 2097152");
						}
						
	                    this.contentPosition = 0;
	                    
	                    if(this.contentLength == CHUNKED_TRANSFER_ENCODING) {
	                    	this.content = new ExpandableBuffer(2097152);
	                    } else {
	                    	this.content = new FixedBuffer((int)this.contentLength);
	                    }
	                    
	                    this.previousCh = ch;
	                    
	                    if(this.contentLength == CHUNKED_TRANSFER_ENCODING) {
	                    	this.state = State.CHUNKED_CONTENT;
	                    } else if(this.contentLength == NO_TRANSFER_ENCODING) {
	                    	this.state = State.END;
	                    } else {
	                    	if(this.multipartParser != null) {
	                    		this.state = State.MULTIPART_CONTENT;
	                    		
	                    		if(this.contentLength > NO_TRANSFER_ENCODING) {
	                    			multipartParser.setContentLength((int)this.contentLength);
	                    			connection.setUploadTracker(multipartParser);
	                    		}
	                    	} else {
	                    		this.state = State.CONTENT;
	                    	}
	                    	break;
	                    }
	                    
	                    this.pos = pos;
	                    return pos;
					} else {
						// New header
						this.savedHeaderLength = 1;
						this.markIndex = pos - 1;
						this.state = State.HEADER_NAME;
					}
					break;
                }	
				break;
			
			case HEADER_NAME:
				switch(ch) {
				case HttpToken.CARRIAGE_RETURN:
				case HttpToken.LINE_FEED:
					if(this.savedHeaderLength > 0) {
						byte[] b = new byte[pos - 1 - this.markIndex];
						data.position(this.markIndex);
						data.get(b);
						this.headerName = new String(b);
						lastElementPos = pos - 1;
					}
					
					this.previousCh = ch;
					this.state = State.HEADER;
					break;
					
				case HttpToken.COLON:
					if(this.savedHeaderLength > 0) {
						byte[] b = new byte[pos - 1 - this.markIndex];
						data.position(this.markIndex);
						data.get(b);
						this.headerName = new String(b);
						lastElementPos = pos - 1;
					}
					
					this.savedHeaderLength = -1;
					this.state = State.HEADER_VALUE;
					break;
					
				case HttpToken.SPACE:
				case HttpToken.TAB:
					break;
					
				default: 
					if(this.savedHeaderLength == -1) {
						this.markIndex = pos - 1;
						this.savedHeaderLength = pos - 1 - this.markIndex;
					} else {
						this.savedHeaderLength++;
					}
					break;
				}
				break;
				
			case HEADER_VALUE:
				switch(ch) {
				case HttpToken.CARRIAGE_RETURN:
				case HttpToken.LINE_FEED:
					if(this.savedHeaderLength > -1) {
						byte[] b = new byte[pos - 1 - this.markIndex];
						data.position(this.markIndex);
						data.get(b);
						this.headerValue = new String(b);
						lastElementPos = pos - 1;
						context.header(this.headerName, this.headerValue);
					}
					
					this.previousCh = ch;
					this.state = State.HEADER;
					break;
					
				case HttpToken.SPACE:
				case HttpToken.TAB:
					break;
					
				default:
					if(this.savedHeaderLength == -1) {
						this.markIndex = pos - 1;
						this.savedHeaderLength = pos - 1 - this.markIndex;
					} else {
						this.savedHeaderLength++;
					}
					break;
				}
				break;
			}
		}
		
		this.pos = pos;
		return lastElementPos;
	}
	
	/**
	 * Parses partial or complete request body from specified request data start at specified position and
	 * of specified length.
	 * 
	 * @param data the request data 
	 * @param pos start position in request data
	 * @param length length to parse
	 * @return state of parser after parsing request data
	 * @throws IOException if an I/O error occurs
	 * @throws HttpException if request body data is invalid causing a HTTP error
	 */
	@SuppressWarnings("incomplete-switch")
	int parseBody(ByteBuffer data, int pos, int length) throws IOException, HttpException {
		int bufLen = length;
		length -= pos;
		data.position(pos);
		
		byte ch = data.get(pos);
		
		if(this.previousCh == HttpToken.CARRIAGE_RETURN && ch == HttpToken.LINE_FEED) {
			this.previousCh = HttpToken.NONE;
			pos++;
			length--;
		}
				
		while(this.state.value < State.END.value && pos < bufLen) {
			ch = data.get(pos);
			
			switch(this.state) {
			case CONTENT:
			case MULTIPART_CONTENT:
				
				byte[] outData = extractContent(data, length, bufLen, pos);
				
				if(this.state == State.CONTENT) {
					content.put(outData);
				} else {
					multipartParser.parse(outData);
				}
				
				this.contentPosition += outData.length;
				
				if(this.contentPosition == this.contentLength) {
					if(this.state == State.CONTENT) {
						content.compact();
						context.content((int)this.contentLength, this.content);
					} else {
						context.multipart(multipartParser.getFieldParts(), multipartParser.getFileParts());
					}
					
					this.state = State.END;
				}
				
				pos += outData.length;
				return pos;
				
			case CHUNKED_CONTENT:
				if(length > bufLen - pos) {
					length = bufLen - pos;
				}
				
				data.position(pos);
				outData = new byte[length];
				data.get(outData);
				boolean done = chunkedParser.parse(outData);
				pos += length;
				
				if(done) {
					this.state = State.END;
				}
				break;
			}
			
			pos++;
		}
		
		if(pos > bufLen) {
			pos = bufLen;
		}
		
		this.pos = pos;
		return pos;
	}
	
	private byte[] extractContent(ByteBuffer data, int length, int bufLen, int pos) {
		long remaining = this.contentLength - this.contentPosition;
		
		if(remaining == 0) {
			this.state = State.END;
			return null;
		}
		
		if(length > remaining) {
			length = (int)remaining;
		}
		
		if(length > bufLen - pos) {
			length = bufLen - pos;
		}
		
		byte[] content = new byte[length];
		data.position(pos);
		data.get(content);
		return content;
	}
	
	private void handleContentTypeHeader(String headerValue) throws HttpException {
		if(headerValue != null && headerValue.startsWith("multipart")) {
			try {
				Header header = new Header(this.headerName, this.headerValue);
				HeaderParameters parameters = header.getParameters();
				String boundary = parameters.get(0).getSubParameter("boundary");
				this.multipartParser = new MultipartParser();
				multipartParser.setBoundary(boundary);
			} catch(HeaderException e) {
				throw new HttpException(HttpStatus.BAD_REQUEST, "invalid content type header");
			}
		}		
	}
	
	private void handleContentLengthHeader(String headerValue) throws HttpException {
		if(this.contentLength != CHUNKED_TRANSFER_ENCODING) {
			try {
				this.contentLength = Long.parseLong(this.headerValue);
				context.contentLengthSet(true);
			} catch(NumberFormatException e) {
				throw new HttpException(HttpStatus.BAD_REQUEST);
			}
			
			if(this.contentLength <= 0) {
                this.contentLength = NO_TRANSFER_ENCODING;
			}
		}		
	}
	
	private void handleTransferEncodingHeader(String headerValue) throws HttpException {
		if(headerValue != null && headerValue.toLowerCase().equals("chunked")) {
			this.contentLength = CHUNKED_TRANSFER_ENCODING;
			this.chunkedParser = new ChunkedParser(this.context);
		} else if(headerValue != null && headerValue.toLowerCase().endsWith("chunked")) {
			this.contentLength = CHUNKED_TRANSFER_ENCODING;
			this.chunkedParser = new ChunkedParser(this.context);
		} else if(headerValue != null && headerValue.toLowerCase().indexOf("chunked") >= 0) {
			throw new HttpException(HttpStatus.BAD_REQUEST);
		}		
	}
	
	private void handleExpectHeader(String headerValue) {
		if(headerValue != null && headerValue.toLowerCase().equals("100-continue")) {
			this.sendExpect100Continue = true;
		} else {
			this.sendExpectationFailed = true;
		}		
	}
}
