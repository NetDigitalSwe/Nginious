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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base class for all classes that generate deserializers based on a bean class as input. Deserializers are created runtime
 * by introspecting bean classes and creating bytecode for deserializer classes.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 * @param <E> the type of bean to create deserializer for
 */
public abstract class DeserializerCreator<E extends Deserializer<?>> {
	
	protected ConcurrentHashMap<Class<?>, E> deserializers;
	
	/**
	 * Constructs a new deserializer creator.
	 */
	protected DeserializerCreator() {
		super();
		this.deserializers = new ConcurrentHashMap<Class<?>, E>();
	}
	
	/**
	 * Removes all deserializers created with this deserializer creator and have been loaded with the specified class loader.
	 * Created deserializers are normally loaded with the calling threads class loader.
	 * 
	 * @param loader the class loader to remove loaded deserializers for
	 * @return whether or not any deserializers have been removed
	 */
	boolean removeLoadedDeserializers(ClassLoader loader) {
		Set<Class<?>> clazzes = deserializers.keySet();
		boolean removed = false;
		
		for(Class<?> clazz : clazzes) {
			if(clazz.getClassLoader().equals(loader)) {
				deserializers.remove(clazz);
				removed = true;
			}
		}
		
		return removed;
	}	

	/**
	 * Checks whether or not a deserializer can be created for the specified bean class and format type. A bean class
	 * is deserializable if it is annotated with the {@link Serializable} annotation type which has the serializable
	 * property set to true and the list of format types includes the specified type.
	 * 
	 * @param beanClazz the bean class to check deserializability for
	 * @param type the format type to check deserializability for
	 * @throws SerializerFactoryException if bean class is not deserializable
	 */
	void checkDeserializability(Class<?> beanClazz, String type) throws SerializerFactoryException {
		Serializable info = beanClazz.getAnnotation(Serializable.class);
		String beanClazzName = beanClazz.getName();
		
		if(info == null) {
			throw new SerializerFactoryException("Class " + beanClazzName + " is not annotated as serializable");
		}
		
		if(!info.deserialize()) {
			throw new SerializerFactoryException("Class " + beanClazzName + " is not marked as serializable");
		}
		
		if(info.types().indexOf(type) == -1) {
			throw new SerializerFactoryException("Class " + beanClazzName + " is not marked as serializable in " + type + " format");
		}
	}	
}
