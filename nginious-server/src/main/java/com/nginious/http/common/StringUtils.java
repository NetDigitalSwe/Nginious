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

import java.util.Random;

/**
 * String utility methods.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class StringUtils {

	private static String hexChars = "0123456789ABCDEF";

	private static String pwdChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvxyz!";
	
	private static String asciiChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvxyz";
	
	/**
	 * Generates a random password with the specified length. The generated password contains
	 * characters from the following set [A-Za-z0-9!].
	 * 
	 * @param length password length
	 * @return the generated password
	 */
	public static String generatePassword(int length) {
		StringBuffer pwd = new StringBuffer();
		Random rnd = new Random();
		
		for(int i = 0; i < length; i++) {
			pwd.append(pwdChars.charAt(rnd.nextInt(pwdChars.length())));
		}
		
		return pwd.toString();
	}
	
	/**
	 * Generates a random ascii string with the specified length. The generated string contains
	 * characters from the following set [A-Za-z0-9].
	 * 
	 * @param length ascii length
	 * @return the generated ascii string
	 */
	public static String generateAscii(int length) {
		StringBuffer ascii= new StringBuffer();
		Random rnd = new Random();
		
		for(int i = 0; i < length; i++) {
			ascii.append(asciiChars.charAt(rnd.nextInt(asciiChars.length())));
		}
		
		return ascii.toString();		
	}
	
	/**
	 * Converts the specified byte array into a hex string.
	 * 
	 * @param data the byte array
	 * @return the hex string
	 */
	public static String asHexString(byte[] data) {
		StringBuffer hexString = new StringBuffer();

		for (int i = 0; i < data.length; i++) {
			int value = data[i] & 0xFF;
			int mostSign = value / 16;
			int leastSign = value % 16;
			hexString.append(hexChars.substring(mostSign, mostSign + 1));
			hexString.append(hexChars.substring(leastSign, leastSign + 1));
		}

		return hexString.toString();
	}

	/**
	 * Converts the specified byte array to a hex string and optionally converts characters
	 * in the generated hex string to lower case.
	 * 
	 * @param data the byte array
	 * @param toLower whether or not to convert characters to lower case
	 * @return the hex string
	 */
	public static String asHexString(byte[] data, boolean toLower) {
		String hex = asHexString(data);
		
		if(toLower) {
			return hex.toLowerCase();
		}
		
		return hex;
	}
	
	/**
	 * Converts the specified hex string to a byte array.
	 * 
	 * @param hex the hex string
	 * @return the byte arrat
	 */
	public static byte[] convertHexStringToBytes(String hex) {
        int len = hex.length() / 2;
        byte[] buf = new byte[len];
        
        for (int i = 0; i < len; i++) {
                buf[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        
        return buf;
    }	
}
