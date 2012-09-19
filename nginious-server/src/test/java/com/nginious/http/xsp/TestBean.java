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

package com.nginious.http.xsp;

public class TestBean {
	
	private String test1;
	
	private int test2;
	
	private double test3;
	
	public TestBean() {
		super();
	}

	public String getTest1() {
		return this.test1;
	}

	public void setTest1(String test1) {
		this.test1 = test1;
	}

	public int getTest2() {
		return this.test2;
	}

	public void setTest2(int test2) {
		this.test2 = test2;
	}

	public double getTest3() {
		return this.test3;
	}

	public void setTest3(double test3) {
		this.test3 = test3;
	}
}
