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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * Manages a list of header parameters.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class HeaderParameters {
	
	private HashMap<String, HeaderParameter> parameters;
	
	private ArrayList<HeaderParameter> sorted;
	
	/**
	 * Constructs a new header parameters;
	 */
	HeaderParameters() {
		super();
		this.parameters = new HashMap<String, HeaderParameter>();
	}
	
	/**
	 * Adds the specified header parameter to this header parameters.
	 * 
	 * @param parameter the header parameter to add
	 */
	void addParameter(HeaderParameter parameter) {
		parameters.put(parameter.getName(), parameter);
	}
	
	/**
	 * Returns names for all header parameters.
	 * 
	 * @return the names of all header parameters
	 */
	public String[] getNames() {
		Set<String> names = parameters.keySet();
		return names.toArray(new String[names.size()]);
	}
	
	/**
	 * Returns header parameter at specified index in list of header parameters.
	 * 
	 * @param index the index
	 * @return the header parameter
	 */
	public HeaderParameter get(int index) {
		if(this.sorted == null) {
			sort();
		}
		
		return sorted.get(index);
	}
	
	/**
	 * Returns header parameter with the specified name.
	 * 
	 * @param name the header parameter name
	 * @return the header parameter or <code>null</code> if no header parameter with given
	 * 	name exists
	 */
	public HeaderParameter getParameter(String name) {
		return parameters.get(name);
	}
	
	/**
	 * Returns all header parameters sorted by their quality sub parameter.
	 * 
	 * @return all header parameters sorted by their quality sub parameter
	 */
	public HeaderParameter[] getSorted() {
		if(this.sorted == null) {
			sort();
		}
		
		return sorted.toArray(new HeaderParameter[sorted.size()]);
	}
	
	/**
	 * Returns number of header parameters.
	 * 
	 * @return number of header parameters
	 */
	public int size() {
		return parameters.size();
	}
	
	/**
	 * Sorts all header parameters in descending order on their quality sub parameter.
	 */
	private void sort() {
		this.sorted = new ArrayList<HeaderParameter>(parameters.size());
		TreeSet<HeaderParameter> sort = new TreeSet<HeaderParameter>(parameters.values());
		sorted.addAll(sort);
	}
}
