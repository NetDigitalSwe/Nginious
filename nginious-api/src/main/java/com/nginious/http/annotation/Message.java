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

import com.nginious.http.websocket.WebSocketOperation;

/**
 * Maps one or more methods in a controller to web socket operations. Each annotated method is called
 * for the specific type of websocket operation it is annotated for. See {@link com.nginious.http.websocket.WebSocketOperation}
 * for possible operations. Below is an example controller that accepts open, close and text operations.
 *
 * <pre>
 * &#64;Controller(path = "/test")
 * public class Test {
 *
 *   &#64;Message(operations = { WebSocketOperation.OPEN })
 *   public void doOpen(HttpRequest request, HttpResponse response, WebSocketSession session) throws HttpException, IOException {
 *     ...
 *   }
 *
 *   &#64;Message(operations = { WebSocketOperation.TEXT })
 *   public void doText(WebSocketTextMessage message, WebSocketSession session) throws WebSocketException, IOException {
 *     ...
 *   }
 *
 *   &#64;Message(operations = { WebSocketOperation.CLOSE })
 *   public void doClose(WebSocketSession session) throws WebSockewtException, IOException {
 *     ...
 *   }
 * }
 * </pre>
 *
 * @see com.nginious.http.websocket.WebSocketOperation
 * @see com.nginious.http.websocket.WebSocketSession
 * @see com.nginious.http.websocket.WebSocketTextMessage
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Message {

	/**
	 * Returns list of web socket operations supported by the class method annotated with this annotation.
	 * 
	 * @return list of web socket operations
	 */
	WebSocketOperation[] operations() default {};

	/**
	 * Returns content type expected in received message for class method annotated wit this annotation.
	 * @return
	 */
	String contentType() default "";
}
