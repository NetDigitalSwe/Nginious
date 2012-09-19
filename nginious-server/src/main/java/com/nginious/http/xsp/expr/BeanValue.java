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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * A bean value takes its value by calling a bean get method. The bean is retrieved
 * from the expressions variable context.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 * @see Expression
 * @see Variables
 */
public class BeanValue extends Value {

    private String beanName;
    
    private String propertyName;
    
    /**
     * Constructs a new bean which retrieves a bean with the specified bean name from
     * the expressions evaluation context. The value is retrieved from the set method
     * matching the specified property name. 
     * 
     * @param beanName the bean name
     * @param propertyName the property name
     */
    public BeanValue(String beanName, String propertyName) {
        this.beanName = beanName;
        this.propertyName = propertyName;
    }
    
    /**
     * Returns this bean values type, {@link Type#ANY}.
     * 
     * @return the type of this value
     */
    protected Type getType() {
    	return Type.ANY;
    }
    
    /**
     * Evaluates this value by retrieving the bean from the expressions variable context. The 
     * beans set method is called to retrieve the value.
     * 
     * @return the value
     */
    protected Value evaluate() {
    	Object o = BeanValue.getValue(this.beanName, this.propertyName);
    	
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
     * Evaluates this bean values as an integer. The bean is retrieved from the
     * expressions variable context where the beans set method is called
     * to retrieve the value.
     * 
     * @return this bean value converted to an integer
     */
    protected int getIntValue() {
    	Object o = BeanValue.getValue(this.beanName, this.propertyName);
    	
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
     * Evaluates this bean values as a double. The bean is retrieved from the
     * expressions variable context where the beans set method is called
     * to retrieve the value.
     * 
     * @return this bean value converted to a double
     */
    protected double getDoubleValue() {
    	Object o = BeanValue.getValue(this.beanName, this.propertyName);
    	
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
     * Evaluates this bean values as a string. The bean is retrieved from the
     * expressions variable context where the beans set method is called
     * to retrieve the value.
     * 
     * @return this bean value converted to a string
     */
    protected String getStringValue() {
    	Object o = BeanValue.getValue(this.beanName, this.propertyName);
    	
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
     * Evaluates this bean values as a boolean. The bean is retrieved from the
     * expressions variable context where the beans set method is called
     * to retrieve the value.
     * 
     * @return this bean value converted to a boolean
     */
    protected boolean getBooleanValue() {
    	Object o = BeanValue.getValue(this.beanName, this.propertyName);
    	
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
     * Creates bytecode for evaluating this bean value. The bytecode is generated using the
     * specified method visitor and produces a value of the specified type.
     * 
     * @param visitor the method visitor
     * @param type the type
     */
    void compile(MethodVisitor visitor, Type type) {
    	visitor.visitLdcInsn(this.beanName);
    	visitor.visitLdcInsn(this.propertyName);
    	visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "com/nginious/http/xsp/expr/BeanValue", "getValue", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");
    	
    	if(type != Type.ANY) {
    		visitor.visitInsn(Opcodes.DUP);
    	}
    	
    	// visitor.visitJumpInsn(Opcodes.IFNONNULL, null);
    	
    	if(type == Type.DOUBLE) {
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
     	} else if(type == Type.STRING) {
    		Label nullLabel = new Label();
    		Label notNullLabel = new Label();
    		
    		visitor.visitJumpInsn(Opcodes.IFNULL, nullLabel);
    		
           	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;");     		
        	visitor.visitJumpInsn(Opcodes.GOTO, notNullLabel);
        	
        	visitor.visitLabel(nullLabel);
        	visitor.visitInsn(Opcodes.POP);
        	visitor.visitInsn(Opcodes.ACONST_NULL);
        	
        	visitor.visitLabel(notNullLabel);
     	}
    }
    
    /**
     * Retrieves the value from the get method matching the specified property name found in the bean
     * assigned to the specified bean name in the expressions variable context. 
     * 
     * @param beanName the bean name
     * @param propertyName the property name
     * @return the value retrieved by calling the beans get method or <code>null</code> if bean isn't found
     * 	in expression context, a method matching the property name isn't found or method returns <code>null</code>
     */
    static Object getValue(String beanName, String propertyName) {
    	try {
    		Object bean = Expression.getVariable(beanName);
    		
    		if(bean == null) {
    			return null;
    		}
    		
    		String methodName = "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    		Class<?> beanClass = bean.getClass();
    		Method method = beanClass.getMethod(methodName);
    		
    		if(method == null) {
    			return null;
    		}
    		
    		return method.invoke(bean);
    	} catch(IllegalAccessException e) {
    		// TODO, exception
    		return null;
    	} catch(InvocationTargetException e) {
    		// TODO, exception
    		return null;
    	} catch(NoSuchMethodException e) {
    		// TODO, exception
    		return null;
    	}
    }
 }
