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
 * A variable context is used to resolve variables during evaluation of an expression.
 * Typically the caller provides the context when evaluating an expression. See
 * {@link Expression#evaluateDouble(Variables)} for an example
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public interface Variables {
	
	/**
	 * Returns the variable with the specified name from this variable context.
	 * 
	 * @param name the variable name
	 * @return the value or <code>null</code> if no variable with the given name exists 
	 * 	in this context
	 */
	public Object getVariable(String name);
}
