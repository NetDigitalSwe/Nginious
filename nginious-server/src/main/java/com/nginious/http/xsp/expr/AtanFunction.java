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
 * Calculates the arc tangent of an argument value.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class AtanFunction extends MathFunction {

	/**
	 * Constructs a new atan function with the specified argument value.
	 * 
	 * @param value the argument value
	 */
    public AtanFunction(Value value) {
        super("atan", value);
    }
    
    /**
     * Calculates the arc tangent of this functions argument.
     * 
     * @return the result of this function 
     */
    protected double getDoubleValue() {
        return Math.atan(value.getDoubleValue());
    }
}
