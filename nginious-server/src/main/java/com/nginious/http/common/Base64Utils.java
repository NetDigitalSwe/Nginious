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

package com.nginious.http.common;

import java.io.IOException;

/**
 * Support for base64 encoding and decoding
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class Base64Utils {
	
	// Mapping table from 6-bit nibbles to Base64 characters.
	private static final char[] sixBitNibblesToBase64Chars = new char[64];
	   
	// Mapping table from Base64 characters to 6-bit nibbles.
	private static final byte[] base64CharsTo6BitNibbles = new byte[128];

	static {
		int i = 0;
		
		for(char c = 'A'; c <= 'Z'; c++) {
			sixBitNibblesToBase64Chars[i++] = c;
		}
		
		for(char c = 'a'; c <= 'z'; c++) {
			sixBitNibblesToBase64Chars[i++] = c;
		}
		
		for(char c = '0'; c <= '9'; c++) {
			sixBitNibblesToBase64Chars[i++] = c;
		}
		
		sixBitNibblesToBase64Chars[i++] = '+'; 
		sixBitNibblesToBase64Chars[i++] = '/';

		for(i = 0; i < base64CharsTo6BitNibbles.length; i++) {
			base64CharsTo6BitNibbles[i] = -1;
		}
		
		for(i = 0; i < 64; i++) {
			base64CharsTo6BitNibbles[sixBitNibblesToBase64Chars[i]] = (byte)i;
		}
	}

	/**
	 * Base 64 encodes the specified byte array.
	 * 
	 * @param in the byte arrat t encode
	 * @return base64 encoded data
	 */
	public static String encode(byte[] in) {
		int len = in.length;
		
		int oDataLen = (len * 4 + 2) / 3;
		int oLen = ((len + 2) / 3) * 4;
		char[] out = new char[oLen];
		int ip = 0;
		int iEnd = len;
		int op = 0;
		
		while(ip < iEnd) {
			int i0 = in[ip++] & 0xff;
			int i1 = ip < iEnd ? in[ip++] & 0xff : 0;
			int i2 = ip < iEnd ? in[ip++] & 0xff : 0;
			int o0 = i0 >>> 2;
			int o1 = ((i0 & 3) << 4) | (i1 >>> 4);
			int o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
			int o3 = i2 & 0x3F;
			out[op++] = sixBitNibblesToBase64Chars[o0];
			out[op++] = sixBitNibblesToBase64Chars[o1];
			out[op] = op < oDataLen ? sixBitNibblesToBase64Chars[o2] : '='; op++;
			out[op] = op < oDataLen ? sixBitNibblesToBase64Chars[o3] : '='; op++;
		}
		
		return new String(out);
	}
	
	/**
	 * Base 64 decodes the specified string.
	 * 
	 * @param in the string to base 64 decode
	 * @return the base 64 decoded byte array
	 * @throws IOException if base 64 encoded string is invalid
	 */
	public static byte[] decode(String in) throws IOException {
		int len = in.length();
		
		if(len % 4 != 0) {
			throw new IOException("Length of base 64 encoded string must be a multiple of 4");
		}
		
		while(len > 0 && in.charAt(len-1) == '=') {
			len--;
		}
		
		int oLen = (len * 3) / 4;
		byte[] out = new byte[oLen];
		int ip = 0;
		int iEnd = len;
		int op = 0;
		
		while(ip < iEnd) {
			int i0 = in.charAt(ip++);
			int i1 = in.charAt(ip++);
			int i2 = ip < iEnd ? in.charAt(ip++) : 'A';
			int i3 = ip < iEnd ? in.charAt(ip++) : 'A';
			
			if(i0 > 127 || i1 > 127 || i2 > 127 || i3 > 127) {
				throw new IOException("Illegal character in base64 encoded data");
			}
			
			int b0 = base64CharsTo6BitNibbles[i0];
			int b1 = base64CharsTo6BitNibbles[i1];
			int b2 = base64CharsTo6BitNibbles[i2];
			int b3 = base64CharsTo6BitNibbles[i3];
			
			if (b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0) {
				throw new IOException("Illegal character in base64 encoded data.");
			}
			
			int o0 = (b0 << 2) | (b1 >>> 4);
			int o1 = ((b1 & 0xf) << 4) | (b2 >>> 2);
			int o2 = ((b2 & 3) << 6) |  b3;
			out[op++] = (byte)o0;
			if(op < oLen) {
				out[op++] = (byte)o1;
			}
			
			if(op < oLen) {
				out[op++] = (byte)o2;
			}
		}
			
		return out;
	}
}
