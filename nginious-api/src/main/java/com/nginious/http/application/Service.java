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

package com.nginious.http.application;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides metadata for a HTTP service used by application configurator to configure the HTTP service.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 * @see com.nginious.http.HttpService
 */
@Target(value = {ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
	
	/**
	 * URI path for HTTP service. 
	 * 
	 * @return the URI path for HTTP service
	 */
	String path() default "";
	
	/**
	 * URI pattern for HTTP services acting as filters in front of other HTTP services.
	 * 
	 * @return the URI pattern for HTTP service
	 */
	String pattern() default "";
	
	/**
	 * Comma separated list of HTTP methods supported by HTTP service.
	 * 
	 * @return comma separated list of supported HTTP services
	 */
	String methods() default "HEAD,GET,POST,PUT,DELETE";
	
	/**
	 * Filter index
	 * 
	 * @return filter index
	 */
	int index() default 0;
}
