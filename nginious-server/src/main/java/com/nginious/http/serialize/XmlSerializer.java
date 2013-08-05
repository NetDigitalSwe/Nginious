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

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Base class for all serializers that serialize beans to XML format. Used as base class
 * by {@link XmlSerializerCreator} when creating serializers runtime.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 * 
 * @param <E> the type of bean that is serialized by this serializer
 */
public abstract class XmlSerializer<E> implements Serializer<E> {
	
	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	
	private String name;
	
	private Class<?> type;
	
	private SerializerFactory factory;
	
	/**
	 * Constructs a new XML serializer.
	 */
	public XmlSerializer() {
		super();
	}
	
	/**
	 * Sets name of serializer to the specified name.
	 * 
	 * @param name serializer name
	 */
	protected void setName(String name) {
		this.name = convertToXmlName(name);
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
	 * Returns the text/xml mime type for this serializer.
	 * 
	 * @return the mime type for this serializer
	 */
	public String getMimeType() {
		return "text/xml";
	}
	
	/**
	 * Serializes the specified collection of items and writes the created XML to the specified writer.
	 * 
	 * @param writer writer for writing generated XML
	 * @param items the items to serialize
	 * @throws SerializerException if unable to serialize bean
	 */
	public void serialize(PrintWriter writer, Collection<E> items) throws SerializerException {
		TransformerHandler handler = createTransformerHandler(writer);
		String name = Serialization.createPropertyNameFromClass(this.type) + "s";
		XmlBeanCollectionSerializer<E> serializer = new XmlBeanCollectionSerializer<E>(name, this);
		serializer.serialize(handler, items);
	}
	
	/**
	 * Serializes the specified item bean an writes the created XML to the specified writer.
	 * 
	 * @param writer writer for writing generated XML
	 * @param item the bean to serialize
	 * @throws SerializerException if unable to serialize bean
	 */
	public void serialize(PrintWriter writer, E item) throws SerializerException {
		TransformerHandler handler = createTransformerHandler(writer);
		serialize(handler, item);
	}
	
    protected TransformerHandler createTransformerHandler(PrintWriter writer) throws SerializerException {
    	try {
    		StreamResult streamResult = new StreamResult(writer);
    		SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
    		tf.setAttribute("indent-number", new Integer(2));
    		TransformerHandler handler = tf.newTransformerHandler();
    		Transformer serializer = handler.getTransformer();
    		serializer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
    		serializer.setOutputProperty(OutputKeys.INDENT, "yes");
    		handler.setResult(streamResult);
    		return handler;
    	} catch(TransformerConfigurationException e) {
    		throw new SerializerException("Can't initialize serialization", e);
    	}
    }
    
	/**
	 * Serializes the specified item bean into XML using the specified handler.
	 * 
	 * @param handler the XML handler to use for generating XML
	 * @param item the bean to seralize
	 * @throws SerializerException if unable to serialize bean
	 */
	public void serialize(TransformerHandler handler, E item) throws SerializerException {
		try {
			AttributesImpl attrs = new AttributesImpl();
			
			handler.startElement("", "", this.name, attrs);
			serializeProperties(handler, item);
			handler.endElement("", "", this.name);
		} catch(SAXException e) {
			throw new SerializerException("Can't serialize " + this.name, e);
		}
	}
	
	/**
	 * Serializes the specified item bean into XML using the provided handler. The specified name is used
	 * for the start XML element.
	 * 
	 * @param handler the XML handler to use for generating XML
	 * @param name name of the start XML element
	 * @param item the item bean to serialize property for
	 * @throws SerializerException if unable to serialize bean
	 */
	public void serialize(TransformerHandler handler, String name, E item) throws SerializerException {
		if(item == null) {
			return;
		}
		
		try {
			AttributesImpl attrs = new AttributesImpl();
			
			handler.startElement("", "", name, attrs);
			handler.startElement("", "", this.name, attrs);
			serializeProperties(handler, item);
			handler.endElement("", "", this.name);
			handler.endElement("", "", name);
		} catch(SAXException e) {
			throw new SerializerException("Can't serialize " + this.name, e);
		}
	}
	
	/**
	 * Serializes all properties in the specified bean item using the provided handler to generate XML. This
	 * method must be implemented by subclasses that whish to implement serializers for specific bean classes. 
	 * 
	 * @param handler the XML handler to use for generating XML
	 * @param item the bean item to serialize
	 * @throws SerializerException if unable to serialize bean
	 */
	protected abstract void serializeProperties(TransformerHandler handler, E item) throws SerializerException;
	
	/**
	 * Serializes the specified boolean value into XML using the specified name as XML element name.
	 * 
	 * @param handler the XML handler to use for generating XML
	 * @param name the XML element name
	 * @param value the value to serialize
	 * @throws SerializerException if unable to serialize property
	 */
	protected void serializeBoolean(TransformerHandler handler, String name, boolean value) throws SerializerException {
		serializeString(handler, name, value ? "true" : "false");
	}
	
	/**
	 * Serializes the specified calendar value into XML using the specified name as XML element name. The provided
	 * calendar is serialized into 'yyyy-MM-dd'T'HH:mm:ssZ' format.
	 * 
	 * @param handler the XML handler to use for generating XML
	 * @param name the XML element name
	 * @param value the value to serialize
	 * @throws SerializerException if unable to serialize property
	 */
	protected void serializeCalendar(TransformerHandler handler, String name, Calendar value) throws SerializerException {
		if(value != null) {
			serializeDate(handler, name, value.getTime());
		}
	}
	
	/**
	 * Serializes the specified date value into XML using the specified name as XML element name. The provided
	 * date is serialized into 'yyyy-MM-dd'T'HH:mm:ssZ' format.
	 * 
	 * @param handler the XML handler to use for generating XML
	 * @param name the XML element name
	 * @param value the value to serialize
	 * @throws SerializerException if unable to serialize property
	 */
	protected void serializeDate(TransformerHandler handler, String name, Date value) throws SerializerException {
		if(value != null) {
			String xmlValue = format.format(value);
			xmlValue = xmlValue.substring(0, xmlValue.length() - 2) + ":" + xmlValue.substring(xmlValue.length() - 2);
			serializeString(handler, name, xmlValue);
		}
	}
	
	/**
	 * Serializes the specified double value into XML using the specified name as XML element name.
	 * 
	 * @param handler the XML handler to use for generating XML
	 * @param name the XML element name
	 * @param value the value to serialize
	 * @throws SerializerException if unable to serialize property
	 */
	protected void serializeDouble(TransformerHandler handler, String name, double value) throws SerializerException {
		serializeString(handler, name, Double.toString(value));
	}
	
	/**
	 * Serializes the specified float value into XML using the specified name as XML element name.
	 * 
	 * @param handler the XML handler to use for generating XML
	 * @param name the XML element name
	 * @param value the value to serialize
	 * @throws SerializerException if unable to serialize property
	 */
	protected void serializeFloat(TransformerHandler handler, String name, float value) throws SerializerException {
		serializeString(handler, name, Float.toString(value));
	}
	
	/**
	 * Serializes the specified integer value into XML using the specified name as XML element name.
	 * 
	 * @param handler the XML handler to use for generating XML
	 * @param name the XML element name
	 * @param value the value to serialize
	 * @throws SerializerException if unable to serialize property
	 */
	protected void serializeInt(TransformerHandler handler, String name, int value) throws SerializerException {
		serializeString(handler, name, Integer.toString(value));
	}
	
	/**
	 * Serializes the specified long value into XML using the specified name as XML element name.
	 * 
	 * @param handler the XML handler to use for generating XML
	 * @param name the XML element name
	 * @param value the value to serialize
	 * @throws SerializerException if unable to serialize property
	 */
	protected void serializeLong(TransformerHandler handler, String name, long value) throws SerializerException {
		serializeString(handler, name, Long.toString(value));
	}
	
	/**
	 * Serializes the specified object into XML using the specified name as XML element name. The object is
	 * converted to string representation using the {@link Object#toString()} method.
	 * 
	 * @param handler the XML handler to use for generating XML
	 * @param name the XML element name
	 * @param value the value to serialize
	 * @throws SerializerException if unable to serialize property
	 */
	protected void serializeObject(TransformerHandler handler, String name, Object value) throws SerializerException {
		if(value != null) {
			serializeString(handler, name, value.toString());
		}
	}
	
	/**
	 * Serializes the specified short value into XML using the specified name as XML element name.
	 * 
	 * @param handler the XML handler to use for generating XML
	 * @param name the XML element name
	 * @param value the value to serialize
	 * @throws SerializerException if unable to serialize property
	 */
	protected void serializeShort(TransformerHandler handler, String name, short value) throws SerializerException {
		serializeString(handler, name, Short.toString(value));
	}
	
	/**
	 * Serializes the specified string value into XML using the specified name as XML element name.
	 * 
	 * @param handler the XML handler to use for generating XML
	 * @param name the XML element name
	 * @param value the value to serialize
	 * @throws SerializerException if unable to serialize property
	 */
	protected void serializeString(TransformerHandler handler, String name, String value) throws SerializerException {
		if(value == null) {
			return;
		}
		
		try {
			name = convertToXmlName(name);
			AttributesImpl attrs = new AttributesImpl();
			handler.startElement("", "", name, attrs);
			handler.characters(value.toCharArray(), 0, value.length());
			handler.endElement("", "", name);
		} catch(SAXException e) {
			throw new SerializerException("Can't serialize " + name + " (" + value + ")", e);
		}
	}
	
    /**
     * Converts the specified method name to a XML tag name for use in serialized XML.
     * 
     * @param name the name to convert
     * @return the converted name
     */
	protected String convertToXmlName(String name) {
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
