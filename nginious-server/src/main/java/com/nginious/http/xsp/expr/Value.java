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

import org.objectweb.asm.MethodVisitor;

/**
 * 
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public abstract class Value {
	
	/**
	 * Returns this values type.
	 * 
	 * @return the type
	 */
	abstract Type getType();
	
	/**
	 * Evaluates this value.
	 * 
	 * @return the result of the evaluation
	 */
    abstract Value evaluate();

    /**
     * Returns this value as an integer. The value is converted to an integer if
     * necessary.
     * 
     * @return this value as an integer
     */
    abstract int getIntValue();
    
    /**
     * Returns this value as a double. The value is converted to a double if
     * necessary.
     * 
     * @return this value as a double
     */
    abstract double getDoubleValue();
    
    /**
     * Returns this value as a string. The value is converted to a string if
     * necessary.
     * 
     * @return this value as a string
     */
    abstract String getStringValue();
    
    /**
     * Returns this value as a boolean. The value is converted to a boolean if
     * necessary.
     * 
     * @return this value as a boolean
     */
    abstract boolean getBooleanValue();
    
    /**
     * Creates bytecode for evaluating this values part of an expression.
     * 
     * @param visitor the method visitor to use for creating bytecode
     * @param type the type to use for evaluation
     */
    abstract void compile(MethodVisitor visitor, Type type);
    
    /**
     * Resolves which type to use for evaluating the two specified values
     * together. The following rules apply
     * 
     * <ul>
     * <li>Compared as strings if both values are of type any.</li>
     * <li>If one of the values is of type any the values are compared using the other values type.</li>
     * <li>Compared as strings if both values are of type string.</li>
     * <li>Compared as integers if both values are of type integer.</li>
     * </ul>
     * 
     * @param value1 the first value to use for type resolving
     * @param value2 the second value to use for type resolving
     * @return the type to use for evaluating the two values
     */
    protected Type resolveType(Value value1, Value value2) {
    	if(value1.getType() == Type.ANY && value2.getType() == Type.ANY) {
    		return Type.STRING;
    	}
    	
    	if(value1.getType() == Type.ANY || value2.getType() == Type.ANY) {
    		return value1.getType() == Type.ANY ? value2.getType() : value1.getType();
    	}
    	
    	if(value1.getType() == Type.STRING && value2.getType() == Type.STRING) {
    		return Type.STRING;
    	}
    	
    	if(value1.getType() == Type.INT && value2.getType() == Type.INT) {
    		return Type.INT;
    	}
    	
    	return Type.DOUBLE;
    }
}
