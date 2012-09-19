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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.nginious.http.HttpException;
import com.nginious.http.HttpMethod;
import com.nginious.http.HttpService;
import com.nginious.http.common.StringUtils;

public class DigestAuthentication extends HttpService {
	
	public DigestAuthentication() {
		super();
	}
	
	public boolean response(String credentials, HttpMethod method, String password) throws HttpException {
		try {
			boolean auth = false;
			Digest digest = createDigest(credentials, method);
			
			if(password != null) {
				auth = digest.check(password);
			}
			
			return auth;
		} catch(HeaderException e) {
			return false;
		}
	}
	
	public String challenge(String realm, String domain) {
		StringBuffer value = new StringBuffer();
		value.append("Digest realm=\"");
		value.append(realm);
		value.append("\", domain=\"");
		value.append(domain);
		value.append("\", nonce=\"");
		String nonce = createNonce();
		value.append(nonce);
		value.append("\", algorithm=\"MD5\", qop=\"auth\"");
		return value.toString();
	}
	
	private String createNonce() {
		long ts = System.currentTimeMillis();
		long sk = this.hashCode() ^ System.currentTimeMillis();
		
		byte[] nounce = new byte[24];
		
		for(int i = 0; i < 8; i++) {
            nounce[i] = (byte)(ts&0xff);
            ts= ts >> 8;
			nounce[8+i] = (byte)(sk&0xff);
			sk = sk >> 8;
		}
		
		byte[] hash=null;
		
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.reset();
			md.update(nounce,0,16);
			hash = md.digest();
        } catch(NoSuchAlgorithmException e) {
        	throw new RuntimeException("No MD5 digest algorithm found");
        }
        
        for(int i = 0; i < hash.length; i++) {
        	nounce[8+i]=hash[i];
        	
        	if(i == 23) {
        		break;
        	}
        }
        
        return StringUtils.asHexString(nounce, true);        
	}
        
	private Digest createDigest(String credentials, HttpMethod method) throws HeaderException {
		Digest digest = new Digest();
		digest.setMethod(method.toString());
		
		Header authentication = new Header("Authorization", credentials);
		HeaderParameters parameters = authentication.getParameters();
		
		digest.setUsername(parameters.getParameter("username").getValue());
		HeaderParameter param = parameters.getParameter("realm");
		digest.setRealm(param == null ? null : param.getValue());
		digest.setNonce(parameters.getParameter("nonce").getValue());
		digest.setNc(parameters.getParameter("nc").getValue());
		digest.setCnonce(parameters.getParameter("cnonce").getValue());
		digest.setQop(parameters.getParameter("qop").getValue());
		digest.setUri(parameters.getParameter("uri").getValue());
		digest.setResponse(parameters.getParameter("response").getValue());
		
        return digest;		
	}
}
