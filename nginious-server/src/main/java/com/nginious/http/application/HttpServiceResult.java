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

/**
 * Possible results returned by methods in a HTTP service.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 * @see HttpService
 */
public enum HttpServiceResult {
	
	/**
	 * Returned by HTTP service when execution is done.
	 */
	DONE, 
	
	/**
	 * Returned by HTTP service when execution is asynchronous. Informs server that response should not
	 * be commited and connection not closed until another thread writes data to the response.
	 */
	ASYNC,
	
	/**
	 * Returned by HTTP service when a HTTP service chain should continue with the next HTTP service.
	 */
	CONTINUE
}
