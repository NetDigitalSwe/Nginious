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

import com.nginious.http.HttpRequest;

/**
 * A deserializer can deserialize a bean from data found in a HTTP request.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 * @param <E> the type of bean that is deserialized by this deserializer
 */
public interface Deserializer<E> {
	
	/**
	 * Returns mime type for data format that this deserializer deserializes beans from.
	 * 
	 * @return the mime type
	 */
	public String getMimeType();
	
	/**
	 * Deserializes the bean from the specified HTTP request.
	 * 
	 * @param request the given HTTP request where data for the serialized bean can be found
	 * @return the deserialized bean
	 * @throws SerializerException if unable to deserialize bean
	 */
	public E deserialize(HttpRequest request) throws SerializerException;
	
	/**
	 * Deserialized the bean from the specified message.
	 * 
	 * @param message the given message where data for the serialized bean can be found
	 * @return the deserialized bean
	 * @throws SerializerException if unable to deserialize bean
	 */
	public E deserialize(String message) throws SerializerException;
}
