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
 * Tokens used by HTTP parser.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 */
public class HttpToken {
	
	public static final byte NONE = 0x00;
	
	public static final byte COLON = (byte)':';
	
	public static final byte SPACE = (byte)0x20;
    
	public static final byte CARRIAGE_RETURN = (byte)0x0D;
    
	public static final byte LINE_FEED = (byte)0x0A;
    
	public static final byte SEMI_COLON = (byte)';';
    
	public static final byte TAB = (byte)0x09;
	
	public static boolean isPrintable(byte ch) {
		return ch > HttpToken.SPACE || ch < 0;
	}
	
	public static boolean isControl(byte ch) {
		return ch < HttpToken.SPACE && ch >= 0;
	}
}
