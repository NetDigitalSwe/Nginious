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

package com.nginious.http.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides information to the serializer and deserializer mechanism which properties should be serialized and
 * deserialized for a bean. Place annotations on properties set methods in beans.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 * 
 */
@Target(value = {ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Serializable {
	
	/**
	 * Returns whether or not property should be serialized.
	 * 
	 * @return <code>true</code> if property should be serialized, <code>false</code> otherwise
	 */
	boolean serialize() default true;
	
	/**
	 * Returns whether or not property should be deserialized.
	 * 
	 * @return <code>true</code> if property should be serialized, <code>false</code> otherwise
	 */
	boolean deserialize() default true;
	
	/**
	 * Returns comma separated list of formats for which property should be serialized and deserialized.
	 * 
	 * @return comma separated list of formats
	 */
	String types() default "json,xml,query";
}
