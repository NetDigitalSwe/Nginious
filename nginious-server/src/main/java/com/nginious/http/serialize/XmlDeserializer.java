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

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Stack;
import java.util.TimeZone;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.nginious.http.HttpRequest;
import com.nginious.http.serialize.Deserializer;
import com.nginious.http.serialize.SerializerException;

/**
 * Base class for all deserializers that deserialize beans from XML format. Used as base class
 * by {@link XmlDeserializerCreator} when creating deserializers runtime.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 * @param <E> the type of bean that is deserialized by this deserializer
 */
public abstract class XmlDeserializer<E> implements Deserializer<E> {
	
	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	
	private String baseName;
	
	/**
	 * Constructs a new XML deserializer
	 */
	public XmlDeserializer() {
		super();
		ParameterizedType type = (ParameterizedType)getClass().getGenericSuperclass();
		@SuppressWarnings("unchecked")
		Class<E> clazz = (Class<E>)type.getActualTypeArguments()[0];
		this.baseName = convertToXmlName(clazz.getSimpleName());
	}
	
	/**
	 * Returns the text/xml mime type for this deserializer.
	 * 
	 * @return the mime type for this deserializer
	 */
	public String getMimeType() {
		return "text/xml";
	}

	/**
	 * Deserializes a bean from the XML body content in the specified HTTP request.
	 * 
	 * @return the deserialized bean
	 * @throws SerializerException if unable to deserialize bean
	 */
	public E deserialize(HttpRequest request) throws SerializerException {
		if(request.getContentLength() == 0) {
			return null;
		}
		
		XMLStreamReader reader = null;
		
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			reader = factory.createXMLStreamReader(request.getReader());
			HashMap<String, String> nameValues = new HashMap<String, String>();
			Stack<String> elements = new Stack<String>();
			String name = null;
			String value = null;
			boolean first = true;
			
			while(reader.hasNext()) {
				int type = reader.next();
				
				switch(type) {
				case XMLStreamReader.START_ELEMENT:
					name = reader.getLocalName().toLowerCase();
					
					if(first) {
						if(!name.equals(this.baseName)) {
							throw new SerializerException("Can't find object '" + name + " in XML data");
						}
						
						first = false;
					}
					
					elements.push(name);
					break;
					
				case XMLStreamReader.CHARACTERS:
					if(!reader.isWhiteSpace()) {
						value = reader.getText();
					}
					break;
				
				case XMLStreamReader.END_ELEMENT:
					String endName = reader.getLocalName().toLowerCase();
					name = elements.pop();
					
					if(!endName.equals(name)) {
						throw new SerializerException("Can't find end tag '" + name + "' (" + endName + ") in XML data");
					}
					
					if(value != null) {
						nameValues.put(endName, value);
					}
					
					value = null;
					break;
				}
			}
			
			return deserialize(nameValues);
		} catch(XMLStreamException e) {
			throw new SerializerException("Can't deserialize object " + this.baseName, e);			
		} catch(IOException e) {
			throw new SerializerException("Can't deserialize object " + this.baseName, e);
		}
	}
	
	/**
	 * Deserializes the specified map of name values.
	 * 
	 * @param nameValues the map of name values to deserialize
	 * @return the deserialized bean
	 * @throws SerializerException if unable to deserialize bean
	 */
	protected abstract E deserialize(HashMap<String, String> nameValues) throws SerializerException;
	
	/**
	 * Deserializes property with the specified name from the specified name value map into a boolean value.
	 * 
	 * @param nameValues the name value map
	 * @param name the given property name
	 * @return the deserialized boolean value or <code>false</code> if property doesn't exist
	 * @throws SerializerException if unable to deserialize boolean property
	 */
	protected boolean deserializeBoolean(HashMap<String, String> nameValues, String name) throws SerializerException {
		String value = nameValues.remove(name);
		return value != null && (value.equals("true") || value.equals("1"));
	}
	
	/**
	 * Deserializes property with the specified name from the specified name value map into a calendar object.
	 * The serialized date must be in 'yyyy-MM-dd'T'HH:mm:ssZ' format.
	 * 
	 * @param nameValues name value map
	 * @param name the given property name
	 * @return the deserialized calendar or <code>null</code> if property doesn't exist
	 * @throws SerializerException if unable to deserialize calendar
	 */
	protected Calendar deserializeCalendar(HashMap<String, String> nameValues, String name) throws SerializerException {
		if(!nameValues.containsKey(name)) {
			return null;
		}
		
		String value = nameValues.get(name);
		
		if(value == null) {
			return null;
		}
		
		if(value.matches(".*[+-][0-9]{2}:[0-9]{2}$")) {
			int lastIndex = value.lastIndexOf(':');
			value = value.substring(0, lastIndex) + value.substring(lastIndex + 1);
		}
		
		Date date = parseDate(value);
		String tz = value.substring(value.length() - 5, value.length() - 2);
		TimeZone zone = TimeZone.getTimeZone("GMT" + tz);
		Calendar cal = Calendar.getInstance(zone);
		cal.setTime(date);
		return cal;
	}
	
	/**
	 * Deserializes property with the specified name from the specified name values map into a data object.
	 * The serialized date must be in 'yyyy-MM-dd'T'HH:mm:ssZ' format.
	 * 
	 * @param nameValues name values map
	 * @param name the given property name
	 * @return the deserialized data or <code>null</code> if property doesn't exist
	 * @throws SerializerException if unable to deserialize date
	 */
	protected Date deserializeDate(HashMap<String, String> nameValues, String name) throws SerializerException {
		if(!nameValues.containsKey(name)) {
			return null;
		}
		
		String value = nameValues.get(name);
		
		if(value.matches(".*[+-][0-9]{2}:[0-9]{2}$")) {
			int lastIndex = value.lastIndexOf(':');
			value = value.substring(0, lastIndex) + value.substring(lastIndex + 1);
		}
		
		return parseDate(value);
	}
	
	/**
	 * Parses the specified value into a date. The provided value must have the format
	 * 'yyyy-MM-dd'T'HH:mm:ssZ'.
	 * 
	 * @param value the provided date string value
	 * @return the created date
	 * @throws SerializerException if unable to parse value into a date
	 */
	private Date parseDate(String value) throws SerializerException {
		try {
			return format.parse(value);
		} catch(ParseException e) {
			throw new SerializerException("Can't parse date property " + value, e);
		}
	}
	
	/**
	 * Deserializes property with the specified name from the specified name value map into a double.
	 * 
	 * @param nameValues name values map
	 * @param name the property name
	 * @return the deserialized double or <code>0.0d</code> if property doesn't exist
	 * @throws SerializerException if unable to deserialize value
	 */
	protected double deserializeDouble(HashMap<String, String> nameValues, String name) throws SerializerException {
		String value = nameValues.remove(name);
		
		if(value != null) {
			try {
				return Double.parseDouble(value);
			} catch(NumberFormatException e) {
				throw new SerializerException("Can't deserialize double property " + name + " (" + value + ")", e);
			}
		}
		
		return 0.0d;
	}
	
	/**
	 * Deserializes property with the specified name from the specified name values map into a float.
	 * 
	 * @param nameValues name values map
	 * @param name the property name
	 * @return the deserialized float or <code>0.0f</code> if property doesn't exist
	 * @throws SerializerException if unable to deserialize value
	 */
	protected float deserializeFloat(HashMap<String, String> nameValues, String name) throws SerializerException {
		String value = nameValues.remove(name);
		
		if(value != null) {
			try {
				return Float.parseFloat(value);
			} catch(NumberFormatException e) {
				throw new SerializerException("Can't deserialize float property " + name + " (" + value + ")", e);
			}
		}
		
		return 0.0f;
	}
	
	/**
	 * Deserializes property with the specified name from the specified name values map into a integer.
	 * 
	 * @param nameValues name values map
	 * @param name name of the property
	 * @return the deserialized integer or <code>0</code> if property doesn't exist
	 * @throws SerializerException uf unable to deserialize value
	 */
	protected int deserializeInt(HashMap<String, String> nameValues, String name) throws SerializerException {
		String value = nameValues.remove(name);
		
		if(value != null) {
			try {
				return Integer.parseInt(value);
			} catch(NumberFormatException e) {
				throw new SerializerException("Can't deserialize int property " + name +" (" + value + ")", e);
			}
		}
		
		return 0;
	}
	
	/**
	 * Deserializes property with the specified name in the specified name values map into a long.
	 * 
	 * @param nameValues name values map
	 * @param name the property name
	 * @return the deserialized value or <code>0</code> if property doesn't exist
	 * @throws SerializerException if unable to deserialize value
	 */
	protected long deserializeLong(HashMap<String, String> nameValues, String name) throws SerializerException {
		String value = nameValues.remove(name);
		
		if(value != null) {
			try {
				return Long.parseLong(value);
			} catch(NumberFormatException e) {
				throw new SerializerException("Can't deserialize long property " + name + " (" + value + ")", e);
			}
		}
		
		return 0;		
	}
	
	/**
	 * Deserializes property with the specified name in the specified name values map into a short.
	 * 
	 * @param nameValues name values map
	 * @param name the property name
	 * @return the deserialized value or <code>0</code> if property doesn't exist
	 * @throws SerializerException if unable to deserialize value
	 */
	protected short deserializeShort(HashMap<String, String> nameValues, String name) throws SerializerException {
		String value = nameValues.remove(name);
		
		if(value != null) {
			try {
				return Short.parseShort(value);
			} catch(NumberFormatException e) {
				throw new SerializerException("Can't deserialize short property " + name + " (" + value + ")", e);
			}
		}
		
		return 0;		
	}
	
	/**
	 * Deserializes property with the specified name in the specified name values map into a string.
	 * 
	 * @param nameValues name values map
	 * @param name the property name
	 * @return the deserialized value or <code>null</code> if property doesn't exist
	 * @throws SerializerException if unable to deserialize value
	 */
	protected String deserializeString(HashMap<String, String> nameValues, String name) throws SerializerException {
		return nameValues.remove(name);
	}
	
    /**
     * Converts the specified method name to a XML tag name for use in serialized XML.
     * 
     * @param name the name to convert
     * @return the converted name
     */
	private String convertToXmlName(String name) {
		StringBuffer xmlName = new StringBuffer();
		
		for(int i = 0; i < name.length(); i++) {
			char ch = name.charAt(i);
			
			if(Character.isUpperCase(ch)) {
				if(i > 0) {
					xmlName.append('-');
				}
				
				xmlName.append(Character.toLowerCase(ch));
			} else {
				xmlName.append(ch);
			}
		}
		
		return xmlName.toString();
	}	
	
}
