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
 * Calculate the trigonometric sine of a argument value.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class SinFunction extends MathFunction {
	
	/**
	 * Constructs a new trigonometric since function with the specified
	 * argument value.
	 * 
	 * @param value the argument value
	 */
    public SinFunction(Value value) {
        super("sin", value);
    }
    
    /**
     * Calculates the trigonometric sine of the argument value.
     * 
     * @return the double result of this function
     */
    protected double getDoubleValue() {
        return Math.sin(value.getDoubleValue());
    }
}
