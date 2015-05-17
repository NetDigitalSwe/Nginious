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

package com.nginious.http.loader;

public class ClassReferenceTest2 {

	private ClassReferenceTest1 test1;

	public ClassReferenceTest2() {
		this(true);
	}

	public ClassReferenceTest2(boolean create) {
		if (create) {
			test1 = new ClassReferenceTest1(false);
		}
	}
	
	public String toString() {
		return "ClassLoader1-ClassReferenceTest2";
	}
}
