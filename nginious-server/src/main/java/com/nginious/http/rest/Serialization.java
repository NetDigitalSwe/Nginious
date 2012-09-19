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

package com.nginious.http.rest;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Provides common methods used by serializer and deserializer creator classes.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
class Serialization {
	
	/**
	 * Creates bytecode for a no argument constructor. The created bytecode also calls the no argument constructor
	 * in the superclass with the specified name.
	 * 
	 * @param writer bytecode writer
	 * @param superclassName binary name of superclass
	 */
	static void createConstructor(ClassWriter writer, String superclassName) {
		MethodVisitor visitor = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		visitor.visitCode();
        visitor.visitVarInsn(Opcodes.ALOAD, 0);
        visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, superclassName, "<init>", "()V");
        visitor.visitInsn(Opcodes.RETURN);
        visitor.visitMaxs(1, 1);
        visitor.visitEnd();
	}
	
	/**
	 * Creates binary class name from the specified class.
	 * 
	 * @param clazz the class to create a binary class name for
	 * @return the binary class name
	 */
	static String createInternalClassName(Class<?> clazz) {
		return clazz.getName().replace('.', '/');
	}

	/**
	 * Creates binary class signature for a serializer or deserializer class using the specified bean class name
	 * as type parameter and the specified class name.
	 * 
	 * @param clazzName the class name
	 * @param beanClazzName the parameter type
	 * @return the created class signature
	 */
	static String createClassSignature(String clazzName, String beanClazzName) {
		StringBuffer signature = new StringBuffer("L");
		signature.append(clazzName);
		signature.append("<L");
		signature.append(beanClazzName);
		signature.append(";>;");
		return signature.toString();
	}	
    
	/**
	 * Creates property name from bean get or set method. The property name is created by removing the "get"
	 * or "set" part then lowercasing the first word.
	 * 
	 * @param methodName the method name to create property name
	 * @return the created property name
	 */
	static String createPropertyNameFromMethodName(String methodName) {
		return methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
	}	
	
	/**
	 * Uses the specified class loader to load created bytecode in a class with the specified class name.
	 * 
	 * @param loader the class loader to load class with
	 * @param className the class name
	 * @param b the created bytecode
	 * @return the loaded class
	 */
    static Class<?> loadClass (ClassLoader loader, String className, byte[] b) {
    	Class<?> clazz = null;
    	
    	try {
    		Class<?> cls = Class.forName("java.lang.ClassLoader");
    		java.lang.reflect.Method method = cls.getDeclaredMethod("defineClass", new Class[] { String.class, byte[].class, int.class, int.class });
    		
    		// protected method invocaton
    		method.setAccessible(true);
    		
    		try {
    			Object[] args = new Object[] { className, b, new Integer(0), new Integer(b.length)};
    			clazz = (Class<?>) method.invoke(loader, args);
    		} finally {
    			method.setAccessible(false);
    		}
        } catch (Exception e) {
        	e.printStackTrace();
        	System.exit(1);
        }
        
        return clazz;
    }
}
