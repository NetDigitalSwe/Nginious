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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.nginious.http.HttpException;
import com.nginious.http.HttpStatus;
import com.nginious.http.common.FixedBuffer;
import com.nginious.http.server.CaseInsensitiveKey;
import com.nginious.http.server.Header;
import com.nginious.http.server.HeaderException;
import com.nginious.http.server.HeaderParameters;
import com.nginious.http.server.HttpToken;

/**
 * Parses multipart content into its parts.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class MultipartParser implements UploadTracker {
	
	private enum State {
		
	    BOUNDARY(1),

	    IN_BOUNDARY(2),
	    
	    HEADER(3),

	    HEADER_NAME(4),

	    HEADER_IN_NAME(5),

	    HEADER_VALUE(6),

	    HEADER_IN_VALUE(7),

	    END_HEADERS(8),

	    CONTENT(9),
	    
	    STATE_END_BOUNDARY(10),
	    
	    STATE_CONTENT_BOUNDARY(11),
	    
	    STATE_END_CONTENT(12);
	    
	    int value;
	    
	    State(int value) {
	    	this.value = value;
	    }
	}
	
    private byte[] startPartBoundary;
    
    private byte[] endPartBoundary;

    private byte[] endContentBoundary;
    
    private byte previousCh;
    
    private int contentLength;
    
    private int uploadedLength;
    
    // Parser state
    private State state;

    private int savedPos;
    
    private int incrementBodyPos;
    
    private boolean hasCrLf;
    
    private FixedBuffer savedData;
    
    private String headerName;
    
    private String headerValue;
    
    // Part state
	private HashMap<CaseInsensitiveKey, List<String>> headers;
	
	private String partName;
	
	private String partFileName;
	
    private PartImpl curPart;
    
	private HashMap<String, FieldPart> fieldParts;
	
	private HashMap<String, FilePart> fileParts;
    
	/**
	 * Constructs a new multipart parser.
	 */
    public MultipartParser() {
        super();
        this.state = State.BOUNDARY;
        this.fieldParts = new HashMap<String, FieldPart>();
        this.fileParts = new HashMap<String, FilePart>();
        this.savedData = new FixedBuffer(4096);
    }
    
    /**
     * Returns whether or not upload is finished.
     * 
     * @return <code>true</code> if upload is finished, <code>false</code> otherwise
     */
	public boolean isFinished() {
		return this.state == State.STATE_END_CONTENT;
	}

	/**
	 * Returns the total length of the uploaded content.
	 * 
	 * @return the total length
	 */
	public int getContentLength() {
		return this.contentLength;
	}

	/**
	 * Returns the uploaded length.
	 * 
	 * @return the uploaded length
	 */
	public int getUploadedLength() {
		return this.uploadedLength;
	}
	
	/**
	 * Sets the HTTP request content length to the specified length.
	 * 
	 * @param contentLength the content length
	 */
	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}

	/**
     * Returns a map with all field parts found during parsing of the multipart request.
     * 
     * @return a map containing all field parts
     */
    public HashMap<String, FieldPart> getFieldParts() {
    	return this.fieldParts;
    }
    
    /**
     * Returns a map with all file parts found during parsing of the multipart request.
     * 
     * @return a map containing all file parts
     */
    public HashMap<String, FilePart> getFileParts() {
    	return this.fileParts;
    }
    
    /**
     * Sets part boundary to the specified boundary for this multipart parser. The parser
     * will use the boundary string to identify boundaries between parts in the parsed
     * multipart content.
     * 
     * @param boundary the part boundary
     */
    public void setBoundary(String boundary) {
        this.startPartBoundary = ("--" + boundary + "\015\012").getBytes();
        this.endPartBoundary = ("\015\012--" + boundary + "\015\012").getBytes();
        this.endContentBoundary = ("\015\012--" + boundary + "--\015\012").getBytes();
    }
    
    /**
     * Parses the specified data fragment of the multipart request content.
     * 
     * @param dataFragment the multipart data fragment to parse
     * @return whether or not parsing is finished
     * @throws IOException if an I/O error occurs while parsing
     * @throws HttpException if multipart content is invalid
     */
    public boolean parse(byte[] dataFragment) throws IOException, HttpException {
    	int pos = 0;
    	
    	while(pos < dataFragment.length && state.value < State.STATE_END_CONTENT.value) {
    		if(state.value >= State.BOUNDARY.value && state.value < State.HEADER.value) {
    			pos = parseBoundary(dataFragment);
    		}
    		
    		if(state.value >= State.HEADER.value && state.value < State.END_HEADERS.value && pos < dataFragment.length) {
    			pos = parseHeaders(dataFragment, pos);
    			
    			// Skipped past CRLF and also past end of buffer, remember to skip one on next call
    			if(pos > dataFragment.length && state.value == State.CONTENT.value) {
    				this.incrementBodyPos = 1;
    			}
    		}
    		
    		if(state.value >= State.CONTENT.value && pos < dataFragment.length) {
    			if(pos == 0 && this.incrementBodyPos == 1 && state.value == State.CONTENT.value) {
    				pos++;
    				this.incrementBodyPos = 0;
    			}
    			
    			pos = parseBody(dataFragment, pos);
    		}
    	}
    	
    	this.uploadedLength += dataFragment.length;
    	return this.state == State.STATE_END_CONTENT;
    }
    
    /**
     * Parses part boundary from the specified data fragment.
     * 
     * @param dataFragment the data fragment to parse
     * @return position in data fragment up to which this method has parsed
     */
    private int parseBoundary(byte[] dataFragment) {
    	int boundaryPos = 0;
    	int pos = 0;
    	
    	if(this.savedPos > 0) {
        	boundaryPos = this.savedPos;
        }
    	
    	while(pos < dataFragment.length && state.value <= State.IN_BOUNDARY.value) {
    		byte ch = dataFragment[pos];
    		
            switch(this.state) {
            case BOUNDARY:
            	if(ch == startPartBoundary[boundaryPos]) {
            		this.state = State.IN_BOUNDARY;
            		boundaryPos++;
            	}
            	break;
            	
            case IN_BOUNDARY:
            	if(startPartBoundary[boundaryPos] == dataFragment[pos]) {
            		boundaryPos++;
            		
            		if(boundaryPos == startPartBoundary.length) {
            			this.state = State.HEADER;
            			this.savedPos = 0;
            			this.headers = new HashMap<CaseInsensitiveKey, List<String>>();
            		}
            	} else {
            		boundaryPos = 0;
            		this.state = State.BOUNDARY;
            		this.savedPos = 0;
            	}
            	break;
            }
            
            pos++;
    	}
    	
    	if(this.state == State.IN_BOUNDARY) {
    		this.savedPos = boundaryPos;
    	}
    	
    	return pos;
    }
    
    /**
     * Parses headers for a part from the specified data fragment starting at the specified position.
     * 
     * @param dataFragment the data fragment to parse
     * @param pos the position to start from in data fragment
     * @return position in data fragment up to which this method has parsed
     * @throws HttpException if any of the parsed headers are invalid
     */
	private int parseHeaders(byte[] dataFragment, int pos) throws HttpException {
		int startHeaderPos = -1;
		
		if(state.value >= State.HEADER_NAME.value && state.value <= State.HEADER_IN_VALUE.value) {
			startHeaderPos = 0;
		}
		
		while(state.value < State.END_HEADERS.value && state.value >= State.HEADER.value && pos < dataFragment.length) {
			byte ch = dataFragment[pos];
			
			if(this.previousCh == HttpToken.CARRIAGE_RETURN && ch == HttpToken.LINE_FEED) {
				this.previousCh = HttpToken.LINE_FEED;
				this.hasCrLf = true;
				pos++;
				continue;
			}
			
			this.previousCh = 0;
			
			switch(this.state) {
			case HEADER:
				switch(ch) {
				case HttpToken.COLON:
				case HttpToken.SPACE:
				case HttpToken.TAB:
					this.state = State.HEADER_VALUE;
					break;
				
				default:
                	if(ch == HttpToken.CARRIAGE_RETURN || ch == HttpToken.LINE_FEED) {
	                    this.previousCh = ch;
	                    this.curPart = createPart();
	                    this.state = State.CONTENT;
	                    this.savedPos = 0;
	                    boolean hasCrLf = this.hasCrLf;
	                    this.hasCrLf = false;
	                    return hasCrLf ? pos + 2 : pos + 1;
					} else {
						// New header
						startHeaderPos = pos;
						this.state = State.HEADER_NAME;
					}
					break;
                }	
				break;
			
			case HEADER_NAME:
				switch(ch) {
				case HttpToken.CARRIAGE_RETURN:
				case HttpToken.LINE_FEED:
					this.headerName = createHeaderValue(dataFragment, startHeaderPos, pos - startHeaderPos);
					this.previousCh = ch;
					this.state = State.HEADER;
					break;
					
				case HttpToken.COLON:
					this.headerName = createHeaderValue(dataFragment, startHeaderPos, pos - startHeaderPos);
					this.state = State.HEADER_VALUE;
					break;
					
				case HttpToken.SPACE:
				case HttpToken.TAB:
					break;
					
				default:
					if(startHeaderPos == -1) {
						startHeaderPos = pos;
					}
					
					this.state = State.HEADER_IN_NAME;
					break;
				}
				
			case HEADER_IN_NAME:
				switch(ch) {
				case HttpToken.CARRIAGE_RETURN:
				case HttpToken.LINE_FEED:
					this.headerName = createHeaderValue(dataFragment, startHeaderPos, pos - startHeaderPos);
					startHeaderPos = -1;
					
					this.previousCh = ch;
					this.state = State.HEADER;
					break;
					
				case HttpToken.COLON:
					this.headerName = createHeaderValue(dataFragment, startHeaderPos, pos - startHeaderPos);
					startHeaderPos = -1;
					this.state = State.HEADER_VALUE;
					break;
					
				case HttpToken.SPACE:
				case HttpToken.TAB:
					startHeaderPos = -1;
					this.state = State.HEADER_NAME;
					break;
				}
				break;
				
			case HEADER_VALUE:
				switch(ch) {
				case HttpToken.CARRIAGE_RETURN:
				case HttpToken.LINE_FEED:
					this.headerValue = createHeaderValue(dataFragment, startHeaderPos, pos - startHeaderPos);
					startHeaderPos = -1;
					
					addHeader(this.headerName, this.headerValue);
					
					if(headerName.equals("Content-Disposition")) {
						try {
							Header header = new Header(headerName, this.headerValue);
							HeaderParameters parameters = header.getParameters();
							this.partName = parameters.get(0).getSubParameter("name");
							this.partFileName = parameters.get(0).getSubParameter("filename");
						} catch(HeaderException e) {
							throw new HttpException(HttpStatus.BAD_REQUEST, "invalid header " + headerName + " value " + this.headerValue);
						}
					}
				
					this.previousCh = ch;
					this.state = State.HEADER;
					break;
					
				case HttpToken.SPACE:
				case HttpToken.TAB:
					break;
					
				default:
					if(startHeaderPos == -1) {
						startHeaderPos = pos;
					}
					
					this.state = State.HEADER_IN_VALUE;
					break;
				}
				break;
				
			case HEADER_IN_VALUE:
				switch(ch) {
				case HttpToken.CARRIAGE_RETURN:
				case HttpToken.LINE_FEED:
					this.headerValue = createHeaderValue(dataFragment, startHeaderPos, pos - startHeaderPos);
					this.headerValue = headerValue.trim();
					
					startHeaderPos = -1;
					addHeader(this.headerName, this.headerValue);
					
					if(headerName.equals("Content-Disposition")) {
						try {
							Header header = new Header(headerName, this.headerValue);
							HeaderParameters parameters = header.getParameters();
							this.partName = parameters.get(0).getSubParameter("name");
							this.partFileName = parameters.get(0).getSubParameter("filename");
						} catch(HeaderException e) {
							throw new HttpException(HttpStatus.BAD_REQUEST, "invalid header " + headerName + " value " + this.headerValue);
						}
					}
					
					this.previousCh = ch;
					this.state = State.HEADER;
					break;
					
				case HttpToken.SPACE:
				case HttpToken.TAB:
					this.state = State.HEADER_VALUE;
					break;
				}
                break;
			}
			
			pos++;
		}
		
		if(state.value < State.CONTENT.value && startHeaderPos != -1) {
			savedData.put(dataFragment, startHeaderPos, pos - startHeaderPos);
		}
		
		return pos;
	}
	
	/**
	 * Adds a header with the specified name and value to the part currently being parsed.
	 * 
	 * @param name the header name
	 * @param value the header value
	 */
	void addHeader(String name, String value) {
		List<String> values = headers.get(name);
		
		if(values == null) {
			values = new ArrayList<String>();
			headers.put(new CaseInsensitiveKey(name), values);
		}
		
		values.add(value);		
	}
	
	/**
	 * Creates part from parsed data.
	 * 
	 * @return the created part
	 */
	private PartImpl createPart() {
		PartImpl part = null;
		
		if(this.partFileName != null) {
			FilePartImpl filePart = new FilePartImpl(this.partName);
			filePart.setHeaders(this.headers);
			filePart.setFilename(this.partFileName);
			fileParts.put(this.partName, filePart);
			part = filePart;
		} else {
			FieldPart fieldPart = new FieldPart(this.partName);
			fieldPart.setHeaders(this.headers);
			fieldParts.put(this.partName, fieldPart);
			part = fieldPart;
		}
		
		this.partName = null;
		this.partFileName = null;
		this.headers = null;
		return part;
	}
	
	/**
	 * Creates header value from the specified data starting at the specified start
	 * offset in the data.
	 * 
	 * @param data the data to create header value from
	 * @param start the start offset in data
	 * @param len the length to copy from data
	 * @return the created header value
	 */
	String createHeaderValue(byte[] data, int start, int len) {
		if(savedData.remaining() < savedData.size()) {
			savedData.put(data, start, len);
			String value = new String(savedData.toByteArray());
			savedData.reset();
			return value;
		}
		
		return new String(data, start, len);
	}
	
	/**
	 * Parsed body content for a part from the specified data fragment starting at the specified position.
	 * 
	 * @param dataFragment the data fragment to parse
	 * @param pos the position to start from in the data fragment
	 * @return position in the data fragment up to which this method has parsed
	 * @throws IOException if an I/O exception occurs while parsing body content
	 */
	int parseBody(byte[] dataFragment, int pos) throws IOException {
		int startBodyPos = pos;
		int boundaryPos = 0;
		boolean posSaved = false;
		
		if(this.savedPos > 0) {
			boundaryPos = this.savedPos;
			this.savedPos = 0;
			posSaved = true;
		}
		
		while(state.value < State.STATE_END_CONTENT.value && state.value >= State.CONTENT.value && pos < dataFragment.length) {
			byte ch = dataFragment[pos];
			
			switch(this.state) {
			case CONTENT:
				if(ch == endPartBoundary[boundaryPos]) {
					boundaryPos++;
					this.state = State.STATE_END_BOUNDARY;
				} else {
					this.state = State.CONTENT;
				}
				break;
				
			case STATE_END_BOUNDARY:
				if(ch == endPartBoundary[boundaryPos]) {
					boundaryPos++;
					
					if(boundaryPos == endPartBoundary.length) {
						this.state = State.HEADER;
						int len = pos + 1 - startBodyPos - endPartBoundary.length;
						
						if(len > 0) {
							curPart.content(dataFragment, startBodyPos, len);
							curPart.end();
						}
						
						this.curPart = null;
            			this.headers = new HashMap<CaseInsensitiveKey, List<String>>();
						this.savedPos = 0;
						boundaryPos = 0;
					}
				} else if(ch == endContentBoundary[boundaryPos]) {
					boundaryPos++;
					this.state = State.STATE_CONTENT_BOUNDARY;
				} else {
					if(posSaved) {
						curPart.content(endPartBoundary, 0, boundaryPos);
						posSaved = false;
					}
					
					boundaryPos = 0;
					this.state = State.CONTENT;
				}
				break;
				
			case STATE_CONTENT_BOUNDARY:
				if(ch == endContentBoundary[boundaryPos]) {
					boundaryPos++;
					
					if(boundaryPos == endContentBoundary.length) {
						this.state = State.STATE_END_CONTENT;
						int len = pos + 1 - startBodyPos - endContentBoundary.length;
						
						if(len > 0) {
							curPart.content(dataFragment, startBodyPos, len);
							curPart.end();
						}
						
						this.curPart = null;
						this.savedPos = 0;
						boundaryPos = 0;
					}
				} else {
					if(posSaved) {
						curPart.content(endContentBoundary, 0, boundaryPos);
						posSaved = false;
					}
					
					boundaryPos = 0;
					this.state = State.CONTENT;
				}
				break;
			}
			
			pos++;
		}
		
		if(this.state == State.STATE_CONTENT_BOUNDARY || this.state == State.STATE_END_BOUNDARY) {
			this.savedPos = boundaryPos;
			int len = dataFragment.length - startBodyPos - boundaryPos;
			
			if(len > 0) {
				curPart.content(dataFragment, startBodyPos, len);
			}
		} else if(state.value >= State.CONTENT.value && state.value < State.STATE_END_CONTENT.value) {
			int len = dataFragment.length - startBodyPos;
			curPart.content(dataFragment, startBodyPos, len);
		}
		
		return pos;
	}	
}
