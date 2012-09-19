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

package com.nginious.http.xsp.expr;

/**
 * Types supported by expression values.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 * @see Value
 */
public enum Type {
	
	/**
	 * A value of type integer
	 */
	INT,
	
	/**
	 * A value of type double
	 */
	DOUBLE,
	
	/**
	 * A value of type string
	 */
	STRING,
	
	/**
	 * A value of type boolean
	 */
	BOOLEAN,
	
	/**
	 * A value which supports any type
	 */
	ANY
}