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

import java.util.Iterator;
import java.util.TreeMap;

/**
 * A header parameter is a subpart of a HTTP header value. Parameters are delimited by
 * ','.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class HeaderParameter implements Comparable<HeaderParameter> {
	
	private String name;
	
	private String value;
	
	private double quality;
	
	private TreeMap<String, String> subParameters;
	
	/**
	 * Constructs a new header parameter with the specified name and default quality
	 * of 1.0.
	 * 
	 * @param name the parameter name
	 */
	HeaderParameter(String name) {
		this.name = name;
		this.subParameters = new TreeMap<String, String>();
		this.quality = 1.0d;
	}
	
	/**
	 * Returns this header parameters name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Returns this header parameters value.
	 * 
	 * @return the value
	 */
	public String getValue() {
		return this.value;
	}
	
	/**
	 * Returns this header parameters quality sub parameter.
	 * 
	 * @return the quality sub parameter
	 */
	public double getQuality() {
		return this.quality;
	}
	
	/**
	 * Sets this header parameters value to the specified value.
	 * 
	 * @param value the value
	 */
	void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * Returns sub parameter value with the specified name.
	 * 
	 * @param name the sub parameter name
	 * @return the sub parameter value or <code>null</code>
	 */
	public String getSubParameter(String name) {
		return subParameters.get(name);
	}
	
	/**
	 * Adds sub parameter with the specified name and value to this header parameter.
	 * 
	 * @param name the sub parameter name
	 * @param value the sub parameter value
	 */
	void addSubParameter(String name, String value) {
		if(name.equals("q")) {
			this.quality = Double.parseDouble(value);
		}
		
		subParameters.put(name, value);
	}
	
	/**
	 * Compares this header parameter to the specified header parameter. This method
	 * returns 0 if the header parameters are equal, -1 if this header parameter is less than
	 * the specified header parameter and 1 if this header parameter is more than the specified
	 * header parameter.
	 * 
	 * <p>
	 * Two header parameters are considered equal if their name anf quality sub parameter are
	 * the same.
	 * </p>
	 * 
	 * @param other the header parameter to compare to
	 * @return 0 if equal, -1 if this header parameter is less than, 1 if this header parameter is more than
	 */
	public int compareTo(HeaderParameter other) {
		return this.quality == other.getQuality() ? name.compareTo(other.getName()) : 
			this.quality < other.getQuality() ? 1 : -1;
	}
	
	/**
	 * Returns whether or not this header parameter contains a media type that matches
	 * the specified content type. See section 14.1 in the 
	 * <a href="http://www.ietf.org/rfc/rfc2616.txt">HTTP/1.1 RFC 2616</a>.
	 * 
	 * @param contentType the content type to match
	 * @return <code>true</code> if this header parameter contains a media type and matches the
	 * 	content type, <code>false</code> otherwise
	 */
	public boolean accepts(String contentType) {
		if(name.indexOf("/") == -1 || contentType.indexOf("/") == -1) {
			return false;
		}
		
		String[] typeSubtype = name.split("/");
		String[] otherTypeSubtype = contentType.split("/");
		
		if(!typeSubtype[0].equals("*") && !typeSubtype[0].equals(otherTypeSubtype[0])) {
			return false;
		}
		
		if(!typeSubtype[1].equals("*") && !typeSubtype[1].equals(otherTypeSubtype[1])) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Converts this header parameter to string representation.
	 * 
	 * @return a string representation of this header parameter
	 */
	public String toString() {
		StringBuffer desc = new StringBuffer(this.name);
		Iterator<String> it = subParameters.keySet().iterator();
		
		while(it.hasNext()) {
			String name = it.next();
			desc.append("; ");
			desc.append(name);
			String value = subParameters.get(name);
			
			if(value != null) {
				desc.append("=");
				desc.append(value);
			}
		}
		
		return desc.toString();
	}
}
