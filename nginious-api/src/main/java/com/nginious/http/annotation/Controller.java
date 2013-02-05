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
 * A class marked with this annotation accepts HTTP requests for processing. It is bound to the path specified in the
 * path attribute. Below is an example controller that accepts HTTP requests for the path "/test".
 * 
 * <pre>
 * &#64;Controller(path = "/test")
 * public class Test {
 *   
 *   &#64;Request(methods = { HttpMethod.GET })
 *   public void doGet(HttpRequest request, HttpResponse) throws HttpException, IOException {
 *     response.setStatus(HttpStatus.OK);
 *     response.setContentType("text/plain");
 *     response.setContentLength(6);
 *     PrintWriter writer = response.getWriter();
 *     writer.println("Hello");
 *   }
 * }
 * </pre>
 * 
 * @see com.nginious.http.annotation.Request
 * @see com.nginious.http.HttpRequest
 * @see com.nginious.http.HttpResponse
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
@Target(value = {ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {
	
	/**
	 * URI path for controller. 
	 * 
	 * @return the URI path for controller
	 */
	String path() default "";
	
	/**
	 * URI pattern for controller acting as filters in front of other controllers.
	 * 
	 * @return the URI pattern for HTTP service
	 */
	String pattern() default "";
	
	/**
	 * Filter index
	 * 
	 * @return filter index
	 */
	int index() default 0;
}
