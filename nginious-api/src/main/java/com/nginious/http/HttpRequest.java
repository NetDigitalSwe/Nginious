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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Locale;

import com.nginious.http.upload.FilePart;
import com.nginious.http.upload.UploadTracker;

/**
 * Contains information for a HTTP request.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public interface HttpRequest {
	
	/**
	 * Returns first header value with the specified header name from this HTTP request.
	 * 
	 * @param name the header name
	 * @return the first header value or <code>null</code> if header not part of request
	 */
	public String getHeader(String name);
	
	/**
	 * Returns names of all headers in this HTTP request.
	 * 
	 * @return all header names
	 */
	public String[] getHeaderNames();
	
	/**
	 * Returns cookie with the specified name from this HTTP request.
	 * 
	 * @param name cookie name
	 * @return cookie or <code>null</code> if no cookie with provided name found
	 */
	public HttpCookie getCookie(String name);
	
	/**
	 * Returns all cookies part of this HTTP request.
	 * 
	 * @return all cookies in this HTT request
	 */
	public HttpCookie[] getCookies();
	
	/**
	 * Returns HTTP protocol version for this HTTP request.
	 * 
	 * @return the HTTP protocol version
	 */
	public String getVersion();
	
	/**
	 * Returns HTTP method for this HTTP request.
	 * 
	 * @return the HTTP method
	 */
	public HttpMethod getMethod();
	
	/**
	 * Returns path part of request URI.
	 * 
	 * @return URI path
	 */
	public String getPath();
	
	/**
	 * Returns query part of request URI
	 * 
	 * @return URI query
	 */
	public String getQueryString();
	
	/**
	 * Returns character encoding for this HTTP request. The character encoding is retrieved
	 * from the "Content-Type" header if present otherwise the default "iso-8859-1" encoding
	 * is returned.
	 * 
	 * @return the character encoding or iso-8859-1 if not found
	 */
	public String getCharacterEncoding();
	
	/**
	 * Returns content length of body in this HTTP request.
	 * 
	 * @return the content length
	 */
	public int getContentLength();
	
	/**
	 * Returns content type as set in the "Content-Type" header in this HTTP request.
	 * 
	 * @return the content type or <code>null</code> if no content type header found
	 */
	public String getContentType();
	
	/**
	 * Returns the body of this HTTP request as a binary input stream.
	 * 
	 * @return the input stream to read the body
	 * @throws IOException if an I/O error occurs while creating the input stream
	 */
	public InputStream getInputStream() throws IOException;
	
	/**
	 * Returns the body of this HTTP request as a reader for reading character data.
	 * 
	 * @return the reader
	 * @throws IOException if anI/O error occurs while creating reader
	 */
	public BufferedReader getReader() throws IOException;
	
	/**
	 * Returns preferred locale for this HTTP request. The locale is extracted from the
	 * request header "Accept-Language" if available. If no header is found the platforms
	 * default locale is returned instead.
	 * 
	 * @return request locale or platforms default locale if not found
	 */
	public Locale getLocale();
	
	/**
	 * Returns first query parameter value with the specified name from this HTTP request.
	 * 
	 * @param name the query parameter name
	 * @return the query parameter value or <code>null</code>
	 */
	public String getParameter(String name);
	
	/**
	 * Returns all query parameter values with the specified name from this HTTP request.
	 * 
	 * @param name the query parameter name
	 * @return all query parameter values or <code>null</code>
	 */
	public String[] getParameterValues(String name);
	
	/**
	 * Returns all query parameter names from this HTTP request.
	 * 
	 * @return all query parameter names
	 */
	public String[] getParameterNames();
	
	/**
	 * Sets attribute with the specified name and value for this HTTP request.
	 * 
	 * @param name the attribute name
	 * @param value the attribute value
	 */
	public void setAttribute(String name, Object value);
	
	/**
	 * Returns value of attribute with the specified name.
	 * 
	 * @param name the attribute name
	 * @return attribute value or <code>null</code> if not set
	 */
	public Object getAttribute(String name);
	
	/**
	 * Removes attribute with the specified name from this HTTP request.
	 * 
	 * @param name the attribute name
	 * @return attribute value or <code>null</code> if not set
	 */
	public Object removeAttribute(String name);
	
	/**
	 * Prepares for tracking a file upload. This method must be called before starting a
	 * file upload. 
	 */
	public void prepareUploadTracker();
	
	/**
	 * Returns the upload tracker associated with this HTTP request.
	 * 
	 * @return the upload tracker
	 */
	public UploadTracker getUploadTracker();
	
	/**
	 * Returns file part with the specified name from this HTTP request. File parts can be part
	 * of a multipart request.
	 * 
	 * @param name the file part name
	 * @return the file part
	 */
	public FilePart getFile(String name);
	
	/**
	 * Returns all file parts from this HTTP request. File Parts can be part of a multipart
	 * request.
	 * 
	 * @return all file parts
	 */
	public Collection<FilePart> getFiles();
	
	/**
	 * Returns name and version of the protocol for this HTTP request.
	 * 
	 * @return protocol name and version
	 */
	public String getProtocol();
	
	/**
	 * Returns scheme for this HTTP request.
	 * 
	 * @return the scheme
	 */
	public String getScheme();
	
	/**
	 * Returns IP address of the client that sent this HTTP request.
	 * 
	 * @return IP address 
	 */
	public String getRemoteAddress();
	
	/**
	 * Returns host address of the client that sent this HTTP request.
	 * 
	 * @return the host address
	 */
	public String getRemoteHost();
	
	/**
	 * Returns port for the client that sent this HTTP request.
	 * 
	 * @return remote port
	 */
	public int getRemotePort();
	
	/**
	 * Returns HTTP session associated with this HTTP request. If no session exists a new one is
	 * created.
	 * 
	 * @return the session
	 */
	public HttpSession getSession();
	
	/**
	 * Returns HTTP session associated with this HTTP request.
	 * 
	 * @param create whether or not session should be created if one doesn't exist
	 * @return the session
	 */
	public HttpSession getSession(boolean create);
	
	/**
	 * Dispatches this HTTP request to the specified URI path.
	 * 
	 * @param path the URI path to dispatch request to
	 * @throws HttpException if a HTTP error occurs while dispatching request
	 * @throws IOException if an I/O error occurs
	 */
	public void dispatch(String path) throws HttpException, IOException;
}
