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

package com.nginious.http.serialize;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.nginious.http.annotation.Serializable;
import com.nginious.http.application.ApplicationClassLoader;

/**
 * <p>
 * Creates deserializers for deserializing beans from XML format. The deserializer class is created runtime
 * by building the necessary bytecode for the class. The created class is a subclass of {@link XmlDeserializer}
 * and overrides the method {@link XmlDeserializer#deserialize(java.util.HashMap)}.
 * </p>
 * 
 * <p>
 * The following outlines the steps used for creating a deserializer class
 * <ul>
 * <li>A subclass of {@link XmlDeserializer} is created by generating the appropriate bytecode.</li>
 * <li>The deserializer class name is the same as the bean class with "XmlDeserializer" appended.</li>
 * <li>The deserializer class is placed in the same package as the bean class.</li>
 * <li>The bean class is introspected searching for matching get and set property methods.</li>
 * <li>Bean set methods can be annotated with {@link Serializable}.</li>
 * <li>For each found property the appropriate byte code is generated for deserializing the property and calling the bean set method</li>
 * <li>The creator generates bytecode which calls methods in {@link XmlDeserializer} to deserialize individual properties. See list below for supported types.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * A bean is regarded as XML deserializable if it is annotated with the {@link Serializable} annotation type where the
 * deserializable property is set to <code>true</code> and types list includes the text "xml".
 * </p>
 * 
 * <p>
 * A bean property is regarded as XML deserializable if it's not annotated with the {@link Serializable} annotation or if it's
 * annotated with the {@link Serializable} annotation type where the deserializable property is set to <code>true</code>
 * and types list includes the text "xml".
 * </p>
 *
 * <p>
 * The following property types are supported
 * <ul>
 * <li>boolean - deserialized by {@link XmlDeserializer#deserializeBoolean(java.util.HashMap, String)}</li>
 * <li>double - deserialized by {@link XmlDeserializer#deserializeDouble(java.util.HashMap, String)}</li>
 * <li>float - deserialized by {@link XmlDeserializer#deserializeFloat(java.util.HashMap, String)}</li>
 * <li>int - deserialized by {@link XmlDeserializer#deserializeInt(java.util.HashMap, String)}</li>
 * <li>long - deserialized by {@link XmlDeserializer#deserializeLong(java.util.HashMap, String)}</li>
 * <li>short - deserialized by {@link XmlDeserializer#deserializeShort(java.util.HashMap, String)}</li>
 * <li>java.util.Calendar - deserialized by {@link XmlDeserializer#deserializeCalendar(java.util.HashMap, String)}</li>
 * <li>java.util.Date - deserialized by {@link XmlDeserializer#deserializeDate(java.util.HashMap, String)}</li>
 * <li>java.lang.String - deserialized by {@link XmlDeserializer#deserializeString(java.util.HashMap, String)}</li>
 * 
 * </ul>
 * </p>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
class XmlDeserializerCreator extends DeserializerCreator<XmlDeserializer<?>> {
	
	private ApplicationClassLoader classLoader;
	
	/**
	 * Constructs a new XML deserializer creator.
	 * 
	 * @param classLoader the class loader to use for loading create deserializer classes
	 */
	XmlDeserializerCreator(ApplicationClassLoader classLoader) {
		super();
		this.classLoader = classLoader;
	}
	
	/**
	 * Creates a XML deserializer for the specified bean class unless a deserializer has already
	 * been created. Created deserializers are cached and returned on subsequent calls to this method.
	 * 
	 * @param <T> class type for bean
	 * @param beanClazz bean class for which a deserializer should be created
	 * @return the created deserializer
	 * @throws SerializerFactoryException if unable to create deserializer or class is not a bean
	 */
	@SuppressWarnings("unchecked")
	protected <T> XmlDeserializer<T> create(Class<T> beanClazz) throws SerializerFactoryException {
		XmlDeserializer<T> deserializer = (XmlDeserializer<T>)deserializers.get(beanClazz);
		
		if(deserializer != null) {
			return deserializer;
		}
		
		try {
			synchronized(this) {
				deserializer = (XmlDeserializer<T>)deserializers.get(beanClazz);
				
				if(deserializer != null) {
					return deserializer;
				}
				
				checkDeserializability(beanClazz, "xml");
				String intBeanClazzName = Serialization.createInternalClassName(beanClazz);
				Method[] methods = beanClazz.getMethods();
				
				String intDeserializerClazzName = new StringBuffer(intBeanClazzName).append("XmlDeserializer").toString();
				
				// Create class
				ClassWriter writer = new ClassWriter(0);
				String signature = Serialization.createClassSignature("com/nginious/http/serialize/XmlDeserializer", intBeanClazzName);
				writer.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, intDeserializerClazzName, signature, "com/nginious/http/serialize/XmlDeserializer", null);
				
				// Create constructor
				Serialization.createConstructor(writer, "com/nginious/http/serialize/XmlDeserializer");
				
				// Create deserialize method
				MethodVisitor visitor = createDeserializeMethod(writer, intBeanClazzName);
				
				for(Method method : methods) {
					Serializable info = method.getAnnotation(Serializable.class);
					boolean canDeserialize = info == null || (info != null && info.deserialize() && info.types().indexOf("xml") > -1);
					
					if(canDeserialize && method.getName().startsWith("set") && method.getReturnType().equals(void.class) && 
							method.getParameterTypes().length == 1) {
						Class<?>[] parameterTypes = method.getParameterTypes();
						Class<?> parameterType = parameterTypes[0];
						
						if(parameterType.isArray()) {
							Class<?> arrayType = parameterType.getComponentType();
							
							if(arrayType.equals(boolean.class)) {
								createPropertyDeserializationCode(visitor, intDeserializerClazzName, "deserializeBooleanArray", "[Z", "[Z", intBeanClazzName, method.getName());
							} else if(arrayType.equals(double.class)) {
								createPropertyDeserializationCode(visitor, intDeserializerClazzName, "deserializeDoubleArray", "[D", "[D", intBeanClazzName, method.getName());
							} else if(arrayType.equals(float.class)) {
								createPropertyDeserializationCode(visitor, intDeserializerClazzName, "deserializeFloatArray", "[F", "[F", intBeanClazzName, method.getName());
							} else if(arrayType.equals(int.class)) {
								createPropertyDeserializationCode(visitor, intDeserializerClazzName, "deserializeIntArray", "[I", "[I", intBeanClazzName, method.getName());
							} else if(arrayType.equals(long.class)) {
								createPropertyDeserializationCode(visitor, intDeserializerClazzName, "deserializeLongArray", "[J", "[J", intBeanClazzName, method.getName());
							} else if(arrayType.equals(short.class)) {
								createPropertyDeserializationCode(visitor, intDeserializerClazzName, "deserializeShortArray", "[S", "[S", intBeanClazzName, method.getName());
							} else if(arrayType.equals(String.class)) {
								createPropertyDeserializationCode(visitor, intDeserializerClazzName, "deserializeStringArray", "[Ljava/lang/String;", "[Ljava/lang/String;", intBeanClazzName, method.getName());
							}
						} else if(parameterType.isPrimitive()) {
							if(parameterType.equals(boolean.class)) {
								createPropertyDeserializationCode(visitor, intDeserializerClazzName, "deserializeBoolean", "Z", "Z", intBeanClazzName, method.getName());
							} else if(parameterType.equals(double.class)) {
								createPropertyDeserializationCode(visitor, intDeserializerClazzName, "deserializeDouble", "D", "D", intBeanClazzName, method.getName());
							} else if(parameterType.equals(float.class)) {
								createPropertyDeserializationCode(visitor, intDeserializerClazzName, "deserializeFloat", "F", "F", intBeanClazzName, method.getName());
							} else if(parameterType.equals(int.class)) {
								createPropertyDeserializationCode(visitor, intDeserializerClazzName, "deserializeInt", "I", "I", intBeanClazzName, method.getName());								
							} else if(parameterType.equals(long.class)) {
								createPropertyDeserializationCode(visitor, intDeserializerClazzName, "deserializeLong", "J", "J", intBeanClazzName, method.getName());								
							} else if(parameterType.equals(short.class)) {
								createPropertyDeserializationCode(visitor, intDeserializerClazzName, "deserializeShort", "S", "S", intBeanClazzName, method.getName());								
							}
						} else if(parameterType.equals(Calendar.class)) {
							createPropertyDeserializationCode(visitor, intDeserializerClazzName, "deserializeCalendar", "Ljava/util/Calendar;", "Ljava/util/Calendar;", intBeanClazzName, method.getName());								
						} else if(parameterType.equals(Date.class)) {
							createPropertyDeserializationCode(visitor, intDeserializerClazzName, "deserializeDate", "Ljava/util/Date;", "Ljava/util/Date;", intBeanClazzName, method.getName());							
						} else if(parameterType.equals(String.class)) {
							createPropertyDeserializationCode(visitor, intDeserializerClazzName, "deserializeString", "Ljava/lang/String;", "Ljava/lang/String;", intBeanClazzName, method.getName());							
						}
					}
				}
				
				visitor.visitVarInsn(Opcodes.ALOAD, 3);
				visitor.visitInsn(Opcodes.ARETURN);
				visitor.visitMaxs(5, 4);
				visitor.visitEnd();
				
				writer.visitEnd();
				byte[] clazzBytes = writer.toByteArray();
				ClassLoader controllerLoader = null;
				
				if(classLoader.hasLoaded(beanClazz)) {
					controllerLoader = beanClazz.getClassLoader();
				} else {
					controllerLoader = this.classLoader;
				}

				Class<?> clazz = Serialization.loadClass(controllerLoader, intDeserializerClazzName.replace('/', '.'), clazzBytes);
				deserializer = (XmlDeserializer<T>)clazz.newInstance();
				deserializers.put(beanClazz, deserializer);
				return deserializer;
			}
		} catch(IllegalAccessException e) {
			throw new SerializerFactoryException(e);
		} catch(InstantiationException e) {
			throw new SerializerFactoryException(e);
		}		
	}
	
	/**
	 * Creates bytecode for deserializing property matching the specified bean method name. The generated bytecode calls the
	 * appropriate deserialization method in the class {@link XmlDeserializer} depending on the specified method type.
	 * Then generated bytecode that calls the corresponding set method in the bean class. 
	 * 
	 * @param visitor method visitor for writing bytecode
	 * @param clazzName binary name of class being generated
	 * @param methodName binary name of method in class {@link XmlDeserializer} used for deserializing property
	 * @param methodType binary type of value returned by called deserialization method in {@link XmlDeserializer}
	 * @param beanType binary type for argument in bean method for setting property
	 * @param beanClazzName binary class name of bean for which the deserializer class is being generated
	 * @param beanMethodName binary name of method in bean class for setting property
	 */
	private void createPropertyDeserializationCode(MethodVisitor visitor, String clazzName, String methodName, String methodType, String beanType, String beanClazzName, String beanMethodName) {
		String propertyName = convertToXmlName(beanMethodName);
		visitor.visitVarInsn(Opcodes.ALOAD, 3);
		visitor.visitVarInsn(Opcodes.ALOAD, 0);
		visitor.visitVarInsn(Opcodes.ALOAD, 1);
		visitor.visitLdcInsn(propertyName);
		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, clazzName, methodName, "(Ljava/util/HashMap;Ljava/lang/String;)" + methodType);
		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, beanClazzName, beanMethodName, "(" + beanType + ")V");
	}
	
	/**
	 * Creates bytecode which implements the {@link XmlDeserializer#deserialize(java.util.HashMap)} method for
	 * the deserializer class being created.
	 * 
	 * @param writer class byte code writer
	 * @param intBeanClazzName name of deserializer class being generated
	 * @return a method visitor for writing bytecode inside the generated method
	 */
	private MethodVisitor createDeserializeMethod(ClassWriter writer, String intBeanClazzName) {
		String[] exceptions = { "com/nginious/http/serialize/SerializerException" };			
		MethodVisitor visitor = writer.visitMethod(Opcodes.ACC_PUBLIC, "deserialize", "(Ljava/util/HashMap;)Ljava/lang/Object;", null, exceptions);
		visitor.visitCode();
		
		visitor.visitTypeInsn(Opcodes.NEW, intBeanClazzName);
		visitor.visitInsn(Opcodes.DUP);
		visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, intBeanClazzName, "<init>", "()V");
		visitor.visitVarInsn(Opcodes.ASTORE, 3);
		return visitor;
	}
	
	/**
	 * Converts the specified name into a XML name.
	 * 
	 * @param name the name to convert
	 * @return the converted name
	 */
	private String convertToXmlName(String name) {
		StringBuffer xmlName = new StringBuffer();
		
		for(int i = 3; i < name.length(); i++) {
			char ch = name.charAt(i);
			
			if(Character.isUpperCase(ch)) {
				if(i > 3) {
					xmlName.append('-');
				}
				
				xmlName.append(Character.toLowerCase(ch));
			} else {
				xmlName.append(ch);
			}
		}
		
		return xmlName.toString();
	}	
}
