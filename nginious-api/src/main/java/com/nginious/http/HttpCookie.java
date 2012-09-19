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

/**
 * Represents a HTTP cookie. Provides functionality for formatting cookies for sending in HTTP responses and
 * parsing cookies received in HTTP requests.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class HttpCookie {
	
	private String name;
	
	private String value;
	
	private String path;
	
	private String comment;
	
	private String domain;
	
	private int maxAge;
	
	private int version;
	
	/**
	 * Constructs a new HTTP cookie.
	 */
	public HttpCookie() {
		super();
	}
	
	/**
	 * Constructs a new HTTP cookie with the specified name and value.
	 * 
	 * @param name the name
	 * @param value the value
	 */
	public HttpCookie(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	/**
	 * Returns this HTTP cookies name field.
	 * 
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Sets this HTTP cookies name field to the specified name value.
	 * 
	 * @param name the name value
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns this HTTP cookies value field.
	 * 
	 * @return the value
	 */
	public String getValue() {
		return this.value;
	}
	
	/**
	 * Sets this HTTP cookies value to the specified value.
	 * 
	 * @param value the value
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * Returns the path field for this HTTP cookie.
	 * 
	 * @return the path
	 */
	public String getPath() {
		return this.path;
	}
	
	/**
	 * Sets the path field for this HTTP cookie to the specified path value.
	 * 
	 * @param path the path value
	 */
	public void setPath(String path) {
		this.path = path;
	}
	
	/**
	 * Returns the comment field for this HTTP cookie.
	 * 
	 * @return the comment field
	 */
	public String getComment() {
		return this.comment;
	}
	
	/**
	 * Sets the comment field for this HTTP cookie to the specified comment vlaue.
	 * 
	 * @param comment the comment value
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	/**
	 * Returns the domain field for this HTTP cookie.
	 * 
	 * @return the domain field
	 */
	public String getDomain() {
		return this.domain;
	}
	
	/**
	 * Sets the domain field for this HTTP cookie to the specified domain value.
	 * 
	 * @param domain the domain value
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	/**
	 * Returns the max age field for this HTTP cookie.
	 * 
	 * @return the max age field
	 */
	public int getMaxAge() {
		return this.maxAge;
	}

	/**
	 * Sets the max age field for this HTTP cookie to the specified max age value.
	 * 
	 * @param maxAge the max age value
	 */
	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
	}
	
	/**
	 * Returns the version field for this HTTP cookie.
	 * 
	 * @return the version field
	 */
	public int getVersion() {
		return this.version;
	}
	
	/**
	 * Sets the version field to the specified version value for this HTTP cookie.
	 * 
	 * @param version the version value
	 */
	public void setVersion(int version) {
		this.version = version;
	}
}
