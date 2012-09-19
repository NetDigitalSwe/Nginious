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
import java.util.List;
import java.util.Set;


public class RequestField {
	
	private String field;
	
	private String method;
	
	private String path;
	
	private HashMap<String, List<String>> parameterValues;
	
	private String protocol;
	
	public RequestField(String field) throws LogException {
		super();
		this.field = field;
		parse(field);
	}
	
	private void parse(String field) throws LogException {
		int nextIndex = parseMethod(field);
		nextIndex = parsePath(field, nextIndex);
		nextIndex = parseParameters(field, nextIndex);
		parseProtocol(field, nextIndex);
	}
	
	private int parseMethod(String field) throws LogException {
		int index = field.indexOf(' ');
		
		if(index == -1) {
			throw new LogException("Can't find method name in request field");
		}
		
		this.method = field.substring(0, index);
		return index + 1;
	}
	
	private int parsePath(String field, int startIndex) throws LogException {
		int index = field.indexOf('?', startIndex);
		
		if(index == -1) {
			index = field.indexOf(' ', startIndex);
		}
		
		if(index == -1) {
			throw new LogException("Can't find path in request field");
		}
		
		this.path = field.substring(startIndex, index);
		return index;
	}
	
	private int parseParameters(String field, int startIndex) throws LogException {
		int index = field.indexOf('?', startIndex);
		
		if(index == -1) {
			return startIndex + 1;
		}
		
		index = field.indexOf(' ', startIndex);
		
		if(index == -1) {
			throw new LogException("Can't find parameters in request field");			
		}
		
		String paramList = field.substring(startIndex + 1, index);
		
		String[] params = paramList.split("&");
		this.parameterValues = new HashMap<String, List<String>>();
		
		for(String param : params) {
			String[] nameValue = param.split("=");
			List<String> values = parameterValues.get(nameValue[0]);
			
			if(values == null) {
				values = new ArrayList<String>();
				parameterValues.put(nameValue[0], values);
			}
			
			values.add(nameValue[1]);
		}
		
		return index + 1;
	}
	
	private void parseProtocol(String field, int startIndex) throws LogException {
		this.protocol = field.substring(startIndex);
	}
	
	public String getMethod() {
		return this.method;
	}
	
	public String getPath() {
		return this.path;
	}
	
	public String[] getParameterNames() {
		Set<String> names = parameterValues.keySet();
		return names.toArray(new String[names.size()]);
	}
	
	public String[] getParameterValues(String name) {
		List<String> values = parameterValues.get(name);
		
		if(values != null) {
			return values.toArray(new String[values.size()]);
		}
		
		return null;
	}
	
	public String getParameterValue(String name) {
		List<String> values = parameterValues.get(name);
		
		if(values != null) {
			return values.get(0);
		}
		
		return null;
	}
	
	public String getProtocol() {
		return this.protocol;
	}
	
	public String toString() {
		return this.field;
	}
}
