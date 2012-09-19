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

package com.nginious.http.cmd;

import java.util.HashMap;

/**
 * Enumeration over possible command line argument types.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public enum ArgumentType {
	
	BYTE(Byte.class),
	
	SHORT(Short.class),
	
	INTEGER(Integer.class),
	
	LONG(Long.class),
	
	FLOAT(Float.class),
	
	DOUBLE(Double.class),
	
	STRING(String.class),
	
	BOOLEAN(Boolean.class);
	
	private static HashMap<Class<?>, ArgumentType> types = new HashMap<Class<?>, ArgumentType>();
	
	static {
		types.put(Byte.class, BYTE);
		types.put(byte.class, BYTE);
		types.put(Short.class, SHORT);
		types.put(short.class, SHORT);
		types.put(Integer.class, INTEGER);
		types.put(int.class, INTEGER);
		types.put(Long.class, LONG);
		types.put(long.class, LONG);
		types.put(Float.class, FLOAT);
		types.put(float.class, FLOAT);
		types.put(Double.class, DOUBLE);
		types.put(double.class, DOUBLE);
		types.put(String.class, STRING);
		types.put(Boolean.class, BOOLEAN);
		types.put(boolean.class, BOOLEAN);
	}
	
	private Class<?> clazzType;
	
	/**
	 * Constructs a new argument type for the specified class type.
	 * 
	 * @param clazzType the class type
	 */
	private ArgumentType(Class<?> clazzType) {
		this.clazzType = clazzType;
	}
	
	/**
	 * Returns argument type for the specified class type.
	 * 
	 * @param clazzType the class type
	 * @return the argument type
	 */
	static ArgumentType getType(Class<?> clazzType) {
		return types.get(clazzType);
	}
	
	/**
	 * Returns class type for this argument type.
	 * 
	 * @return the class type
	 */
	Class<?> getClassType() {
		return this.clazzType;
	}
}
