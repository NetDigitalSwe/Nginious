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
 * Provides a factory for creating serializers for beans at runtime. Once a serializer has been
 * created it is cached and returned on subsequent calls.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public interface SerializerFactory {

	/**
	 * Creates a serializer for the specified bean class and format found in the specified HTTP accept header. The accept
	 * header is interpreted as defined in section 14.1 of the <a href="http://www.ietf.org/rfc/rfc2616.txt">HTTP 1.1 RFC 2616</a> 
	 * Mime types with higher quality parameters have precedence over mime types with lower quality parameters.
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
	public <T> Serializer<T> createSerializer(Class<T> beanClazz, String acceptHeader) throws SerializerFactoryException;
}
