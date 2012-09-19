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

package com.nginious.http.rest;

import com.nginious.http.rest.Serializable;

@Serializable
public class QueryAnnotatedBean {
	
	private String first;
	
	private String second;
	
	private String third;
	
	public QueryAnnotatedBean() {
		super();
	}

	public String getFirst() {
		return this.first;
	}
	
	@Serializable(deserialize = false)
	public void setFirst(String first) {
		this.first = first;
	}

	public String getSecond() {
		return this.second;
	}
	
	@Serializable(types = "xml,json")
	public void setSecond(String second) {
		this.second = second;
	}

	public String getThird() {
		return this.third;
	}

	public void setThird(String third) {
		this.third = third;
	}
}
