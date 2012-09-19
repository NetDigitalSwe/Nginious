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

package com.nginious.http;

import java.util.Set;

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
public interface HttpSession {
	
	/**
	 * Returns the session id for this session.
	 * 
	 * @return the session id
	 */
	String getSessionId();
	
	/**
	 * Returns attribute with the specified name from this HTTP session.
	 * 
	 * @param name attribute name
	 * @return attribute or <code>null</code> if this HTTP session does not contain an attribute with the given name.
	 */
	public Object getAttribute(String name);
	
	/**
	 * Returns a set with all attribute names from this HTTP session.
	 * 
	 * @return the attribute names set
	 */
	public Set<String> getAttributeNames();
	
	/**
	 * Returns the creation time in milliseconds for this HTTP session.
	 * 
	 * @return the creation time in milliseconds
	 */
	public long getCreationTime();
	
	/**
	 * Returns the time in milliseconds when this HTTP session was last accessed.
	 * 
	 * @return last accessed time in milliseconds
	 */
	public long getLastAccessedTime();
	
	/**
	 * Invalidates this HTTP session.
	 */
	public void invalidate();
	
	/**
	 * Returns whether or not this HTTP session has been invalidated.
	 * 
	 * @return <code>true</code> if this HTTP session has been invalidated, <code>false</code> otherwise
	 */
	public boolean isInvalidated();
	
	/**
	 * Returns whether or not this HTTP session is new. A HTTP session is considered to be new when
	 * it is created.
	 * 
	 * @return <code>true</code> if this HTTP session is new, <code>false</code> otherwise
	 */
	public boolean isNew();
	
	/**
	 * Removes attribute with the specified name from this HTTP session.
	 * 
	 * @param name the attribute name
	 */
	public void removeAttribute(String name);
	
	/**
	 * Sets attribute with the specified name and value in this HTTP session.
	 * 
	 * @param name the attribute name
	 * @param value the attribute value
	 */
	public void setAttribute(String name, Object value);
}
