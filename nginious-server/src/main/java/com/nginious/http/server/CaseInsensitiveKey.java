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
 * A string for case insensitive hashing and comparison.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class CaseInsensitiveKey {
	
	private String key;
	
	private String lowerCaseKey;
	
	/**
	 * Constructs a new case insensitive key with the specified key.
	 * 
	 * @param key the key
	 */
	public CaseInsensitiveKey(String key) {
		this.key = key;
		this.lowerCaseKey = key.toLowerCase();
	}
	
	/**
	 * Returns key that this case insensitive key was created from.
	 * 
	 * @return the key
	 */
	public String getKey() {
		return this.key;
	}
	
	/**
	 * Returns whether or not this case insensitive key is equal to the specified object. This case insensitive
	 * key is equal to the specified object if it is also a case insensitive key and the keys match without matching
	 * case.
	 * 
	 * @param o the object to compare to
	 * @return <code>true</code> if the objects are equal, <code>false</code> otherwise
	 */
	public boolean equals(Object o) {
		if(o instanceof CaseInsensitiveKey) {
			CaseInsensitiveKey other = (CaseInsensitiveKey)o;
			return other.lowerCaseKey.equals(this.lowerCaseKey);
		} else if(o instanceof String) {
			String other = (String)o;
			return other.toLowerCase().equals(this.lowerCaseKey);
		}
		
		return super.equals(o);
	}
	
	/**
	 * Returns hash code for this case insensitive key. Two case insensitive keys return the same hash code if
	 * all characters are the same except for case.
	 * 
	 * @return the hash code
	 */
	public int hashCode() {
		return lowerCaseKey.hashCode();
	}
}
