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

import java.io.PrintWriter;
import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONObject;

import com.nginious.http.serialize.SerializerException;

/**
 * Serializes a collection of beans into JSON format. Each collection element is serialized separately using
 * a separate element serializer.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 * @param <E> the type of bean collections to serialize
 */
public class JsonBeanCollectionSerializer<E> {
	
	private JsonSerializer<E> elementSerializer;
	
	/**
	 * Constructs a new JSON bean collection serializer with the specified bean element serializer.
	 * 
	 * @param elementSerializer the given serializer to use for serializing individual collection elements 
	 */
	public JsonBeanCollectionSerializer(JsonSerializer<E> elementSerializer) {
		this.elementSerializer = elementSerializer;
	}
	
	/**
	 * Serializes the specified collection and writes it using the specified writer.
	 * 
	 * @param writer the given writer
	 * @param items the given collection of bean elements to serialize
	 * @throws SerializerException if unable to serialize collection
	 */
	public void serialize(PrintWriter writer, Collection<E> items) throws SerializerException {
		writer.println(serialize(items));
	}
	
	/**
	 * Serializes the specified collection of bean elements into a JSON array.
	 * 
	 * @param items the given collection of beans to serialize
	 * @return the serialized JSON array
	 * @throws SerializerException if unable to serialize collection
	 */
	public JSONArray serialize(Collection<E> items) throws SerializerException {
		if(items == null) {
			return null;
		}
		
		JSONArray array = new JSONArray();
		
		for(E item : items) {
			JSONObject object = elementSerializer.serialize(item);
			array.put(object);
		}
		
		return array;
	}
}
