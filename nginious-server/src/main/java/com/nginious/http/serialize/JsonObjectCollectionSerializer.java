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

import com.nginious.http.serialize.SerializerException;

/**
 * Serializes a collection of opaque objects into JSON format. Each object element is converted into
 * string representation.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class JsonObjectCollectionSerializer {
	
	/**
	 * Constructs a new JSON object collection serializer.
	 */
	public JsonObjectCollectionSerializer() {
		super();
	}
	
	/**
	 * Serializes the specified collection and writes it using the specified writer.
	 * 
	 * @param writer the given writer
	 * @param items the given collection of bean elements to serialize
	 * @throws SerializerException if unable to serialize collection
	 */
	public void serialize(PrintWriter writer, Collection<?> items) throws SerializerException {
		writer.println(serialize(items));
	}
	
	/**
	 * Serializes the specified collection of object elements into a JSON array.
	 * 
	 * @param items the given collection of beans to serialize
	 * @return the serialized JSON array
	 * @throws SerializerException if unable to serialize collection
	 */
	public JSONArray serialize(Collection<?> items) throws SerializerException {
		if(items == null) {
			return null;
		}
		
		JSONArray array = new JSONArray();
		
		for(Object item : items) {
			array.put(item);
		}
		
		return array;
	}
}
