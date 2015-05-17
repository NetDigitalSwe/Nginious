/**
 * Copyright 2013 NetDigital Sweden AB
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

/**
 * A deserializer factory creates deserializer instances for deserializing beans from other formats. Typical formats
 * are JSON and XML.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 */
public interface DeserializerFactory {
	
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
	public <T> Deserializer<T> createDeserializer(Class<T> beanClazz, String contentType) throws SerializerFactoryException;	
}
