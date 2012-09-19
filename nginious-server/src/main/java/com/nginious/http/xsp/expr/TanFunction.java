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
 * Calculates the trigonometric tangent of an argument value.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class TanFunction extends MathFunction {
	
	/**
	 * Constructs a new tan function with the specified argument value.
	 * 
	 * @param value the argument value
	 */
    public TanFunction(Value value) {
        super("tan", value);
    }
    
    /**
     * Calculates the trigonometric tangent of this tan functions argument.
     * 
     * @return the result of this function 
     */
    protected double getDoubleValue() {
        return Math.tan(value.getDoubleValue());
    }
}
