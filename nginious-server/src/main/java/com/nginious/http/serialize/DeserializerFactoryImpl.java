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

import com.nginious.http.application.ApplicationClassLoader;

/**
 * Factory for constructing deserializers.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class DeserializerFactoryImpl implements DeserializerFactory {
	
	private XmlDeserializerCreator xmlCreator;
	
	private JsonDeserializerCreator jsonCreator;
	
	private QueryDeserializerCreator queryCreator;
	
	/**
	 * Constructs a new deserializer factory with the specified class loader.
	 * 
	 * @param classLoader the class loader to use for loading created deserializer classes
	 */
	public DeserializerFactoryImpl(ApplicationClassLoader classLoader) {
		super();
		this.xmlCreator = new XmlDeserializerCreator(classLoader);
		this.jsonCreator = new JsonDeserializerCreator(classLoader);
		this.queryCreator = new QueryDeserializerCreator(classLoader);
	}
	
	/**
	 * Creates deserializer for the specified bean class that deserializes from the specified content type unless a
	 * deserializer for the specified combination of bean class and content type already exists.
	 * 
	 * @param <T> the type of bean to create deserializer for
	 * @param beanClazz the given bean class to create deserializer for
	 * @param contentType the given content type
	 * @return the constructed deserializer
	 * @throws SerializerFactoryException if unable to construct deserializer
	 */
	public <T> Deserializer<T> createDeserializer(Class<T> beanClazz, String contentType) throws SerializerFactoryException {
		if(contentType == null) {
			contentType = "application/json";
		}
		
		Deserializer<T> deserializer = null;
		
		if(contentType.equals("application/json")) {
			deserializer = jsonCreator.create(beanClazz);
		} else if(contentType.equals("text/xml")) {
			deserializer = xmlCreator.create(beanClazz);
		} else if(contentType.equals("application/x-www-form-urlencoded")) {
			deserializer = queryCreator.create(beanClazz);
		}
		
		return deserializer;
	}
	
	/**
	 * Flushes any deserializers loaded with the specified class loader.
	 * 
	 * @param loader the given class loader
	 * @return <code>true</code> if any deserializer have been removed, <code>false</code> otherwise
	 */
	public boolean removeLoadedDeserializers(ClassLoader loader) {
		boolean removed = false;
		
		if(xmlCreator.removeLoadedDeserializers(loader)) {
			removed = true;
		}
		
		if(jsonCreator.removeLoadedDeserializers(loader)) {
			removed = true;
		}
		
		if(queryCreator.removeLoadedDeserializers(loader)) {
			removed = true;
		}
		
		return removed;
	}
}
