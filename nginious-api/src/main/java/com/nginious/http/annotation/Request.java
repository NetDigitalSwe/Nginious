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

import com.nginious.http.HttpMethod;

/**
 * Maps one or more methods in a controller to HTTP requests. The method is called for the given HTTP method
 * and path bound to the corresponding {@link Controller}. Below is an example method signature for a method
 * that accepts HTTP GET requests.
 * 
 * <pre>
 * &#64;Request(methods = { HttpMethod.GET })
 * public void executeGet(HttpRequest request, HttpResponse response) throws HttpException, IOException {
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
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Request {
	
	/**
	 * Returns list of HTTP methods supported by the class method annotated with this annotation.
	 * 
	 * @return list of HTTP methods
	 */
	HttpMethod[] methods() default {};
	
	/**
	 * Returns whether or not method executes request asynchronously.
	 * 
	 * @return <code>true</code> if request is handled asynchronously, <code>false</code> otherwise
	 */
	boolean async() default false;
}
