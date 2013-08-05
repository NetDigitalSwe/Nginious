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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Base class for all serializers that serialize beans to JSON format. Used as base class
 * by {@link JsonSerializerCreator} when creating serializers runtime.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 * 
 * @param <E> the type of bean that is serialized by this serializer
 */
public abstract class JsonSerializer<E> implements Serializer<E> {
	
	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	
	private String name;
	
	private Class<?> type;
	
	private SerializerFactory factory;
	
	/**
	 * Constructs a new JSON serializer.
	 */
	public JsonSerializer() {
		super();
	}
	
	/**
	 * Sets name of serializer to the specified name.
	 * 
	 * @param name serializer name
	 */
	protected void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Sets the class type for the bean that this serializer serializes.
	 * 
	 * @param type the class type
	 */
	protected void setType(Class<?> type) {
		this.type = type;
	}
	
	/**
	 * Returns the application/json mime type for this serializer.
	 * 
	 * @return the mime type for this serializer
	 */
	public String getMimeType() {
		return "application/json";
	}
	
	/**
	 * Sets serializer factory to the specified serializer factory
	 * 
	 * @param factory the serializer factory
	 */
	protected void setSerializerFactory(SerializerFactory factory) {
		this.factory = factory;
	}
	
	/**
	 * Returns the serializer factory
	 * 
	 * @return the serializer factory
	 */
	protected SerializerFactory getSerializerFactory() {
		return this.factory;
	}
	
	/**
	 * Serializes the specified item collection and writes the created JSON to the specified writer.
	 * 
	 * @param writer writer for writing generated JSON
	 * @param items the item collection to serialize
	 * @throws SerializerException if unable to serialize bean
	 */
	public void serialize(PrintWriter writer, Collection<E> items) throws SerializerException {
		writer.println(serialize(items));
	}
	
	/**
	 * Serializes the specified item bean and writes the created JSON to the specified writer.
	 * 
	 * @param writer writer for writing generated JSON
	 * @param item the bean to serialize
	 * @throws SerializerException if unable to serialize bean
	 */
	public void serialize(PrintWriter writer, E item) throws SerializerException {
		writer.println(serialize(item));
	}
	
	/**
	 * Serializes the specified item collection into a JSON object.
	 * 
	 * @param items the item collection to serialize
	 * @return the serialized JSON object
	 * @throws SerializerException if unable to serialize item collection
	 */
	public JSONObject serialize(Collection<E> items) throws SerializerException {
		try {
			JsonBeanCollectionSerializer<E> serializer = new JsonBeanCollectionSerializer<E>(this);
			JSONArray outItems = serializer.serialize(items);
			String name = Serialization.createPropertyNameFromClass(this.type) + "s";
			JSONObject object = new JSONObject();
			object.put(name, outItems);
			return object;
		} catch(JSONException e) {
			throw new SerializerException("Can't serialize collection " + this.name);			
		}
	}
	
	/**
	 * Serializes the specified item bean into a JSON object.
	 * 
	 * @param item the bean to seralize
	 * @return the serialized JSON object
	 * @throws SerializerException if unable to serialize bean
	 */
	public JSONObject serialize(E item) throws SerializerException {
		try {
			JSONObject object = new JSONObject();
			JSONObject subObject = new JSONObject();
			serializeProperties(subObject, item);
			object.put(this.name, subObject);
			return object;
		} catch(JSONException e) {
			throw new SerializerException("Can't serialize object " + this.name);
		}
	}
	
	/**
	 * Serializes properties in the specified item bean and adds them to the specified JSON object. This
	 * method must be implemented by subclasses that implemented JSON serializers for specific bean types.
	 * {@link JsonSerializerCreator} creates JSON serializer classes runtime which override this method.
	 * 
	 * @param object the JSON object to add properties to
	 * @param item the bean to serialize
	 * @throws SerializerException if unable to serialize bean
	 */
	protected abstract void serializeProperties(JSONObject object, E item) throws SerializerException;
	
	/**
	 * Serializes property with the specified name and boolean value to the specified JSON object.
	 * 
	 * @param object the JSON object
	 * @param name the property name
	 * @param value the boolean property value
	 * @throws SerializerException if unable to serialize property to JSON object
	 */
	protected void serializeBoolean(JSONObject object, String name, boolean value) throws SerializerException {
		try {
			object.put(name, value);
		} catch(JSONException e) {
			throw new SerializerException("Can't serialize boolean " + name + " (" + value + ")", e);
		}
	}
	
