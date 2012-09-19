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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;


public class CombinedLogEntry {
	
	private static final String DATE_FORMAT = "dd/MMM/yyyy:HH:mm:ss Z";
	
	private String remoteAddr;
	
	private String remoteUser;
	
	private Date timeLocal;
	
	private RequestField request;
	
	private int status;
	
	private int bodyBytesSent;
	
	private String referer;
	
	private String userAgent;
	
	public CombinedLogEntry(String entry) throws LogException {
		super();
		parse(entry);
	}
	
	void parse(String entry) throws LogException {
		try {
			StringTokenizer matcher = new StringTokenizer(entry);
			
			// Read remote address delimited by a " " after the field
			this.remoteAddr = matcher.nextToken();
			
			// Eat the "-" between remote addr and remote user
			this.remoteUser = matcher.nextToken();
			
			// Eat the "[" that marks the start of the time local field
			matcher.nextToken("[");
			
			// Read and parse time local field
			SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
			this.timeLocal = format.parse(matcher.nextToken("]").substring(1));
			
			// Eat the '"' between the time local and request fields
			matcher.nextToken("\"");
			
			// Read the request field
			String requestStr = matcher.nextToken("\"");
			this.request = new RequestField(requestStr);
			
			// Eat the " " between the request and status fields
			matcher.nextToken(" ");
			
			// Read the status field
			try {
				this.status = Integer.parseInt(matcher.nextToken(" "));
			} catch(NumberFormatException e) {
				throw new LogException("Can't parse entry status code", e);
			}
			
			// Read the body bytes sent field
			try {
				this.bodyBytesSent = Integer.parseInt(matcher.nextToken());
			} catch(NumberFormatException e) {
				throw new LogException("Can't parse entry body bytes sent", e);
			}
			
			// Eat the '"' that starts the referer field
			matcher.nextToken("\"");
			
			this.referer = matcher.nextToken("\"");
			
			// Eat the '"' that starts the user agent field
			matcher.nextToken("\"");
			
			this.userAgent = matcher.nextToken("\"");
		} catch(ParseException e) {
			throw new LogException("Can't parse entry local timestamp", e);
		}		
	}
	
	public String getRemoteAddr() {
		return this.remoteAddr;
	}
	
	public String getRemoteUser() {
		return this.remoteUser;
	}
	
	public Date getTimeLocal() {
		return this.timeLocal;
	}
	
	public RequestField getRequest() {
		return this.request;
	}
	
	public int getStatus() {
		return this.status;
	}
	
	public int getBodyBytesSent() {
		return this.bodyBytesSent;
	}
	
	public String getReferer() {
		return this.referer;
	}
	
	public String getUserAgent() {
		return this.userAgent;
	}
	
	public String toString() {
		return this.remoteAddr;
	}
}
