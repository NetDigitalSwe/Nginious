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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base class for all classes that generate serializers based on a bean class as input. Serializers are created runtime
 * by introspecting bean classes and creating bytecode for serializer classes.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 * @param <E> the type of bean to create serializer for
 */
abstract class SerializerCreator<E extends Serializer<?>> {
	
	protected ConcurrentHashMap<Class<?>, E> serializers;
		
	/**
	 * Constructs a new serializer creator.
	 */
	SerializerCreator() {
		super();
		this.serializers = new ConcurrentHashMap<Class<?>, E>();
	}
	
	/**
	 * Removes all serializers created with this serializer creator and have been loaded with the specified class loader.
	 * Created serializers are normally loaded with the calling threads class loader.
	 * 
	 * @param loader the class loader to remove loaded serializers for
	 * @return whether or not any serializers have been removed
	 */
	boolean removeLoadedSerializers(ClassLoader loader) {
		Set<Class<?>> clazzes = serializers.keySet();
		boolean removed = false;
		
		for(Class<?> clazz : clazzes) {
			if(clazz.getClassLoader().equals(loader)) {
				serializers.remove(clazz);
				removed = true;
			}
		}
		
		return removed;
	}	
	
	/**
	 * Checks whether or not the element type in a collection returned by a bean method is serializable. A bean class
	 * is serializable if it is annotated with the {@link Serializable} annotation type which has the serializable
	 * property set to true and the list of format types includes the specified type.
	 * 
	 * @param method method returning collection
	 * @param type the format type to check serializability for
	 * @return the class element type
	 */
	Class<?> canSerializeGenericCollectionType(Method method, String type) {
		Type genericReturnType = method.getGenericReturnType();
		Class<?> collectionType = null;
		
		if(genericReturnType instanceof ParameterizedType) {
			ParameterizedType paramReturnType = (ParameterizedType)genericReturnType;
			Type unknownType = paramReturnType.getActualTypeArguments()[0];
			
			if(unknownType instanceof WildcardType) {
				return null;
			}
			
			collectionType = (Class<?>)unknownType;
			Serializable info = collectionType.getAnnotation(Serializable.class);
			boolean canSerialize = info != null && info.serialize() && info.types().indexOf(type) > -1;
			
			if(!canSerialize) {
				return null;
			}
		}
		
		return collectionType;
	}
	
	/**
	 * Checks whether or not a serializer can be created for the specified bean class and format type. A bean class
	 * is serializable if it is annotated with the {@link Serializable} annotation type which has the serializable
	 * property set to true and the list of format types includes the specified type.
	 * 
	 * @param beanClazz the bean class to check serializability for
	 * @param type the format type to check serializability for
	 * @throws SerializerFactoryException if bean class is not serializable
	 */
	void checkSerializability(Class<?> beanClazz, String type) throws SerializerFactoryException {
		Serializable info = beanClazz.getAnnotation(Serializable.class);
		String beanClazzName = beanClazz.getName();
		
		if(info == null) {
			throw new SerializerFactoryException("Class " + beanClazzName + " is not annotated as serializable");
		}
		
		if(!info.serialize()) {
			throw new SerializerFactoryException("Class " + beanClazzName + " is not marked as serializable");
		}
		
		if(info.types().indexOf(type) == -1) {
			throw new SerializerFactoryException("Class " + beanClazzName + " is not marked as serializable in " + type + " format");
		}
	}	
}
