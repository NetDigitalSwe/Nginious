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

/**
 * Exception thrown by all classes that create serializers and deserializers to indicate a problem
 * with creating a serializer or deserializer. 
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class SerializerFactoryException extends Exception {
	
	static final long serialVersionUID = 1L;
	
	/**
	 * Constructs a new empty serializer factory exception.
	 */
	public SerializerFactoryException() {
		super();
	}

	/**
	 * Constructs a new serializer factory exception with the specified message and cause.
	 * 
	 * @param message the exception message
	 * @param cause the exception cause
	 */
	public SerializerFactoryException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new serializer factory exception with the specified message.
	 * 
	 * @param message the exception message
	 */
	public SerializerFactoryException(String message) {
		super(message);
	}
	
	/**
	 * Constructs a new serializer factory exception with the specified cause.
	 * 
	 * @param cause the exception cause
	 */
	public SerializerFactoryException(Throwable cause) {
		super(cause);
	}
}
