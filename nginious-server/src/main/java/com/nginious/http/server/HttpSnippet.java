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

/**
 * Static HTTP protocol snippets.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
enum HttpSnippet {
	
	/**
	 * Carriage return
	 */
	CR(getBytes("\015")),
	
	/**
	 * Line feed
	 */
	LF(getBytes("\012")),
	
	/**
	 * Carriage return, line feed
	 */
	CRLF(getBytes("\015\012")),
	
	/**
	 * Header delimiter
	 */
	HDEL(getBytes(": ")),
	
	/**
	 * Connection close header
	 */
	CONNECTION_CLOSE(getBytes("Connection: close\015\012")),
	
	/**
	 * Connection keep alive header
	 */
	CONNECTION_KEEP_ALIVE(getBytes("Connection: keep-alive\015\012")),
	
	/**
	 * Connection upgrade header
	 */
	CONNECTION_UPGRADE(getBytes("Connection: Upgrade\015\012")),
	
	/**
	 * Server header
	 */
	SERVER(getBytes("Server: Nginious/1.0.0\015\012")),
	
	/**
	 * Set cookie header
	 */
	COOKIE(getBytes("Set-Cookie: ")),
	
	/**
	 * End chunk header
	 */
	END_CHUNK(getBytes("0\015\012\015\012")),
	
	/**
	 * HTTP 1.1 protocol version
	 */
	VERSION(getBytes("HTTP/1.1 "));
	
	byte[] bytes;
	
	/**
	 * Constructs a new HTTP snippet from the specified bytes.
	 * 
	 * @param bytes the HTTP snippet bytes
	 */
	HttpSnippet(byte[] bytes) {
		this.bytes = bytes;
	}
	
	/**
	 * Encodes the specified string into bytes using the iso-8859-1 encoding. Any exceptions are handled internally.
	 * 
	 * @param str the string to encode
	 * @return a byte array containing the encoded string
	 */
	private static byte[] getBytes(String str) {
		try { return str.getBytes("iso-8859-1"); } catch(UnsupportedEncodingException e) { return str.getBytes(); }
	}
	
}
