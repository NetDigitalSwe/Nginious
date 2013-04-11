package com.nginious.http.server;

import java.io.IOException;

import com.nginious.http.HttpException;
import com.nginious.http.HttpStatus;
import com.nginious.http.common.Buffer;
import com.nginious.http.common.ExpandableBuffer;

class ChunkedParser {
	
	private enum State {
		
		CHUNKED_CONTENT(1),
		
		CHUNK_LENGTH(2),
		
		CHUNK_PARAMS(3),
		
		CHUNK(4),
		
		END(5);
		
		int value;
	    
	    State(int value) {
	    	this.value = value;
	    }
	}
	
	private State state;
	
    private int chunkLength;
    
    private byte previousCh;
    
    private Buffer content;
    
    private long contentLength;
    
    private int chunkPosition;
    
    private HttpContext context;
    
    ChunkedParser(HttpContext context) {
		super();
		this.state = State.CHUNKED_CONTENT;
		this.content = new ExpandableBuffer(2097152);
		this.context = context;
	}
	
	@SuppressWarnings("incomplete-switch")
	boolean parse(byte[] dataFragment) throws IOException, HttpException {
		int pos = 0;
		
		while(this.state.value < State.END.value && pos < dataFragment.length) {
			byte ch = dataFragment[pos];
			
			if(this.previousCh == HttpToken.CARRIAGE_RETURN && ch == HttpToken.LINE_FEED) {
				this.previousCh = ch;
				pos++;
				continue;
			}
			
			switch(this.state) {
			case CHUNKED_CONTENT:
				if(ch == HttpToken.CARRIAGE_RETURN || ch == HttpToken.LINE_FEED) {
					this.previousCh = dataFragment[pos];
				} else if (ch <= HttpToken.SPACE) {
					pos++;
				} else {
					this.chunkLength = 0;
					this.chunkPosition = 0;
					this.state = State.CHUNK_LENGTH;
					continue;
                }				
				break;
				
			case CHUNK_LENGTH:
				if(ch == HttpToken.CARRIAGE_RETURN || ch == HttpToken.LINE_FEED) {
					this.previousCh = ch;
					
					if(this.chunkLength == 0) {
						this.state = State.END;
						this.contentLength = content.size();
						content.compact();
						context.content((int)this.contentLength, this.content);
						return true;
					} else {
						this.state = State.CHUNK;
					}
				} else if(ch <= HttpToken.SPACE || ch == HttpToken.SEMI_COLON) {
					this.state = State.CHUNK_PARAMS;
				} else if(ch >= '0' && ch <= '9') {
					this.chunkLength = chunkLength * 16 + (ch - '0');
				} else if(ch >= 'a' && ch <= 'f') {
					this.chunkLength = chunkLength * 16 + (10 + ch - 'a');
				} else if(ch >= 'A' && ch <= 'F') {
					this.chunkLength = chunkLength * 16 + (10 + ch - 'A');
				} else {
					throw new HttpException(HttpStatus.BAD_REQUEST, "bad chunk char: " + ch);
				}
				break;
				
			case CHUNK_PARAMS:
				if(ch == HttpToken.CARRIAGE_RETURN || ch == HttpToken.LINE_FEED) {
					this.previousCh = ch;
					
					if(this.chunkLength == 0) {
                        this.state = State.END;
                        content.compact();
                        context.content((int)this.contentLength, this.content);
                        return true;
					} else {
						this.state = State.CHUNK;
					}
				}
				break;
				
			case CHUNK:
				int remain = this.chunkLength - this.chunkPosition;
				int length = dataFragment.length - pos;
				
				if(remain == 0) {
					this.state = State.CHUNKED_CONTENT;
					break;
                } else if(length > remain) { 
                	length = remain;
                }
				
				content.put(dataFragment, pos, length);
				this.chunkPosition += length;
				pos += length;
				break;
			}
			
			pos++;
		}
		
		return false;
	}
}
