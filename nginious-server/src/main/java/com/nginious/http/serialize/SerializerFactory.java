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
import com.nginious.http.server.Header;
import com.nginious.http.server.HeaderException;
import com.nginious.http.server.HeaderParameter;
import com.nginious.http.server.HeaderParameters;

/**
 * Provides a singleton factory for creating serializers for beans at runtime. Once a serializer has been
 * created it is cached and returned on subsequent calls.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class SerializerFactory {
	
	private JsonSerializerCreator jsonCreator;
	
	private XmlSerializerCreator xmlCreator;
	
	/**
	 * Constructs a new serializer factory with the specified class loader
	 * 
	 * @param classLoader the class loader to use for loading created serializer classes
	 */
	public SerializerFactory(ApplicationClassLoader classLoader) {
		super();
		this.jsonCreator = new JsonSerializerCreator(classLoader);
		this.xmlCreator = new XmlSerializerCreator(classLoader);
	}
	
	/**
	 * Creates a serializer for the specified bean class and format found in the specified HTTP accept header. The accept
	 * header is intepreted as defined in section 14.1 of the <a href="http://www.ietf.org/rfc/rfc2616.txt">HTTP 1.1 RFC 2616</a> 
	 * This means that mime types with higher quality parameters have precedence over mime types with lower quality
	 * parameters.
	 * 
	 * <p>
	 * In the following example JSON is preferred over XML
	 * <pre>Accept: text/xml; q=0.8, application/json</pre>
	 * </p>
	 * 
	 * @param <T> type of bean to create serializer for
	 * @param beanClazz the bean class to create a serializer for
	 * @param acceptHeader the HTTP accept header
	 * @return the created serializer
	 * @throws SerializerFactoryException if class is not a bean or accept header is invalid
	 */
	public <T> Serializer<T> createSerializer(Class<T> beanClazz, String acceptHeader) throws SerializerFactoryException {
		if(acceptHeader == null) {
			acceptHeader = "application/json";
		}
		
		try {
			Header header = new Header("Accept", acceptHeader);
			HeaderParameters parameters = header.getParameters();
			HeaderParameter[] sorted = parameters.getSorted();

			Serializer<T> serializer = null;
			
			for(HeaderParameter parameter : sorted) {
				if(parameter.accepts("application/json")) {
					serializer = jsonCreator.create(this, beanClazz);
				} else if(parameter.accepts("text/xml") || parameter.accepts("application/xml")) {
					serializer = xmlCreator.create(this, beanClazz);
				}				
				
				if(serializer != null) {
					return serializer;
				}
			}
			
			return null;
		} catch(HeaderException e) {
			throw new SerializerFactoryException("Can't parse accept header", e);
		}		
	}
	
	/**
	 * Creates a JSON serializer for the specified bean class. If a serializer has already been created it is returned
	 * from internal caches.
	 * 
	 * @param <T> the type of bean to create a serializer for
	 * @param beanClazz the bean to create a serializer for
	 * @return the created serializer
	 * @throws SerializerFactoryException if class is not a bean
	 */
	public <T> JsonSerializer<T> createJsonSerializer(Class<T> beanClazz) throws SerializerFactoryException {
		return jsonCreator.create(this, beanClazz);
	}
	
	/**
	 * Creates a XML serializer for the specified bean class. If a serializer has already been created it is returned
	 * from internal caches.
	 * 
	 * @param <T> the type of bean to create serializer for
	 * @param beanClazz the bean to create serializer for
	 * @return the created serializer
	 * @throws SerializerFactoryException if class is not a bean
	 */
	public <T> XmlSerializer<T> createXmlSerializer(Class<T> beanClazz) throws SerializerFactoryException {
		return xmlCreator.create(this, beanClazz);		
	}
	
	/**
	 * Removes all created and cached serializers that have been loaded with the specified class loader.
	 * 
	 * @param loader the class loader to remove serializers for
	 * @return <code>true</code> if any serializers have been removed, <code>false</code> otherwise
	 */
	public boolean removeLoadedSerializers(ClassLoader loader) {
		boolean removed = false;
		
		if(xmlCreator.removeLoadedSerializers(loader)) {
			removed = true;
		}
		
		if(jsonCreator.removeLoadedSerializers(loader)) {
			removed = true;
		}
		
		return removed;
	}
}
