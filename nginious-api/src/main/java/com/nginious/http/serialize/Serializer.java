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

/**
 * A serializer serializes a bean into serialized form. Example formats are JSON and XML. A serializer
 * for a specific bean class can be created by implementing this interface or runtime by using one of
 * the serializer creators.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 * @param <E> the type of bean serialized by this serializer
 */
public interface Serializer<E> {
	
	/**
	 * Returns mime type of format that this serializer serializes beans into.
	 * 
	 * @return the mime type.
	 */
	public String getMimeType();
	
	/**
	 * Serializes the specified item collection. The serialized collection is written using the specified
	 * writer.
	 * 
	 * @param writer the writer used for writing serialized items
	 * @param items the items to serialize
	 * @throws SerializerException if unable to serialize items
	 */
	public void serialize(PrintWriter writer, Collection<E> items) throws SerializerException;
	
	/**
	 * Serializes the specified bean item. The serialized bean is written using the specified writer.
	 * 
	 * @param writer the writer used for writing serialized bean
	 * @param item the bean to serialize
	 * @throws SerializerException if unable to serialize bean
	 */
	public void serialize(PrintWriter writer, E item) throws SerializerException;
}
