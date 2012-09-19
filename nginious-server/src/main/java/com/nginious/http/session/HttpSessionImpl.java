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

import java.util.HashMap;
import java.util.Set;

import com.nginious.http.HttpSession;

/**
 * Provides a way to hold information over several HTTP requests and responses. A HTTP session stores
 * a set of attributes where each attribute is mapped to a name. A HTTP session is available for as long
 * as it is considered to be active. How a session is managed during its lifetime depends on how it is
 * stored.
 *
 * <p>
 * One way to store a HTTP session is to serialize and store it in a HTTP cookie which is sent to the
 * HTTP client in responses. When a subsequent request arrives from the same client the HTTP session is
 * deserialized from the cookie.
 * </p>
 * 
 * <p>
 * All attributes values must be serializable to allow HTTP sessions to be serialized and stored in
 * binary form as described above.
 * </p>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
class HttpSessionImpl implements HttpSession {
	
	private HashMap<String, Object> attributes;
	
	private String sessionId;
	
	private long creationTime;
	
	private long lastAccessedTime;
	
	private boolean created;
	
	private boolean invalidated;
	
	/**
	 * Constructs a new empty HTTP session.
	 */
	public HttpSessionImpl() {
		super();
		this.attributes = new HashMap<String, Object>();
		this.created = true;
		this.creationTime = System.currentTimeMillis();
		this.lastAccessedTime = System.currentTimeMillis();
	}
	
	/**
	 * Constructs a new HTTP session with the specified attributes, creation time and last accessed time.
	 * 
	 * @param type the session type
	 * @param attributes the attributes
	 * @param creationTime creation time in milliseconds
	 */
	HttpSessionImpl(HashMap<String, Object> attributes, long creationTime) {
		this.attributes = attributes;
		this.created = false;
		this.creationTime = creationTime;
	}
	
	/**
	 * Sets this sessions id to the specified session id.
	 * 
	 * @param sessionId the session id
	 */
	void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	/**
	 * Returns the session id for this session.
	 * 
	 * @return the session id
	 */
	public String getSessionId() {
		return this.sessionId;
	}
	
	/**
	 * Sets this sessions last accessed time to the current time.
	 */
	void setLastAccessedTime() {
		this.lastAccessedTime = System.currentTimeMillis();
	}
	
	/**
	 * Returns attribute with the specified name from this HTTP session.
	 * 
	 * @param name attribute name
	 * @return attribute or <code>null</code> if this HTTP session does not contain an attribute with the given name.
	 */
	public Object getAttribute(String name) {
		this.lastAccessedTime = System.currentTimeMillis();
		return attributes.get(name);
	}
	
	/**
	 * Returns a set with all attribute names from this HTTP session.
	 * 
	 * @return the attribute names set
	 */
	public Set<String> getAttributeNames() {
		this.lastAccessedTime = System.currentTimeMillis();
		return attributes.keySet();
	}
	
	/**
	 * Returns the creation time in milliseconds for this HTTP session.
	 * 
	 * @return the creation time in milliseconds
	 */
	public long getCreationTime() {
		return this.creationTime;
	}
	
	/**
	 * Returns the time in milliseconds when this HTTP session was last accessed.
	 * 
	 * @return last accessed time in milliseconds
	 */
	public long getLastAccessedTime() {
		return this.lastAccessedTime;
	}
	
	/**
	 * Invalidates this HTTP session.
	 */
	public void invalidate() {
		this.attributes = new HashMap<String, Object>();
		this.creationTime = 0L;
		this.lastAccessedTime = 0L;
		this.invalidated = true;
	}
	
	/**
	 * Returns whether or not this HTTP session has been invalidated.
	 * 
	 * @return <code>true</code> if this HTTP session has been invalidated, <code>false</code> otherwise
	 */
	public boolean isInvalidated() {
		return this.invalidated;
	}
	
	/**
	 * Returns whether or not this HTTP session is new. A HTTP session is considered to be new when
	 * it is created.
	 * 
	 * @return <code>true</code> if this HTTP session is new, <code>false</code> otherwise
	 */
	public boolean isNew() {
		return this.created;
	}
	
	/**
	 * Removes attribute with the specified name from this HTTP session.
	 * 
	 * @param name the attribute name
	 */
	public void removeAttribute(String name) {
		this.lastAccessedTime = System.currentTimeMillis();
		attributes.remove(name);
	}
	
	/**
	 * Sets attribute with the specified name and value in this HTTP session.
	 * 
	 * @param name the attribute name
	 * @param value the attribute value
	 */
	public void setAttribute(String name, Object value) {
		this.lastAccessedTime = System.currentTimeMillis();
		attributes.put(name, value);
	}
	
	/**
	 * Returns the hash code of this session.
	 * 
	 * @return the hash code
	 */
	public int hashCode() {
		if(this.sessionId != null) {
			return sessionId.hashCode();
		}
		
		return super.hashCode();
	}
	
	/**
	 * Checks if this session is equal to the specified object. Two session are considered equal if they
	 * have the same session id.
	 * 
	 * @param o object to compare to
	 * @return <code>true</code> if the given object is an instanceof HttpSession and has the same session id
	 * as this session, <code>false</code>
	 */
	public boolean equals(Object o) {
		if(o instanceof HttpSessionImpl) {
			HttpSessionImpl other = (HttpSessionImpl)o;
			
			if(other.getSessionId() != null && this.sessionId != null) {
				return other.getSessionId().equals(this.sessionId);
			}
		}
		
		return super.equals(o);
	}
}
