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

import java.util.concurrent.atomic.AtomicLong;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Creates bytecode compiled expressions from tree value node expressions. 
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class ExpressionCompiler {
	
	private static final AtomicLong classNameCounter = new AtomicLong(1);
	
	private String classBaseName;
	
	/**
	 * Creates an expression compiler for 
	 * @param classBaseName
	 */
	public ExpressionCompiler(String classBaseName) {
		super();
		this.classBaseName = classBaseName;
	}
	
	/**
	 * Creates a compiled expression from the specified tree value node expression. The class bytecode for the 
	 * compiled expression is generated at runtime.
	 * 
	 * @param uncompiled the uncompiled tree value node expression
	 * @return the compiled expression
	 * @throws ExpressionException if unable to compile expression
	 */
	public Expression compile(TreeExpression uncompiled) throws ExpressionException {
        ClassWriter writer = new ClassWriter(0);

        // Create class
        String className = classBaseName + classNameCounter.getAndIncrement();
        String classIdentifier = className.replace('.', '/');
		writer.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, classIdentifier, "L" + classIdentifier + ";", "com/nginious/http/xsp/expr/Expression", null);
		
        // Create constructor
        MethodVisitor visitor = writer.visitMethod(Opcodes.ACC_PROTECTED, "<init>", "()V", null, null);
        visitor.visitCode();
        visitor.visitVarInsn(Opcodes.ALOAD, 0);
        visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "com/nginious/http/xsp/expr/Expression", "<init>", "()V");
        visitor.visitInsn(Opcodes.RETURN);
        visitor.visitMaxs(1, 1);
        visitor.visitEnd();
        
        // protected abstract boolean evaluateBoolean();
        visitor = writer.visitMethod(Opcodes.ACC_PROTECTED, "evaluateBoolean", "()Z", null, null);
        
        if(uncompiled.getType() == Type.BOOLEAN) {
        	uncompiled.compile(visitor);
        } else if(uncompiled.getType() == Type.DOUBLE) {
        	visitor.visitVarInsn(Opcodes.ALOAD, 0); // this
        	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classIdentifier, "evaluateDouble", "()D");
        	
        	Label endLabel = new Label();
        	Label falseLabel = new Label();
        	visitor.visitLdcInsn(0.0d);
        	visitor.visitInsn(Opcodes.DCMPL);
        	visitor.visitJumpInsn(Opcodes.IFEQ, falseLabel);
        	visitor.visitLdcInsn(true);
        	visitor.visitJumpInsn(Opcodes.GOTO, endLabel);
        	visitor.visitLabel(falseLabel);
        	visitor.visitLdcInsn(false);
        	visitor.visitLabel(endLabel);
        } else if(uncompiled.getType() == Type.INT) {
        	visitor.visitVarInsn(Opcodes.ALOAD, 0); // this
        	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classIdentifier, "evaluateInt", "()I");  
        	
        	Label endLabel = new Label();
        	Label falseLabel = new Label();
        	visitor.visitLdcInsn(0);
        	visitor.visitJumpInsn(Opcodes.IFNE, falseLabel);
        	visitor.visitLdcInsn(true);
        	visitor.visitJumpInsn(Opcodes.GOTO, endLabel);
        	visitor.visitLabel(falseLabel);
        	visitor.visitLdcInsn(false);
        	visitor.visitLabel(endLabel);        	
        } else if(uncompiled.getType() == Type.STRING) {
           	visitor.visitVarInsn(Opcodes.ALOAD, 0); // this
        	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classIdentifier, "evaluateString", "()Ljava/lang/String;");
    		visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z"); 
        }
        
        visitor.visitInsn(Opcodes.IRETURN);
        visitor.visitMaxs(5, 5);
        visitor.visitEnd();
        
        // protected abstract int evaluateInt();
        visitor = writer.visitMethod(Opcodes.ACC_PROTECTED, "evaluateInt", "()I", null, null);
        
        if(uncompiled.getType() == Type.BOOLEAN) {
        	visitor.visitVarInsn(Opcodes.ALOAD, 0); // this
        	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classIdentifier, "evaluateBoolean", "()Z");
    		
        	Label endLabel = new Label();
        	Label falseLabel = new Label();
        	visitor.visitJumpInsn(Opcodes.IFEQ, falseLabel);
        	visitor.visitLdcInsn(1);
        	visitor.visitJumpInsn(Opcodes.GOTO, endLabel);
        	visitor.visitLabel(falseLabel);
        	visitor.visitLdcInsn(0);
        	visitor.visitLabel(endLabel);
        } else if(uncompiled.getType() == Type.DOUBLE) {
        	visitor.visitVarInsn(Opcodes.ALOAD, 0); // this
        	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classIdentifier, "evaluateDouble", "()D");
    		visitor.visitInsn(Opcodes.D2I);
        } else if(uncompiled.getType() == Type.INT) {
        	uncompiled.compile(visitor);
        } else if(uncompiled.getType() == Type.STRING) {
           	visitor.visitVarInsn(Opcodes.ALOAD, 0); // this
        	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classIdentifier, "evaluateString", "()Ljava/lang/String;");
    		visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I");        	
        }
        
        visitor.visitInsn(Opcodes.IRETURN);
        visitor.visitMaxs(5, 5);
        visitor.visitEnd();
        
        // protected abstract double evaluateDouble();
        visitor = writer.visitMethod(Opcodes.ACC_PROTECTED, "evaluateDouble", "()D", null, null);
        
        if(uncompiled.getType() == Type.BOOLEAN) {
        	visitor.visitVarInsn(Opcodes.ALOAD, 0); // this
        	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classIdentifier, "evaluateBoolean", "()Z");
    		
        	Label endLabel = new Label();
        	Label falseLabel = new Label();
        	visitor.visitJumpInsn(Opcodes.IFEQ, falseLabel);
        	visitor.visitLdcInsn(1.0d);
        	visitor.visitJumpInsn(Opcodes.GOTO, endLabel);
        	visitor.visitLabel(falseLabel);
        	visitor.visitLdcInsn(0.0d);
        	visitor.visitLabel(endLabel);
        } else if(uncompiled.getType() == Type.DOUBLE) {
        	uncompiled.compile(visitor);
        } else if(uncompiled.getType() == Type.INT) {
        	visitor.visitVarInsn(Opcodes.ALOAD, 0); // this
        	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classIdentifier, "evaluateInt", "()I");
    		visitor.visitInsn(Opcodes.I2D);
        } else if(uncompiled.getType() == Type.STRING) {
        	visitor.visitVarInsn(Opcodes.ALOAD, 0); // this
        	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classIdentifier, "evaluateString", "()Ljava/lang/String;");
    		visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "parseDouble", "(Ljava/lang/String;)D");
        }
        
        visitor.visitInsn(Opcodes.DRETURN);
        visitor.visitMaxs(5, 5);
        visitor.visitEnd();
        
        // protected abstract String evaluateString();
        visitor = writer.visitMethod(Opcodes.ACC_PROTECTED, "evaluateString", "()Ljava/lang/String;", null, null);
        
        if(uncompiled.getType() == Type.BOOLEAN) {
        	visitor.visitVarInsn(Opcodes.ALOAD, 0); // this
        	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classIdentifier, "evaluateBoolean", "()Z");
    		visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "toString", "(Z)Ljava/lang/String;");
        } else if(uncompiled.getType() == Type.DOUBLE) {
        	visitor.visitVarInsn(Opcodes.ALOAD, 0); // this
        	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classIdentifier, "evaluateDouble", "()D");
    		visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "toString", "(D)Ljava/lang/String;");
        } else if(uncompiled.getType() == Type.INT) {
        	visitor.visitVarInsn(Opcodes.ALOAD, 0); // this
        	visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classIdentifier, "evaluateInt", "()I");
    		visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;");        	
        } else if(uncompiled.getType() == Type.STRING) {
        	uncompiled.compile(visitor);
        }
        
        visitor.visitInsn(Opcodes.ARETURN);
        visitor.visitMaxs(6, 6);
        visitor.visitEnd();
        
        // public abstract Type getType();        
        visitor = writer.visitMethod(Opcodes.ACC_PUBLIC, "getType", "()Lcom/nginious/http/xsp/expr/Type;", null, null);
        
        if(uncompiled.getType() == Type.BOOLEAN) {
        	visitor.visitFieldInsn(Opcodes.GETSTATIC, "com/nginious/http/xsp/expr/Type", "BOOLEAN", "Lcom/nginious/http/xsp/expr/Type;");
            visitor.visitInsn(Opcodes.ARETURN);
        } else if(uncompiled.getType() == Type.DOUBLE) {
        	visitor.visitFieldInsn(Opcodes.GETSTATIC, "com/nginious/http/xsp/expr/Type", "DOUBLE", "Lcom/nginious/http/xsp/expr/Type;");
            visitor.visitInsn(Opcodes.ARETURN);
        } else if(uncompiled.getType() == Type.INT) {
        	visitor.visitFieldInsn(Opcodes.GETSTATIC, "com/nginious/http/xsp/expr/Type", "INT", "Lcom/nginious/http/xsp/expr/Type;");
            visitor.visitInsn(Opcodes.ARETURN);
        } else if(uncompiled.getType() == Type.STRING) {
        	visitor.visitFieldInsn(Opcodes.GETSTATIC, "com/nginious/http/xsp/expr/Type", "STRING", "Lcom/nginious/http/xsp/expr/Type;");
            visitor.visitInsn(Opcodes.ARETURN);
        }
        
        visitor.visitMaxs(1, 1);
        visitor.visitEnd();
        
        try {
        	writer.visitEnd();
        	byte[] clazzBytes = writer.toByteArray();
        	Class<?> clazz = loadClass(className, clazzBytes);
        	return (Expression)clazz.newInstance();
        } catch(Exception e) {
        	throw new ExpressionException("Can't instantiate compiled expression", e);
        }
	}
	
	public Type test() {
		return Type.STRING;
	}
	
	/**
	 * Loads class bytecode from the specified buffer b into a class with the specified class name. The
	 * system class loader is used to load the class.
	 * 
	 * @param className the class name
	 * @param b the bytecode
	 * @return the loaded class
	 * @throws Exception if bytecode is invalid or a class loading error occurs
	 */
    Class<?> loadClass (String className, byte[] b) throws Exception {
    	//override classDefine (as it is protected) and define the class.
    	Class<?> clazz = null;

    	ClassLoader loader = ClassLoader.getSystemClassLoader();
    	Class<?> cls = Class.forName("java.lang.ClassLoader");
    	java.lang.reflect.Method method = cls.getDeclaredMethod("defineClass", new Class[] { String.class, byte[].class, int.class, int.class });
    	
    	// protected method invocaton
    	method.setAccessible(true);
    	
    	try {
    		Object[] args = new Object[] { className, b, new Integer(0), new Integer(b.length)};
    		clazz = (Class<?>)method.invoke(loader, args);
    	} finally {
    		method.setAccessible(false);
    	}
    	
        return clazz;
    }
}
