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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A representation of a URI and its subcomponents. Provides functionality for parsing a
 * text URI and breaking it up into its sub components. 
 * 
 * <p>
 * See <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396 Uniform Resource Identifiers</a>
 * for additional information.
 * </p>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class URI {
	
	private static final int STATE_NONE = 0;
	
	private static final int STATE_SCHEME_OR_PATH = 1;
	
	private static final int STATE_AUTHORITY = 2;
	
	private static final int STATE_HOST = 3;
	
	private static final int STATE_PORT = 4;
	
	private static final int STATE_PATH = 5;
	
	private static final int STATE_QUERY = 6;
	
	private static final int STATE_FRAGMENT = 7;
	
	private String uri;
	
	private String scheme;
	
	private String userinfo;
	
	private String host;
	
	private int port;
	
	private String path;
	
	private String query;
	
	private String fragment;
	
	/**
	 * Constructs a new URI from the specified uri string.
	 * 
	 * @param uri the uri string
	 */
	public URI(String uri) {
		this.uri = uri;
	}
	
	/**
	 * Returns whether or not this URI is a wildcard URI '*'.
	 * 
	 * @return <code>true</code> if this URI is a wildcard URI, <code>false</code> otherwise
	 */
	public boolean isWildcard() {
		return uri.equals("*");
	}
	
	/**
	 * Returns the scheme component of this URI.
	 * 
	 * @return the scheme.
	 */
	public String getScheme() {
		return this.scheme;
	}
	
	/**
	 * Returns the user info component of this URI.
	 * 
	 * @return the user info
	 */
	public String getUserinfo() {
		return this.userinfo;
	}
	
	/**
	 * Returns the host component of this URI.
	 * 
	 * @return the host
	 */
	public String getHost() {
		return this.host;
	}
	
	/**
	 * Returns the port of this URI.
	 * 
	 * @return the port
	 */
	public int getPort() {
		return this.port;
	}
	
	/**
	 * Returns the path component of this URI.
	 * 
	 * @return the path
	 */
	public String getPath() {
		return this.path;
	}
	
	/**
	 * Returns the query component of this URI.
	 * 
	 * @return the query
	 */
	public String getQuery() {
		return this.query;
	}
	
	/**
	 * Returns the fragment component of this URI.
	 * 
	 * @return the fragment or <code>null</code> if this URI does not contain a fragment
	 */
	public String getFragment() {
		return this.fragment;
	}
	
	/**
	 * Parses this URI from string representation.
	 * 
	 * @throws URIException if unable to parse URI
	 */
	public void parse() throws URIException {
		if(uri.equals("*")) {
			return;
		}
		
		int length = uri.length();
		int index = 0;
		int startIndex = 0;
		int startQueryIndex = -1;
		int state = STATE_NONE; // this.fromPath ? STATE_PATH : STATE_NONE;
		
		while(index < length) {
			char c = uri.charAt(index);
			
			switch(state) {
			case STATE_NONE:
				switch(c) {
				case '?':
					state = STATE_QUERY;
					startQueryIndex = index + 1;
					startIndex = index + 1;
					break;
				
				default:
					// Should really test
					if(Character.isLetter(c)) {
						state = STATE_SCHEME_OR_PATH;
						startIndex = index;
					} else {
						state = STATE_PATH;
						// throw new URIException("Invalid scheme: " + uri);
					}
					break;
				}
				break;
			
			case STATE_SCHEME_OR_PATH:
				switch(c) {
				case '#':
					this.path = extractValue(uri, startIndex, index, true);
					state = STATE_FRAGMENT;
					startIndex = index + 1;
					break;
				
				case '?':
					this.path = extractValue(uri, startIndex, index, true);
					state = STATE_QUERY;
					startQueryIndex = index + 1;
					startIndex = index + 1;
					break;
				
				case ':':
					this.scheme = extractValue(uri, startIndex, index, false);
					
					if(!scheme.matches("[a-zA-Z0-9]+")) {
						throw new URIException("Invalid scheme: " + uri);
					}
					
					if(peek(index + 1) == '/' && peek(index + 2) == '/') {
						index += 2;
					}
					
					state = STATE_AUTHORITY;
					startIndex = index + 1;
					break;
				}
				break;
				
			case STATE_AUTHORITY:
				switch(c) {
				case '@':
					this.userinfo = extractValue(uri, startIndex, index, true);
					state = STATE_HOST;
					startIndex = index + 1;
					break;
				
				case ':':
					this.host = extractValue(uri, startIndex, index, true);
					state = STATE_PORT;
					startIndex = index + 1;
					break;
					
				case '/':
					this.host = extractValue(uri, startIndex, index, true);
					state = STATE_PATH;
					startIndex = index;
					break;
				
				case '#':
					this.host = extractValue(uri, startIndex, index, true);
					state = STATE_FRAGMENT;
					startIndex = index + 1;
					break;
				
				case '?':
					this.host = extractValue(uri, startIndex, index, true);
					state = STATE_QUERY;
					startQueryIndex = index + 1;
					startIndex = index + 1;
					break;
				}
				break;
			
			case STATE_HOST:
				switch(c) {
				case ':':
					this.host = extractValue(uri, startIndex, index, true);
					state = STATE_PORT;
					startIndex = index + 1;
					break;
					
				case '/':
					this.host = extractValue(uri, startIndex, index, true);
					state = STATE_PATH;
					startIndex = index;
					break;
				
				case '#':
					this.host = extractValue(uri, startIndex, index, true);
					state = STATE_FRAGMENT;
					startIndex = index + 1;
					break;
				
				case '?':
					this.host = extractValue(uri, startIndex, index, true);
					state = STATE_QUERY;
					startQueryIndex = index + 1;
					startIndex = index + 1;
					break;
				}
				break;
			
			case STATE_PORT:
				switch(c) {
				case '/':
					this.port = Integer.parseInt(uri.substring(startIndex, index));
					state = STATE_PATH;
					startIndex = index;
					break;
				
				case '#':
					this.port = Integer.parseInt(uri.substring(startIndex, index));
					state = STATE_FRAGMENT;
					startIndex = index + 1;
					break;
				
				case '?':
					this.port = Integer.parseInt(uri.substring(startIndex, index));
					state = STATE_QUERY;
					startQueryIndex = index + 1;
					startIndex = index + 1;
					break;
				
				default:
					if(!Character.isDigit(c)) {
						throw new URIException("Invalid port: " + uri);
					}
				}
				break;
			
			case STATE_PATH:
				switch(c) {
				case '#':
					this.path = extractValue(uri, startIndex, index, true);
					state = STATE_FRAGMENT;
					startIndex = index + 1;
					break;
				
				case '?':
					this.path = extractValue(uri, startIndex, index, true);
					state = STATE_QUERY;
					startQueryIndex = index + 1;
					startIndex = index + 1;
					break;
				}
				break;
			
			case STATE_QUERY:
				switch(c) {
				case '#':
					this.query = uri.substring(startQueryIndex, index);
					state = STATE_FRAGMENT;
					startIndex = index + 1;
					break;
				}
				break;
			
			case STATE_FRAGMENT:
				break;
			}
			
			index++;
		}
		
		switch(state) {
		case STATE_NONE:
			throw new URIException("Invalid URI: " + this.uri);
		
		case STATE_SCHEME_OR_PATH:
			throw new URIException("Invalid URI: " + this.uri);
		
		case STATE_AUTHORITY:
		case STATE_HOST:
			if(startIndex == index) {
				throw new URIException("Invalid URI: " + this.uri);				
			}
			
			this.host = extractValue(uri, startIndex, index, true);
			break;
		
		case STATE_PORT:
			this.port = Integer.parseInt(uri.substring(startIndex, index));
			break;
		
		case STATE_PATH:
			this.path = extractValue(uri, startIndex, index, true);
			break;
					
		case STATE_QUERY:
			this.query = uri.substring(startQueryIndex, index);
			break;
			
		case STATE_FRAGMENT:
			this.fragment = extractValue(uri, startIndex, index, true);
			break;
		}
	}
	
	/**
	 * Extracts value from the specified uri, start index, end index and optionally URL decodes
	 * value.
	 * 
	 * @param uri the URI
	 * @param startIndex the start index
	 * @param endIndex the end index
	 * @param decode whether or not to URL decode extracted value
	 * @return the extracted value
	 */
	private String extractValue(String uri, int startIndex, int endIndex, boolean decode) {
		String value = uri.substring(startIndex, endIndex);
		
		if(decode) {
			try {
				return URLDecoder.decode(value, "utf-8");
			} catch(UnsupportedEncodingException e) {}
		}
		
		return value;
	}
	
	/**
	 * Extracts the character at the specified index from this URIs string representation.
	 * 
	 * @param index the index
	 * @return the found character or <code>0</code> if index is out of range
	 */
	private char peek(int index) {
		if(index < uri.length()) {
			return uri.charAt(index);
		}
		
		return 0;
	}
	
	/**
	 * Decodes parameters in this URIs query part. Parameters are placed in the specified parameter map. The
	 * specified encoding is used to URL decode the parameter names and value.
	 * 
	 * @param paramsMap the parameters map to place decoded parameter in
	 * @param encoding the character encoding to use when URL decoding parameter names and value
	 * @throws UnsupportedEncodingException if encoding is not supported
	 */
	public void decodeQuery(HashMap<String, List<String>> paramsMap, String encoding) throws UnsupportedEncodingException {
		if(this.query == null) {
			return;
		}
		
		String[] nameValues = query.split("&");
		
		for(String nameValue : nameValues) {
			String[] pair = nameValue.split("=");
			String name = pair[0];
			String value = pair.length > 1 ? pair[1] : null; // TODO, should value be null on no value???
			name = URLDecoder.decode(name, encoding);
			
			if(value != null) {
				value = URLDecoder.decode(value, encoding);
			}
			
			List<String> params = paramsMap.get(name);
			
			if(params == null) {
				params = new ArrayList<String>();
				paramsMap.put(name, params);
			}
			
			params.add(value);
		}
	}
}
