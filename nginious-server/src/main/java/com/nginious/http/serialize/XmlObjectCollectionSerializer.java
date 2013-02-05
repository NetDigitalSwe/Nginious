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

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.nginious.http.serialize.SerializerException;

/**
 * Serializes a collection of opaque objects into XML format. Each object element is converted into
 * string representation.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class XmlObjectCollectionSerializer {
	
	private String name;
	
	/**
	 * Constructs a new XML object collection serializer.
	 */
	public XmlObjectCollectionSerializer(String name) {
		super();
		this.name = convertToXmlName(name);
	}
	
	/**
	 * Serializes the specified collection and writes it using the specified writer.
	 * 
	 * @param writer the given writer
	 * @param items the given collection of bean elements to serialize
	 * @throws SerializerException if unable to serialize collection
	 */
	public void serialize(PrintWriter writer, Collection<?> items) throws SerializerException {
		TransformerHandler handler = createTransformerHandler(writer);
		serialize(handler, items);
	}
	
	/**
	 * Sets up and initializes XML handlers for writing XML to the specified writer.
	 * 
	 * @param writer the writer where generated XML should be written
	 * @return the XML handler
	 * @throws SerializerException if unable to setup XML handler
	 */
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
	 * Serializes the specified collection and creates XML using the specified handler.
	 * 
	 * @param handler the XML handler
	 * @param items the given collection of bean elements to serialize
	 * @throws SerializerException if unable to serialize collection
	 */
	public void serialize(TransformerHandler handler, Collection<?> items) throws SerializerException {
		if(items == null) {
			return;
		}
		
		String value = null;
		
		try {
			AttributesImpl attrs = new AttributesImpl();		
			handler.startElement("", "", this.name, attrs);
			
			for(Object item : items) {
				value = item.toString();
				
				handler.startElement("", "", "value", attrs);
				handler.characters(value.toCharArray(), 0, value.length());
				handler.endElement("", "", "value");
			}
			
			handler.endElement("", "", this.name);
		} catch(SAXException e) {
			throw new SerializerException("Can't serialize value" + " (" + value + ")", e);
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
