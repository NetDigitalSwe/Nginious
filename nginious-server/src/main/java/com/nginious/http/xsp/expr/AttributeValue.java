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

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * An attribute value retrieves its value from the expressions variable context
 * when evaluated. See {@link Variables}.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class AttributeValue extends Value {
	
	private String name;
	
	/**
	 * Constructs a new attribute value with the specified attribute name.
	 * @param name
	 */
    public AttributeValue(String name) {
    	this.name = name;
    }
    
    /**
     * Returns the type of value that this value returns when evaluated,
     * {@link Type#ANY}.
     * 
     * @return the type returned by this value
     */
    protected Type getType() {
    	return Type.ANY;
    }
    
    /**
     * Evaluates this value which retrieves the attribute value from the expressions
     * variable context.
     * 
     * @return the result of evaluating this value
     */
    protected Value evaluate() {
    	Object o = Expression.getVariable(this.name);
    	
    	if(o == null) {
    		return null;
    	}
    	
    	if(o instanceof Integer) {
    		return new IntegerValue(((Integer)o).intValue());
    	}
    	
    	if(o instanceof Double) {
    		return new DoubleValue(((Double)o).doubleValue());
    	}
    	
    	if(o instanceof Boolean) {
    		return new BooleanValue(((Boolean)o).booleanValue());
    	}
    	
    	return new StringValue(o.toString());
    }
    
    /**
     * Returns this attribute value converted to an integer.
     * 
     * @return this attribute value converted to an integer
     */
    protected int getIntValue() {
    	Object o = Expression.getVariable(this.name);
    	
    	if(o == null) {
    		return 0;
    	}
    	
    	if(o instanceof Integer) {
    		return ((Integer)o).intValue();
    	}
    	
    	if(o instanceof Double) {
    		return (int)((Double)o).doubleValue();
    	}
    	
    	if(o instanceof Boolean) {
    		return ((Boolean)o).booleanValue() ? 1 : 0;
    	}
    	
    	return Integer.parseInt(o.toString());
    }
    
    /**
     * Returns this attribute value converted to a double.
     * 
     * @return this attribute value converted to a double
     */
    protected double getDoubleValue() {
    	Object o = Expression.getVariable(this.name);
    	
    	if(o == null) {
    		return 0.0d;
    	}
    	
    	if(o instanceof Integer) {
    		return (double)((Integer)o).intValue();
    	}
    	
    	if(o instanceof Double) {
    		return ((Double)o).doubleValue();
    	}
    	
    	if(o instanceof Boolean) {
    		return ((Boolean)o).booleanValue() ? 1.0d : 0.0d;
    	}
    	
    	return Double.parseDouble(o.toString());
    }
    
    /**
     * Returns this attribute value converted to a string.
     * 
     * @return this attribute value converted to a string
     */
    protected String getStringValue() {
    	Object o = Expression.getVariable(this.name);
    	
    	if(o == null) {
    		return null;
    	}
    	
    	if(o instanceof Integer) {
    		return ((Integer)o).toString();
    	}
    	
    	if(o instanceof Double) {
    		return ((Double)o).toString();
    	}
    	
    	if(o instanceof Boolean) {
    		return ((Boolean)o).booleanValue() ? "true" : "false";
    	}
    	
    	return o.toString();
    }
    
    /**
     * Returns this attribute value converted to a boolean.
     * 
     * @return this attribute value converted to a boolean
     */
    protected boolean getBooleanValue() {
    	Object o = Expression.getVariable(this.name);
    	
    	if(o == null) {
    		return false;
    	}
    	
    	if(o instanceof Integer) {
    		return ((Integer)o).intValue() > 0;
    	}
    	
    	if(o instanceof Double) {
    		return ((Double)o).doubleValue() >= 0.0d;
    	}
    	
    	if(o instanceof Boolean) {
    		return ((Boolean)o).booleanValue();
    	}
    	
    	return o.toString().equals("true");
    }
    
    /**
     * Creates bytecode for evaluating this attribute value. The bytecode is created using the
     * specified method visitor and creates a result of specified type.
     * 
     * @param visitor the method visitor
     * @param type the type
     */
    void compile(MethodVisitor visitor, Type type) {
    	visitor.visitLdcInsn(this.name);
    	visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "com/nginious/http/xsp/expr/Expression", "getVariable", "(Ljava/lang/String;)Ljava/lang/Object;");
    	
    	if(type != Type.ANY) { 
        	visitor.visitInsn(Opcodes.DUP);
    	}
    	
    	if(type == Type.STRING){ 
    		Label nullLabel = new Label();
    		Label notNullLabel = new Label();
    		
    		visitor.visitJumpInsn(Opcodes.IFNULL, nullLabel);
    		
        	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;");
        	visitor.visitJumpInsn(Opcodes.GOTO, notNullLabel);
        	
        	visitor.visitLabel(nullLabel);
        	visitor.visitInsn(Opcodes.POP);
        	visitor.visitInsn(Opcodes.ACONST_NULL);
        	
        	visitor.visitLabel(notNullLabel);
    	} else if(type == Type.INT) { 
    		Label nullLabel = new Label();
    		Label notNullLabel = new Label();
    		
    		visitor.visitJumpInsn(Opcodes.IFNULL, nullLabel);
    		
        	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;");
        	visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I");
        	visitor.visitJumpInsn(Opcodes.GOTO, notNullLabel);
        	
        	visitor.visitLabel(nullLabel);
        	visitor.visitInsn(Opcodes.POP);
        	visitor.visitLdcInsn(0);
        	
        	visitor.visitLabel(notNullLabel);
    	} else if(type == Type.DOUBLE) { 
    		Label nullLabel = new Label();
    		Label notNullLabel = new Label();
    		
    		visitor.visitJumpInsn(Opcodes.IFNULL, nullLabel);
    		
        	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;");
        	visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "parseDouble", "(Ljava/lang/String;)D");
        	visitor.visitJumpInsn(Opcodes.GOTO, notNullLabel);
        	
        	visitor.visitLabel(nullLabel);
        	visitor.visitInsn(Opcodes.POP);
        	visitor.visitLdcInsn(0.0d);
        	
        	visitor.visitLabel(notNullLabel);
    	}
    }
}
