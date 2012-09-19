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

/**
 * A XSP part that is a child of another XSP part.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
abstract class ChildPart extends XspPart {
	
	/**
	 * Constructs a new child part found in the XSP file located at the specified
	 * source file path and line number.
	 * 
	 * @param srcFilePath the XSP source file path
	 * @param lineNo the line number
	 */
	ChildPart(String srcFilePath, int lineNo) {
		super(srcFilePath, lineNo);
	}
}
