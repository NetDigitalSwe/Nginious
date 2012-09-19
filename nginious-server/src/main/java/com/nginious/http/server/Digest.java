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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.nginious.http.common.StringUtils;

public class Digest {

	private String method;
    
    private String username;
    
    private String realm;
    
    private String nonce;
    
    private String nc;
    
    private String cnonce;
    
    private String qop;
    
    private String uri;
    
    private String response;
	
	public Digest() {
		super();
	}
	
	public void setMethod(String method) {
		this.method = method;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public void setRealm(String realm) {
		this.realm = realm;
	}
	
	public void setNonce(String nonce) {
		this.nonce = nonce;
	}
	
	public void setNc(String nc) {
		this.nc = nc;
	}
	
	public void setCnonce(String cnonce) {
		this.cnonce = cnonce;
	}
	
	public void setQop(String qop) {
		this.qop = qop;
	}
	
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public void setResponse(String response) {
		this.response = response;
	}
	
	public String createResponse(String password) {
		byte[] a1 = null;
		byte[] a2 = null;
		
	    if(this.method == null || this.username == null || this.realm == null || this.nonce == null
	    		|| this.nc == null || this.cnonce == null || this.qop == null || this.uri == null) {
	    	return null;
	    }
	    
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(username.getBytes("iso-8859-1"));
            md.update((byte)':');
            md.update(realm.getBytes("iso-8859-1"));
            md.update((byte)':');
            md.update(password.getBytes("iso-8859-1"));
            a1 = md.digest();

            md.reset();
            md.update(method.getBytes("iso-8859-1"));
            md.update((byte)':');
            md.update(uri.getBytes("iso-8859-1"));
            a2 = md.digest();

            md.update(StringUtils.asHexString(a1, true).getBytes("iso-8859-1"));
            md.update((byte)':');
            md.update(nonce.getBytes("iso-8859-1"));
            md.update((byte)':');
            md.update(nc.getBytes("iso-8859-1"));
            md.update((byte)':');
            md.update(cnonce.getBytes("iso-8859-1"));
            md.update((byte)':');
            md.update(qop.getBytes("iso-8859-1"));
            md.update((byte)':');
            md.update(StringUtils.asHexString(a2, true).getBytes("iso-8859-1"));
            byte[] digest = md.digest();
            
            return StringUtils.asHexString(digest, true);
		} catch(NoSuchAlgorithmException e) {
			throw new RuntimeException("MD5 not supported", e);
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException("iso-8859-1 encoding not supported", e);
		}
	}
	
	public boolean check(String password) {
		byte[] a1 = null;
		byte[] a2 = null;
		
	    if(this.method == null || this.username == null || this.realm == null || this.nonce == null
	    		|| this.nc == null || this.cnonce == null || this.qop == null || this.uri == null) {
	    	return false;
	    }
	    
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(username.getBytes("iso-8859-1"));
            md.update((byte)':');
            md.update(realm.getBytes("iso-8859-1"));
            md.update((byte)':');
            md.update(password.getBytes("iso-8859-1"));
            a1 = md.digest();

            md.reset();
            md.update(method.getBytes("iso-8859-1"));
            md.update((byte)':');
            md.update(uri.getBytes("iso-8859-1"));
            a2 = md.digest();

            md.update(StringUtils.asHexString(a1, true).getBytes("iso-8859-1"));
            md.update((byte)':');
            md.update(nonce.getBytes("iso-8859-1"));
            md.update((byte)':');
            md.update(nc.getBytes("iso-8859-1"));
            md.update((byte)':');
            md.update(cnonce.getBytes("iso-8859-1"));
            md.update((byte)':');
            md.update(qop.getBytes("iso-8859-1"));
            md.update((byte)':');
            md.update(StringUtils.asHexString(a2, true).getBytes("iso-8859-1"));
            byte[] digest = md.digest();
            
            // check digest
            return StringUtils.asHexString(digest, true).equalsIgnoreCase(response);
		} catch(NoSuchAlgorithmException e) {
		} catch(UnsupportedEncodingException e) {}
			
		
		return false;
	}
}