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

package com.nginious.http.session;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import com.nginious.http.HttpCookie;
import com.nginious.http.HttpRequest;
import com.nginious.http.common.Base64Utils;
import com.nginious.http.common.StringUtils;

/*
 * Deserializes HTTP sessions from HTTP cookies found in HTTP requests. The following procedure is
 * used to deserialized HTTP sessions.
 *
 * <ul>
 * <li>The HTTP request is searched for cookies whose name starts with "http_session_".</li>
 * <li>The found cookies are sorted in name order.</li>
 * <li>The values of all cookies are concatenated into a string in cookie name order.</li>
 * <li>The string is based64 decoded and AES decrypted into binary form.</li>
 * <li>The binary data is deserialized into HTTP attribute names and values.</li>
 * </ul>
 * 
 */
class HttpCookieSessionDeserializer {
	
	/*
	 * Deserializes HTTP session from specified HTTP request.
	 */
	static HttpSessionImpl deserialize(HttpRequest request) throws IOException {
		Collection<HttpCookie> cookies = extractSessionCookies(request);
		
		if(cookies == null || cookies.size() == 0) {
			return null;
		}
		
		StringBuffer data = new StringBuffer();
		
		for(HttpCookie cookie : cookies) {
			data.append(cookie.getValue().replace("%", "="));
		}
		
		return deserialize(data.toString());
	}
	
	/*
	 * Deserializes HTTP session from specified encoded string. 
	 */
	static HttpSessionImpl deserialize(String encoded) throws IOException {
		byte[] decoded = Base64Utils.decode(encoded);
		
		ByteArrayInputStream bytes = new ByteArrayInputStream(decoded);
		ObjectInputStream in = createInputStream(bytes);
		
		int magicNumber = in.readInt();
		
		if(magicNumber != HttpSessionConstants.MAGIC_NUMBER) {
			throw new IOException("Can't decrypt cookie data, bad magic number at start '" + magicNumber + "'");
		}
		
		int version = in.readByte();
		
		if(version != HttpSessionConstants.VERSION) {
			return null;
		}
		
		long createTime = in.readLong();
		int objectCount = in.readInt();
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		
		for(int i = 0; i < objectCount; i++) {
			String key = (String)readObject(in);
			Serializable value = readObject(in);
			attributes.put(key, value);
		}
		
		magicNumber = in.readInt();

		if(magicNumber != HttpSessionConstants.MAGIC_NUMBER) {
			throw new IOException("Can't decrypt cookie data, bad magic number at end '" + magicNumber + "'");
		}
		
		return new HttpSessionImpl(attributes, createTime);		
	}
	
	/*
	 * Extracts all cookies whose name starts with "http_session_" from specified HTTP request. The cookies
	 * are sorted in ascending name order.
	 */
	private static Collection<HttpCookie> extractSessionCookies(HttpRequest request) {
		HttpCookie[] cookies = request.getCookies();
		
		if(cookies == null) {
			return null;
		}
		
		TreeMap<String, HttpCookie> outCookies = new TreeMap<String, HttpCookie>();
		
		for(HttpCookie cookie : cookies) {
			if(cookie.getName().startsWith(HttpSessionConstants.COOKIE_PREFIX)) {
				outCookies.put(cookie.getName(), cookie);
			}
		}
		
		return outCookies.values();
	}
	
	private static Serializable readObject(ObjectInputStream in) throws IOException {
		try {
			return (Serializable)in.readObject();
		} catch(ClassNotFoundException e) {
			throw new IOException("Unable to create object from cookie", e);
		}
	}
	
	private static ObjectInputStream createInputStream(ByteArrayInputStream bytes) throws IOException {
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(StringUtils.convertHexStringToBytes(HttpSessionConstants.KEY), "AES");
			
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			
			CipherInputStream cIn = new CipherInputStream(bytes, cipher);
			GZIPInputStream gzip = new GZIPInputStream(cIn);
			ObjectInputStream in = new ObjectInputStream(gzip);
			return in;
		} catch(NoSuchPaddingException e) {
			throw new IOException("Unable to initialize AES decryption", e);						
		} catch(NoSuchAlgorithmException e) {
			throw new IOException("Unable to initialize AES decryption", e);			
		} catch(InvalidKeyException e) {
			throw new IOException("Unable to initialize AES decryption", e);			
		}
	}
}
