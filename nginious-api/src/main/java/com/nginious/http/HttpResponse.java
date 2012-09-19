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

package com.nginious.http;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;


/**
 * Provides functionality for sending a HTTP response to a client.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public interface HttpResponse {	
	
	/**
	 * Returns character encoding for this HTTP response.
	 * 
	 * @return the character encoding
	 */
	public abstract String getCharacterEncoding();
	
	/**
	 * Returns content type as set in the "Content-Type" header for this HTTP response.
	 * 
	 * @return the content type
	 */
	public abstract String getContentType();
	
	/**
	 * Returns  content length as set in the "Content-Length" header for this HTTP response.
	 * 
	 * @return the content length
	 */
	public abstract int getContentLength();
	
	/**
	 * Returns binary output stream for writing binary data to the response body of this HTTP response.
	 * 
	 * @return the output stream
	 */
	public abstract OutputStream getOutputStream();
	
	/**
	 * Returns writer for writing character data to the response body of this HTTP response. The returned
	 * writer uses the set character encoding. If no character encoding has been set prior to calling this
	 * method the default iso-8859-1 character encoding is used.
	 * 
	 * @return the writer
	 */
	public abstract PrintWriter getWriter();
	
	/**
	 * Sets character encoding to the specified encoding. The set character encoding is used in the
	 * "Content-Type" header of the HTTP response as well as when retrieving a writer for writing character
	 * data to the response body.
	 * 
	 * @param encoding the character encoding
	 * @see #getWriter()
	 */
	public abstract void setCharacterEncoding(String encoding);
	
	/**
	 * Sets content length to the specified length for this HTTP response. The content length is used
	 * in the "Content-Length" header of the HTTP response.
	 * 
	 * @param len the content length
	 */
	public abstract void setContentLength(int len);
	
	/**
	 * Sets content type to the specified content type for this HTTP response. The content type is used
	 * in the "Content-TYPE" header of the HTTP response.
	 * 
	 * @param type the content type
	 */
	public abstract void setContentType(String type);
	
	/**
	 * Sets locale to the specified locale for this HTTP response.
	 * 
	 * @param locale the locale
	 */
	public abstract void setLocale(Locale locale);
	
	/**
	 * Returns locale for this HTTP response. Default the HTTP response uses the same locale as the
	 * corresponding HTTP request.
	 * 
	 * @return the locale
	 */
	public abstract Locale getLocale();
	
	/**
	 * Adds the specified HTTP cookie to this HTTP response.
	 * 
	 * @param cookie the HTTP cookie
	 */
	public abstract void addCookie(HttpCookie cookie);
	
	/**
	 * Returns all added HTTP cookies for this HTTP response.
	 * 
	 * @return all HTTP cookies
	 */
	public abstract HttpCookie[] getCookies();
	
	/**
	 * Adds a header with the specified name and value to this HTTP response.
	 * 
	 * @param name the HTTP header name
	 * @param value the HTTP header value
	 */
	public abstract void addHeader(String name, String value);
	
	/**
	 * Returns the first HTTP header value with the specified name from this HTTP response.
	 * 
	 * @param name the HTTP header name
	 * @return the first HTTP header value or <code>null</code> if not set
	 */
	public abstract String getHeader(String name);
	
	/**
	 * Returns names of all HTTP headers for this HTTP response.
	 * 
	 * @return all HTTP header names
	 */
	public abstract String[] getHeaderNames();
	
	/**
	 * Returns all HTTP header values for the specified name for this HTTP response.
	 * 
	 * @param name the HTTP header name
	 * @return all HTTP header values or <code>null</code> if none set
	 */
	public abstract String[] getHeaders(String name);
	
	/**
	 * Returns status for this HTTP response. By default a HTTP response has a status 
	 * of 200 if not set.
	 * 
	 * @return the HTTP response status.
	 */
	public abstract HttpStatus getStatus();
	
	/**
	 * Sets status for this HTTP response to the specified status.
	 * 
	 * @param status the HTTP status
	 */
	public abstract void setStatus(HttpStatus status);
	
	/**
	 * Sets status and message for this HTTP response.
	 * 
	 * @param status the HTTP status
	 * @param message the status message
	 */
	public abstract void setStatus(HttpStatus status, String message);
	
	/**
	 * Completes this HTTP response when executed asynchronously.
	 */
	public abstract void completed();
}
