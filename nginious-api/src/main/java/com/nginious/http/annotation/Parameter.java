/**
 * Copyright 2012, 2013 NetDigital Sweden AB
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
 * Maps a controller method parameter to a HTTP request parameter. The method parameter is filled with the value
 * from the HTTP request. Below is an example method signature for a method that accepts HTTP GET requests.
 * 
 * <pre>
 * &#64;Request(methods = { HttpMethod.GET })
 * public void executeGet(@Parameter(name = "name") String name) throws HttpException, IOException {
 *   response.setStatus(HttpStatus.OK);
 *   response.setContentType("text/plain");
 *   response.setContentLength(6);
 *   PrintWriter writer = response.getWriter();
 *   writer.println("Hello");
 * }
 * </pre>
 * 
 * @see com.nginious.http.annotation.Controller
 * @see com.nginious.http.HttpRequest
 * @see com.nginious.http.HttpResponse
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
@Target(value = {ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Parameter {
	
	/**
	 * Returns name of HTTP request parameter to map to controller method parameter.
	 * 
	 * @return name of HTTP request parameter
	 */
	String name() default "";
}
