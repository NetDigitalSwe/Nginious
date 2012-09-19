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
 * A value of type string.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class StringValue extends Value {

    private String value;
    
    /**
     * Constructs a new string value with the specified value.
     * 
     * @param value the string value
     */
    public StringValue(String value) {
        this.value = value;
    }
    
    /**
     * Returns this values type, {@link Type#STRING}.
     * 
     * @return this values type
     */
    protected Type getType() {
    	return Type.STRING;
    }
    
    /**
     * Evaluates this value.
     * 
     * @return result of evaluating this value
     */
    protected Value evaluate() {
        return this;
    }
    
    /**
     * Evaluates this string value as an integer.
     * 
     * @return this value converted to an integer
     */
    protected int getIntValue() {
    	return Integer.parseInt(this.value);
    }
    
    /**
     * Evaluates this string value as a double.
     * 
     * @return this value converted to a double
     */
    protected double getDoubleValue() {
        return Double.parseDouble(this.value);
    }

    /**
     * Returns this string values value.
     * 
     * @return this values string value
     */
    protected String getStringValue() {
        return value;
    }
    
    /**
     * Evaluates this string value as a boolean.
     * 
     * @return <code>true</code> if this string value contains the string <code>true</code>,
     * 	otherwise <code>false</code>
     */
    protected boolean getBooleanValue() {
    	return Boolean.parseBoolean(this.value);
    }
    
    /**
     * Creates bytecode for evaluating this string value. The bytecode is generated using
     * the specified method visitor.
     * 
     * @param visitor the method visitor
     * @param type the type
     */
    void compile(MethodVisitor visitor, Type type) {
        visitor.visitLdcInsn(this.value);
    }
}
