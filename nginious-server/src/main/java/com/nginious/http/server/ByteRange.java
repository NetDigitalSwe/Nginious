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

/**
 * Represents a byte range as specified in section 14.35.1 in <a href="http://www.ietf.org/rfc/rfc2616.txt">HTTP/1.1 RFC 2616</a>.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class ByteRange {
	
	private static final String regexp = "[0-9]*-[0-9]*|[0-9]+";
	
	private String byteRangeSpec;
	
	private int start;
	
	private int end;
	
	/**
	 * Constructs a new byte range from the specified byte range specification using th specified
	 * content length as reference.
	 * 
	 * @param byteRangeSpec the byte range specification
	 * @param contentLength the content length
	 * @throws HeaderException if unable to parse byte range specification
	 */
	ByteRange(String byteRangeSpec, int contentLength) throws HeaderException {
		this.byteRangeSpec = byteRangeSpec;
		
		if(!byteRangeSpec.matches(regexp)) {
			throw new HeaderException("Invalid byte range set format " + byteRangeSpec);
		}
		
		String[] startEnd = byteRangeSpec.split("-");
		
		try {
			if(byteRangeSpec.indexOf("-") == -1) {
				this.start = Integer.parseInt(startEnd[0]);
				this.end = this.start;
			} else {
				if(byteRangeSpec.startsWith("-")) {
					this.start = contentLength - Integer.parseInt(startEnd[1]);
					this.end = contentLength - 1;
				} else if(byteRangeSpec.endsWith("-")) {
					this.start = Integer.parseInt(startEnd[0]);
					this.end = contentLength - 1;
				} else {
					this.start = Integer.parseInt(startEnd[0]);
					this.end = Integer.parseInt(startEnd[1]);
					
					if(this.start > this.end) {
						throw new HeaderException("Invalid byte range set format " + byteRangeSpec);
					}
					
					if(this.start < contentLength && this.end >= contentLength) {
						this.end = contentLength - 1;
						this.byteRangeSpec = this.start + "-" + this.end;
					}
				}
			}
		} catch(NumberFormatException e) {
			throw new HeaderException("Invalid byte range set format " + byteRangeSpec);
		}
		
		if(this.start > this.end) {
			throw new HeaderException("Invalid byte range set format " + byteRangeSpec);
		}
	}
	
	/**
	 * Returns start value in this byte range
	 * 
	 * @return the start value
	 */
	public int getStart() {
		return this.start;
	}
	
	/**
	 * Returns end value in this byte range
	 * 
	 * @return the end value
	 */
	public int getEnd() {
		return this.end;
	}
	
	/**
	 * Returns whether or not the specified position is inside this byte range. A position is
	 * within this byte range if the position is more or equals to the start value and less
	 * or equals to the end value.
	 * 
	 * @param pos the position
	 * @return <code>true</code> if the position is within this byte range, <code>false</code> otherwise
	 */
	public boolean includes(int pos) {
		return pos >= this.start && pos <= this.end;
	}
	
	public String toString() {
		return this.byteRangeSpec;
	}
}