	/**
	 * Serializes property with the specified name and value to the specified JSON object as a date.
	 * 
	 * @param object the JSON object
	 * @param name the property name
	 * @param value the property value
	 * @throws SerializerException if unable to serialize property to JSON object
	 */
	protected void serializeCalendar(JSONObject object, String name, Calendar value) throws SerializerException {
		if(value != null) {
			serializeDate(object, name, value.getTime());
		}
	}
	
	/**
	 * Serializes property with the specified name and value to the specified JSON object as a date.
	 * 
	 * @param object the JSON object
	 * @param name the property name
	 * @param value the property value
	 * @throws SerializerException if unable to serialize property to JSON object
	 */
	protected void serializeDate(JSONObject object, String name, Date value) throws SerializerException {
		try {
			if(value != null) {
				String formatted = format.format(value);
				formatted = formatted.substring(0, formatted.length() - 2) + ":" + formatted.substring(formatted.length() - 2);
				object.put(name, formatted);
			}
		} catch(JSONException e) {
			throw new SerializerException("Can't serialize date " + name + " (" + value + ")", e);
		}
	}
	
	/**
	 * Serializes property with the specified name and double value to the specified JSON object.
	 * 
	 * @param object the JSON object
	 * @param name the property name
	 * @param value the property value
	 * @throws SerializerException if unable serialize property to JSON object
	 */
	protected void serializeDouble(JSONObject object, String name, double value) throws SerializerException {
		try {
			object.put(name, value);
		} catch(JSONException e) {
			throw new SerializerException("Can't serialize double " + name + " (" + value + ")", e);
		}
	}
	
	/**
	 * Serializes property with the specified name and float value to the specified JSON object.
	 * 
	 * @param object the JSON object
	 * @param name the property name
	 * @param value the property value
	 * @throws SerializerException if unable to serialize property to JSON object
	 */
	protected void serializeFloat(JSONObject object, String name, float value) throws SerializerException {
		try {
			object.put(name, value);
		} catch(JSONException e) {
			throw new SerializerException("Can't serialize float " + name + " (" + value + ")", e);
		}
	}
	
	/**
	 * Serializes property with the specified name and int value to the specified JSON object.
	 * 
	 * @param object the JSON object
	 * @param name the property name
	 * @param value the property value
	 * @throws SerializerException if unable to serialize property to JSON object
	 */
	protected void serializeInt(JSONObject object, String name, int value) throws SerializerException {
		try {
			object.put(name, value);
		} catch(JSONException e) {
			throw new SerializerException("Can't serialize int " + name + " (" + value + ")", e);
		}
	}
	
	/**
	 * Serializes property with the specified name and long value to the specified JSON object.
	 * 
	 * @param object the JSON object
	 * @param name the property name
	 * @param value the property value
	 * @throws SerializerException if unable to serialize property to JSON object
	 */
	protected void serializeLong(JSONObject object, String name, long value) throws SerializerException {
		try {
			object.put(name, value);
		} catch(JSONException e) {
			throw new SerializerException("Can't serialize long " + name + " (" + value + ")", e);
		}
	}
	
	/**
	 * Serializes property with the specified name and opaque object to the specified JSON object. The
	 * value is converted to string representation using the object toString method.
	 * 
	 * @param object the JSON object
	 * @param name the property name
	 * @param value the property value
	 * @throws SerializerException if unable to serialize property to JSON object
	 */
	protected void serializeObject(JSONObject object, String name, Object value) throws SerializerException {
		try {
			if(value != null) {
				object.put(name, value);
			}
		} catch(JSONException e) {
			throw new SerializerException("Can't serialize object " + name + " (" + value + ")", e);
		}
	}
	
	/**
	 * Serializes property with the specified name and short value to the specified JSON object.
	 * 
	 * @param object the JSON object
	 * @param name the property name
	 * @param value the property value
	 * @throws SerializerException if unable to serialize property to JSON object
	 */
	protected void serializeShort(JSONObject object, String name, short value) throws SerializerException {
		try {
			object.put(name, value);
		} catch(JSONException e) {
			throw new SerializerException("Can't serialize short " + name + " (" + value + ")", e);
		}
	}
	
	/**
	 * Serializes property with the specified name and value to the specified JSON object.
	 * 
	 * @param object the JSON object
	 * @param name the property name
	 * @param value the property value
	 * @throws SerializerException if unable to serialize property to JSON object
	 */
	protected void serializeString(JSONObject object, String name, String value) throws SerializerException {
		try {
			if(value != null) {
				object.put(name, value);
			}
		} catch(JSONException e) {
			throw new SerializerException("Can't serialize string " + name + " (" + value + ")", e);
		}
	}
}
