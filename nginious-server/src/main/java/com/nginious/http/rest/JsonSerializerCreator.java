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

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * <p>
 * Creates serializers for serializing beans to JSON format. The serializer class is created runtime
 * by building the necessary bytecode for the class. The created class is a subclass of {@link JsonSerializer}
 * and overrides the method {@link JsonSerializer#serializeProperties(org.json.JSONObject, Object)}.
 * </p>
 * 
 * <p>
 * The following outlines the steps used for creating a serializer class
 * <ul>
 * <li>A subclass of {@link JsonSerializer} is created by generating the appropriate bytecode.</li>
 * <li>The serializer class name is the same as the bean class with "JsonSerializer" appended.</li>
 * <li>The serializer class is placed in the same package as the bean class.</li>
 * <li>The bean class is introspected searching for matching get and set property methods.</li>
 * <li>Bean set methods can be annotated with {@link Serializable}.</li>
 * <li>For each found property the appropriate bytecode is generated for calling each bean get methd and serializing the property.</li>
 * <li>The creator generates bytecode which calls methods in {@link JsonSerializer} to serialize individual properties. See list below for supported types.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * A bean is regarded as JSON serializable if it is annotated with the {@link Serializable} annotation type where the
 * serializable property is set to <code>true</code> and types list includes the text "json".
 * </p>
 * 
 * <p>
 * A bean property is regarded as JSON serializable if it's not annotated with the {@link Serializable} annotation or if it's
 * annotated with the {@link Serializable} annotation type where the serializable property is set to <code>true</code>
 * and types list includes the text "json".
 * </p>
 * 
 * <p>
 * The following property types are supported
 * <ul>
 * <li>boolean - serialized by {@link JsonSerializer#serializeBoolean(org.json.JSONObject, String, boolean)}</li>
 * <li>double - serialized by {@link JsonSerializer#serializeDouble(org.json.JSONObject, String, double)}</li>
 * <li>float - serialized by {@link JsonSerializer#serializeFloat(org.json.JSONObject, String, float)}</li>
 * <li>int - serialized by {@link JsonSerializer#serializeInt(org.json.JSONObject, String, int)}</li>
 * <li>long - serialized by {@link JsonSerializer#serializeLong(org.json.JSONObject, String, long)}</li>
 * <li>short - serialized by {@link JsonSerializer#serializeShort(org.json.JSONObject, String, short)}</li>
 * <li>java.util.Calendar - serialized by {@link JsonSerializer#serializeCalendar(org.json.JSONObject, String, Calendar)}</li>
 * <li>java.util.Date - serialized by {@link JsonSerializer#serializeDate(org.json.JSONObject, String, Date)}</li>
 * <li>java.lang.String - serialized by {@link JsonSerializer#serializeString(org.json.JSONObject, String, String)}</li>
 * <li>java.lang.Object - serialized by {@link JsonSerializer#serializeObject(org.json.JSONObject, String, Object)}</li>
 * 
 * </ul>
 * 
 * In addition to the above types, if a property is a serializable bean as defined above the 
 * {@link JsonSerializerCreator#create(Class)} method is called recursively to create a serializer for the property. 
 * The created serializer is then used for serializing the property.
 * </p>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
class JsonSerializerCreator extends SerializerCreator<JsonSerializer<?>> {
	
	/**
	 * Constructs a new JSON serializer creator.
	 */
	JsonSerializerCreator() {
		super();
	}
	
	/**
	 * Creates a JSON serializer for the specified bean class unless a serializer has already been
	 * created. Created serializers are cached and returned on subsequent calls to this method.
	 * 
	 * @param <T> class type for bean
	 * @param beanClazz bean class for which a serializer should be created
	 * @return the created serializer
	 * @throws SerializerFactoryException if unable to create serializer or class is not a bean
	 */
	@SuppressWarnings("unchecked")
	<T> JsonSerializer<T> create(Class<T> beanClazz) throws SerializerFactoryException {
		JsonSerializer<T> serializer = (JsonSerializer<T>)serializers.get(beanClazz);
		
		if(serializer != null) {
			return serializer;
		}
		
		try {
			synchronized(this) {
				serializer = (JsonSerializer<T>)serializers.get(beanClazz);
				
				if(serializer != null) {
					return serializer;
				}
				
				checkSerializability(beanClazz, "json");
				String intBeanClazzName = Serialization.createInternalClassName(beanClazz);
				Method[] methods = beanClazz.getMethods();
				
				String intSerializerClazzName = new StringBuffer(intBeanClazzName).append("JsonSerializer").toString();
				
				// Create class
				ClassWriter writer = new ClassWriter(0);
				String signature = Serialization.createClassSignature("com/nginious/http/rest/JsonSerializer", intBeanClazzName);
				writer.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, intSerializerClazzName, signature, "com/nginious/http/rest/JsonSerializer", null);
				
				// Create constructor
				Serialization.createConstructor(writer, "com/nginious/http/rest/JsonSerializer");
				
				// Create serialize method
				MethodVisitor visitor = createSerializeMethod(writer, intBeanClazzName);
				
				for(Method method : methods) {
					Serializable info = method.getAnnotation(Serializable.class);
					boolean canSerialize = info == null || (info != null && info.serialize() && info.types().indexOf("json") > -1);
					
					if(canSerialize && method.getName().startsWith("get") && !method.getName().equals("getClass") && 
							method.getReturnType() != null && method.getParameterTypes().length == 0) {
						Class<?> returnType = method.getReturnType();
						
						if(returnType.isPrimitive()) {
							if(returnType.equals(boolean.class)) {
								createPropertySerializationCode(visitor, intSerializerClazzName, "serializeBoolean", "Z", "Z", intBeanClazzName, method.getName());
							} else if(returnType.equals(double.class)) {
								createPropertySerializationCode(visitor, intSerializerClazzName, "serializeDouble", "D", "D", intBeanClazzName, method.getName());
							} else if(returnType.equals(float.class)) {
								createPropertySerializationCode(visitor, intSerializerClazzName, "serializeFloat", "F", "F", intBeanClazzName, method.getName());
							} else if(returnType.equals(int.class)) {
								createPropertySerializationCode(visitor, intSerializerClazzName, "serializeInt", "I", "I", intBeanClazzName, method.getName());
							} else if(returnType.equals(long.class)) {
								createPropertySerializationCode(visitor, intSerializerClazzName, "serializeLong", "J", "J", intBeanClazzName, method.getName());
							} else if(returnType.equals(short.class)) {
								createPropertySerializationCode(visitor, intSerializerClazzName, "serializeShort", "S", "S", intBeanClazzName, method.getName());
							}
						} else if(Collection.class.isAssignableFrom(returnType)) {
							Class<?> collectionType = canSerializeGenericCollectionType(method, "json");
							
							if(collectionType != null) {
								createBeanCollectionSerializationCode(visitor, intBeanClazzName, method.getName(), returnType, collectionType);
							} else {
								createObjectCollectionSerializationCode(visitor, returnType, intBeanClazzName, method.getName());
							}
						} else if(returnType.equals(Calendar.class)) {
							createPropertySerializationCode(visitor, intSerializerClazzName, "serializeCalendar", "Ljava/util/Calendar;", "Ljava/util/Calendar;", intBeanClazzName, method.getName());
						} else if(returnType.equals(Date.class)) {
							createPropertySerializationCode(visitor, intSerializerClazzName, "serializeDate", "Ljava/util/Date;", "Ljava/util/Date;", intBeanClazzName, method.getName());
						} else if(returnType.equals(String.class)) {
							createPropertySerializationCode(visitor, intSerializerClazzName, "serializeString", "Ljava/lang/String;", "Ljava/lang/String;", intBeanClazzName, method.getName());
						} else {
							info = returnType.getAnnotation(Serializable.class);
							canSerialize = info != null && info.serialize() && info.types().indexOf("json") > -1;
							
							if(canSerialize) {
								createBeanSerializationCode(visitor, method.getName(), returnType, intBeanClazzName);
							} else {
								createPropertySerializationCode(visitor, intSerializerClazzName, "serializeObject", "Ljava/lang/Object;", "L" + returnType.getName().replace('.', '/') + ";", intBeanClazzName, method.getName());
							}
						}					
					}
				}
				
				visitor.visitInsn(Opcodes.RETURN);
				visitor.visitMaxs(7, 6);
				visitor.visitEnd();
				
				writer.visitEnd();
				byte[] clazzBytes = writer.toByteArray();
				ClassLoader loader = beanClazz.getClassLoader();
				Class<?> clazz = Serialization.loadClass(loader, intSerializerClazzName.replace('/', '.'), clazzBytes);
				serializer = (JsonSerializer<T>)clazz.newInstance();
				String propertyName = createPropertyNameFromClassName(beanClazz.getSimpleName());
				serializer.setName(propertyName);
				serializers.put(beanClazz, serializer);
				return serializer;
			}
		} catch(IllegalAccessException e) {
			throw new SerializerFactoryException("Can't create JSON serializer for " + beanClazz.getName(), e);
		} catch(InstantiationException e) {
			throw new SerializerFactoryException("Can't create JSON serializer for " + beanClazz.getName(), e);			
		}		
	}
	
	/**
	 * Creates bytecode for serializing a bean property which returns a collection of opaque objects.
	 * 
	 * @param visitor method visitor used for creating bytecode
	 * @param returnType return type of get method in bean
	 * @param intBeanClazzName binary name of bean
	 * @param methodName binary name of get method in bean
	 */
	private void createObjectCollectionSerializationCode(MethodVisitor visitor, Class<?> returnType, String intBeanClazzName, String methodName) {
		visitor.visitTypeInsn(Opcodes.NEW, "com/nginious/http/rest/JsonObjectCollectionSerializer");
		visitor.visitInsn(Opcodes.DUP);
		visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "com/nginious/http/rest/JsonObjectCollectionSerializer", "<init>", "()V");
		visitor.visitVarInsn(Opcodes.ASTORE, 4);
		
		visitor.visitVarInsn(Opcodes.ALOAD, 4);
		String intReturnClazzName = returnType.getName().replace('.', '/');
		visitor.visitVarInsn(Opcodes.ALOAD, 3);
		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, intBeanClazzName, methodName, "()L" + intReturnClazzName + ";");
		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/nginious/http/rest/JsonObjectCollectionSerializer", "serialize", "(Ljava/util/Collection;)Lorg/json/JSONArray;");
		visitor.visitVarInsn(Opcodes.ASTORE, 4);
		
		visitor.visitVarInsn(Opcodes.ALOAD, 1);
		String propertyName = Serialization.createPropertyNameFromMethodName(methodName);
		visitor.visitLdcInsn(propertyName);
		visitor.visitVarInsn(Opcodes.ALOAD, 4);
		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "org/json/JSONObject", "put", "(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;");
	}
	
	/**
	 * Creates bytecode for serializing a bean property which returns a collection of beans that are serializable. Bean serializability
	 * is determined as described in the class description.
	 * 
	 * @param visitor method visitor used for creating bytecode
	 * @param intBeanClazzName binary class name of bean
	 * @param methodName binary name of get method in bean returning collection
	 * @param returnType return type of get method in bean
	 * @param collectionBeanType class of serializable bean found in collection
	 */
	private void createBeanCollectionSerializationCode(MethodVisitor visitor, String intBeanClazzName, String methodName, Class<?> returnType, Class<?> collectionBeanType) {
		visitor.visitLdcInsn(collectionBeanType.getName());
		visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;");
		visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "com/nginious/http/rest/SerializerFactory", "createJsonSerializer", "(Ljava/lang/Class;)Lcom/nginious/http/rest/JsonSerializer;");
		visitor.visitVarInsn(Opcodes.ASTORE, 4);
		
		visitor.visitTypeInsn(Opcodes.NEW, "com/nginious/http/rest/JsonBeanCollectionSerializer");
		visitor.visitInsn(Opcodes.DUP);
		visitor.visitVarInsn(Opcodes.ALOAD, 4);
		visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "com/nginious/http/rest/JsonBeanCollectionSerializer", "<init>", "(Lcom/nginious/http/rest/JsonSerializer;)V");
		visitor.visitVarInsn(Opcodes.ASTORE, 5);
		
		visitor.visitVarInsn(Opcodes.ALOAD, 5);
		String intReturnClazzName = returnType.getName().replace('.', '/');
		visitor.visitVarInsn(Opcodes.ALOAD, 3);
		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, intBeanClazzName, methodName, "()L" + intReturnClazzName + ";");
		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/nginious/http/rest/JsonBeanCollectionSerializer", "serialize", "(Ljava/util/Collection;)Lorg/json/JSONArray;");
		visitor.visitVarInsn(Opcodes.ASTORE, 5);
		
		visitor.visitVarInsn(Opcodes.ALOAD, 1);
		String propertyName = Serialization.createPropertyNameFromMethodName(methodName);
		visitor.visitLdcInsn(propertyName);
		visitor.visitVarInsn(Opcodes.ALOAD, 5);
		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "org/json/JSONObject", "put", "(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;");		
	}
	
	/**
	 * Creates bytecode for serializing a bean property which is in itself a serializable bean as defined in the class description.
	 * 
	 * @param visitor method visitor used for creating bytecode
	 * @param returnMethodName binary name of get method in bean that returns serializable bean
	 * @param returnType class of serializable bean
	 * @param intBeanClazzName binary class name of bean
	 */
	private void createBeanSerializationCode(MethodVisitor visitor, String returnMethodName, Class<?> returnType, String intBeanClazzName) {
		String intReturnClazzName = Serialization.createInternalClassName(returnType);
		visitor.visitLdcInsn(returnType.getName());
		visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;");

		visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "com/nginious/http/rest/SerializerFactory", "createJsonSerializer", "(Ljava/lang/Class;)Lcom/nginious/http/rest/JsonSerializer;");
		visitor.visitVarInsn(Opcodes.ASTORE, 4);
		
		visitor.visitVarInsn(Opcodes.ALOAD, 4);
		visitor.visitVarInsn(Opcodes.ALOAD, 3);
		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, intBeanClazzName, returnMethodName, "()L" + intReturnClazzName + ";");
		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/nginious/http/rest/JsonSerializer", "serialize", "(Ljava/lang/Object;)Lorg/json/JSONObject;");
		visitor.visitVarInsn(Opcodes.ASTORE, 4);
		
		visitor.visitVarInsn(Opcodes.ALOAD, 1);
		String propertyName = Serialization.createPropertyNameFromMethodName(returnMethodName);
		visitor.visitLdcInsn(propertyName);
		visitor.visitVarInsn(Opcodes.ALOAD, 4);
		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "org/json/JSONObject", "put", "(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;");
	}
	
	/**
	 * Creates bytecode for serializing property matching the specified bean method name. The generated bytecode calls the
	 * appropriate serialization method in the class {@link JsonSerializer} depending on the specified method type.
	 * 
	 * @param visitor method visitor for generating bytecode
	 * @param clazzName binary name of serializer class being generated
	 * @param methodName binary name of method in class {@link JsonSerializer} used for serializing property
	 * @param methodType binary type for method in class {@link JsonSerializer} used for serializing property
	 * @param beanType binary return type of get method in bean
	 * @param beanClazzName binary name of bean class
	 * @param beanMethodName binary name of get method in bean for getting method
	 */
	private void createPropertySerializationCode(MethodVisitor visitor, String clazzName, String methodName, String methodType, String beanType, String beanClazzName, String beanMethodName) {
		String propertyName = Serialization.createPropertyNameFromMethodName(beanMethodName);
		visitor.visitVarInsn(Opcodes.ALOAD, 0);
		visitor.visitVarInsn(Opcodes.ALOAD, 1);
		visitor.visitLdcInsn(propertyName);
		visitor.visitVarInsn(Opcodes.ALOAD, 3);
		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, beanClazzName, beanMethodName, "()" + beanType);
		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, clazzName, methodName, "(Lorg/json/JSONObject;Ljava/lang/String;" + methodType + ")V");
	}
	
	/**
	 * Creates bytecode which implements the {@link JsonSerializer#serializeProperties(org.json.JSONObject, Object)}
	 * method for the serializer class being created.
	 * 
	 * @param writer class byte code writer
	 * @param intBeanClazzName binary name of serializer class being generated
	 * @return a method visitor for writing bytecode inside the generated method
	 */
	private MethodVisitor createSerializeMethod(ClassWriter writer, String intBeanClazzName) {
		String[] exceptions = { "com/nginious/rest/SerializerException" };			
		MethodVisitor visitor = writer.visitMethod(Opcodes.ACC_PUBLIC, "serializeProperties", "(Lorg/json/JSONObject;Ljava/lang/Object;)V", null, exceptions);
		visitor.visitCode();
		
		Label label = new Label();
		visitor.visitVarInsn(Opcodes.ALOAD, 2);
		visitor.visitJumpInsn(Opcodes.IFNONNULL, label);
		visitor.visitInsn(Opcodes.RETURN);
		visitor.visitLabel(label);
		visitor.visitVarInsn(Opcodes.ALOAD, 2);
		visitor.visitTypeInsn(Opcodes.CHECKCAST, intBeanClazzName);
		visitor.visitIntInsn(Opcodes.ASTORE, 3);
		
		return visitor;
	}
	
	/**
	 * Creates property name from the specified class name. The property is the same as the class name with the
	 * first character in lower case.
	 * 
	 * @param className the given class name
	 * @return the property name
	 */
	private String createPropertyNameFromClassName(String className) {
		return className.substring(0, 1).toLowerCase() + className.substring(1);
	}
}
