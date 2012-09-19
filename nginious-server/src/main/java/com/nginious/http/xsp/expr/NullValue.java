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
import org.objectweb.asm.Opcodes;

/**
 * A value which represents <code>null</code>. A null value does not have
 * a type.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class NullValue extends Value {
	
	/**
	 * Constructs a new null value.
	 */
    public NullValue() {
    	super();
    }
    
    /**
     * Returns this values type, {@link Type#ANY}.
     * 
     * @return this values type
     */
    protected Type getType() {
    	return Type.ANY;
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
     * Evaluates this null values as an integer.
     * 
     * @return <code>0</code>
     */
    protected int getIntValue() {
    	return 0;
    }
    
    /**
     * Evaluates this null value as a double.
     * 
     * @return <code>0.0d</code>
     */
    protected double getDoubleValue() {
        return 0.0d;
    }
    
    /**
     * Evaluates this null value as a string.
     * 
     * @return <code>null</code>
     */
    protected String getStringValue() {
    	return null;
    }
    
    /**
     * Evaluates this null value as a boolean.
     * 
     * @return <code>false</code>
     */
    protected boolean getBooleanValue() {
    	return false;
    }
    
    /**
     * Creates bytecode for this null value. The bytecode is generated using the specified method 
     * visitor. The result of the evaluation produces the specified type.
     * 
     * @param visitor the method visitor
     * @param type the type
     */
    void compile(MethodVisitor visitor, Type type) {
    	if(type == Type.STRING) {
        	visitor.visitInsn(Opcodes.ACONST_NULL);    		
    	} else if(type == Type.INT) {
    		visitor.visitLdcInsn(0);
    	} else if(type == Type.DOUBLE) {
    		visitor.visitLdcInsn(0.0d);
    	}
    }
}
