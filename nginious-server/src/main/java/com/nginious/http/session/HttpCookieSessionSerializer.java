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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import com.nginious.http.HttpCookie;
import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.HttpSession;
import com.nginious.http.common.Base64Utils;
import com.nginious.http.common.StringUtils;

/*
 * Serializes a HTTP session into one or more cookies. The following procedure is used to serializes
 * the session.
 * 
 * <ul>
 * <li>Each session attribute name and attribute value is serialized into binary form.</li>
 * <li>The serialized binary data is AES encrypted and base64 encoded.</li>
 * <li>The base64 data is divided into chunks of 2048 bytes each and each chunk is added to a separate HTTP cookie.</li>
 * <li>Each cookie is names "http_session_'x'" where x is replaced with the chunk index.</li>
 * </ul>
 */
class HttpCookieSessionSerializer {
	
	/*
	 * Serializes the specified HTTP session and adds the HTTP cookies to the specified HTTP response.
	 * The cookie path is set to the specified path.
	 */
	static void serialize(HttpSession session, HttpResponse response, String path) throws IOException {
		String encoded = serialize(session);
		
		if(encoded == null) {
			return;
		}
		
		int postfix = 1;
		
		for(int i = 0; i < encoded.length(); i += 2048) {
			int length = encoded.length() - i > 2048 ? 2048 : encoded.length() - i;
			String data = encoded.substring(i, i + length).replace('=', '%');
			HttpCookie cookie = new HttpCookie(HttpSessionConstants.COOKIE_PREFIX + postfix, data);
			postfix++;
			cookie.setPath(path);
			cookie.setMaxAge(HttpSessionConstants.MAX_AGE);
			response.addCookie(cookie);
		}		
	}
	
	/*
	 * Invalidates the specified HTTP session and removes all cookies associated with the session from the specified
	 * HTTP request and HTTP response.
	 */
	static void invalidate(HttpSession session, HttpRequest request, HttpResponse response, String path) throws IOException {
		HttpCookie[] cookies = request.getCookies();
		
		for(HttpCookie cookie : cookies) {
			if(cookie.getName().startsWith(HttpSessionConstants.COOKIE_PREFIX)) {
				HttpCookie outCookie = new HttpCookie(cookie.getName(), cookie.getValue());
				outCookie.setMaxAge(-HttpSessionConstants.MAX_AGE);
				outCookie.setPath(path);
				response.addCookie(outCookie);
			}
		}
	}
	
	/*
	 * Serializes the specified HTTP session into a string. The following procedure is used to serializes
	 * the HTTP session.
	 * 
	 * <ul>
	 * <li>Each session attribute name and attribute value is serialized into binary form.</li>
	 * <li>The serialized binary data is AES encrypted and base64 encoded.</li>
	 * <li>The base64 data is divided into chunks of 2048 bytes each and each chunk is added to a separate HTTP cookie.</li>
	 * <li>Each cookie is names "http_session_'x'" where x is replaced with the chunk index.</li>
	 * </ul>
	 */
	static String serialize(HttpSession session) throws IOException {
		if(session.isInvalidated()) {
			return null;
		}
		
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ObjectOutputStream out = createOutputStream(bytes);
		
		out.writeInt(HttpSessionConstants.MAGIC_NUMBER);
		out.writeByte(HttpSessionConstants.VERSION);
		out.writeLong(session.getCreationTime());
		Set<String> names = session.getAttributeNames();
		out.writeInt(names.size());
		
		for(String name : names) {
			out.writeObject(name);
			out.writeObject(session.getAttribute(name));
		}
		
		out.writeInt(HttpSessionConstants.MAGIC_NUMBER);
		out.flush();
		out.close();
		
		String encoded = Base64Utils.encode(bytes.toByteArray());
		
		if(encoded.length() > 2048 * 10) {
			throw new IOException("Session data overflow, max size is 20480 bytes");
		}
		
		return encoded;
	}
	
	/*
	 * Creates an object output stream for serializing objects and AES encrypting the serialized data.
	 */
	private static ObjectOutputStream createOutputStream(ByteArrayOutputStream bytes) throws IOException {
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(StringUtils.convertHexStringToBytes(HttpSessionConstants.KEY), "AES");
			
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			
			CipherOutputStream cOut = new CipherOutputStream(bytes, cipher);
			GZIPOutputStream gzip = new GZIPOutputStream(cOut);
			ObjectOutputStream out = new ObjectOutputStream(gzip);
			return out;
		} catch(NoSuchPaddingException e) {
			throw new IOException("Unable to initialize AES decryption", e);						
		} catch(NoSuchAlgorithmException e) {
			throw new IOException("Unable to initialize AES decryption", e);			
		} catch(InvalidKeyException e) {
			throw new IOException("Unable to initialize AES decryption", e);			
		}
	}
}
