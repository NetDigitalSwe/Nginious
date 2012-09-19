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

import java.util.HashMap;

/**
 * Enumeration of HTTP status codes.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public enum HttpStatus {
	
	/**
	 * The client should continue with the request. This status is usually sent by the server in response
	 * to a <code>Expect</code> header with value <code>100-continue</code>.
	 */
	CONTINUE(100, "100 Continue"),
    
	/**
	 * Sent by server to inform client that it understands the request and is willing to comply with the
	 * request to switch protocols.
	 */
	SWITCHING_PROTOCOLS(101, "101 Switching Protocols"),
    
	/**
	 * The request has succeeded.
	 */
	OK(200, "200 OK"),
	
	/**
	 * The request succeeded and resulted in a new resource being created. 
	 */
	CREATED(201, "201 Created"),

	/**
	 * The request has been accepted for processing but the processing has not been completed.
	 */
	ACCEPTED(202, "202 Accepted"),
	
	/**
	 * The returned meta information in the entity header is no the definitive set as available
	 * from the origin server.
	 */
	NON_AUTHORITATIVE_INFORMATION(203, "203 Non Authoritative Information"),

	/**
	 * The server has fulfilled the request but there is no content to return.
	 */
	NO_CONTENT(204, "204 No Content"),
	
	/**
	 * The server has fulfilled the request but the client should reset the document which
	 * caused the request to be sent.
	 */
	RESET_CONTENT(205, "205 Reset Content"),
    
	/**
	 * The server has fulfilled the request for partial content.
	 */
	PARTIAL_CONTENT(206, "206 Partial Content"),
    
	/**
	 * The requested resource corresponds to any one of a set of representations, each with its own 
	 * specific location, and agent driven negotiation information.
	 */
	MULTIPLE_CHOICES(300, "300 Multiple Choices"),
	
	/**
	 * The requested resource has been assigned a new permanent URI and any future references to this 
	 * resource SHOULD use one of the returned URIs.
	 */
	MOVED_PERMANENTLY(301, "301 Moved Permanently"),
	
	/**
	 * The requested resource resides temporarily under a different URI.
	 */
	FOUND(302, "302 Found"),
	
	/**
	 * The requested resource resides temporarily under a different URI.
	 */
	MOVED_TEMPORARILY(302, "302 Moved Temporarily"),
    
	/**
	 * The response to the request can be found under a different URI and should be retrieved using a GET 
	 * method on that resource.
	 */
	SEE_OTHER(303, "303 See Other"),
    
	/**
	 * If the client has performed a conditional GET request and access is allowed, but the document has not 
	 * been modified, the server should respond with this status code.
	 */
	NOT_MODIFIED(304, "304 Not Modified"),
    
	/**
	 * The requested resource MUST be accessed through the proxy given by the Location field. The Location field
	 * gives the URI of the proxy.
	 */
	USE_PROXY(305, "305 Use Proxy"),
	
	/**
	 * The requested resource resides temporarily under a different URI.
	 */
	TEMPORARY_REDIRECT(307, "307 Temporary Redirect"),
    
	/**
	 * The request could not be understood by the server due to malformed syntax.
	 */
	BAD_REQUEST(400, "400 Bad Request"),
	
	/**
	 * The request requires user authentication.
	 */
	UNAUTHORIZED(401, "401 Unauthorized"),
    
	/**
	 * The code is reserved for future use.
	 */
	PAYMENT_REQUIRED(402, "402 Payment Required"),
    
	/**
	 * The server understood the request, but is refusing to fulfill it.
	 */
	FORBIDDEN(403, "403 Forbidden"),

	/**
	 * The server has not found anything matching the request URI.
	 */
	NOT_FOUND(404, "404 Not Found"),
    
	/**
	 * The method specified in the request is not allowed for the resource identified by the
	 * request URI.
	 */
	METHOD_NOT_ALLOWED(405, "405 Not Allowed"),
    
	/**
	 * The resource identified by the request is only capable of generating response entities which 
	 * have content characteristics not acceptable according to the accept headers sent in the request.
	 */
	NOT_ACCEPTABLE(406, "406 Not Acceptable"),
	
	/**
	 * This code is similar to 401 (Unauthorized), but indicates that the client must first authenticate 
	 * itself with the proxy.
	 */
	PROXY_AUTHENTICATION_REQUIRED(407, "407 Authentication Required"),
    
	/**
	 * The client did not produce a request within the time that the server was prepared to wait.
	 */
	REQUEST_TIMEOUT(408, "408 Request Timeout"),
    
	/**
	 * The request could not be completed due to a conflict with the current state of the resource.
	 */
	CONFLICT(409, "409 Conflict"),
	
	/**
	 * The requested resource is no longer available at the server and no forwarding address is known.
	 */
	GONE(410, "410 Gone"),
	
	/**
	 * The server refuses to accept the request without a defined content length.
	 */
	LENGTH_REQUIRED(411, "411 Length Required"),
	
	/**
	 * The precondition given in one or more of the request header fields evaluated to false when it was 
	 * tested on the server.
	 */
	PRECONDITION_FAILED(412, "412 Precondition Failed"),
    
	/**
	 * The server is refusing to process a request because the request entity is larger than the server 
	 * is willing or able to process.
	 */
	REQUEST_ENTITY_TOO_LARGE(413, "413 Request Entity Too Large"),
    
	/**
	 * The server is refusing to service the request because the request URI is longer than the server 
	 * is willing to interpret.
	 */
	REQUEST_URI_TOO_LONG(414, "414 Request URI Too Large"),
	
	/**
	 * The server is refusing to service the request because the entity of the request is in a format 
	 * not supported by the requested resource for the requested method.
	 */
	UNSUPPORTED_MEDIA_TYPE(415, "415 Unsupported Media Type"),
	
	/**
	 * A server SHOULD return a response with this status code if a request included a Range request header 
	 * field and none of the range-specifier values in this field overlap the current extent of the selected
	 * resource.
	 */
	REQUESTED_RANGE_NOT_SATISFIABLE(416, "416 Range Not Satisfiable"),
    
	/**
	 * The expectation given in an Expect request header field could not be met by this server.
	 */
	EXPECTATION_FAILED(417, "417 Expectation Failed"),
	
	/**
	 * Reserved for future use.
	 */
	UPGRADE_REQUIRED(426, "426 Upgrade Required"),
	
	/**
	 * The server encountered an unexpected condition which prevented it from fulfilling the request.
	 */
	INTERNAL_SERVER_ERROR(500, "500 Internal Server Error"),
	
	/**
	 * The server does not support the functionality required to fulfill the request.
	 */
	NOT_IMPLEMENTED(501, "501 Not Implemented"),
	
	/**
	 * The server, while acting as a gateway or proxy, received an invalid response from the upstream 
	 * server it accessed in attempting to fulfill the request.
	 */
	BAD_GATEWAY(502, "502 Bad Gateway"),
	
	/**
	 * The server is currently unable to handle the request due to a temporary overloading or maintenance 
	 * of the server.
	 */
	SERVICE_UNAVAILABLE(503, "503 Service Unavailable"),
    
	/**
	 * The server, while acting as a gateway or proxy, did not receive a timely response from the upstream 
	 * server specified by the URI
	 */
	GATEWAY_TIMEOUT(504, "504 Gateway Timeout"),
	
	/**
	 * The server does not support, or refuses to support, the HTTP protocol version that was used in the 
	 * request message.
	 */
	HTTP_VERSION_NOT_SUPPORTED(505, "505 HTTP Version Not Supported");
	
	private static HashMap<Integer, HttpStatus> codeLookup = new HashMap<Integer, HttpStatus>();
	
	static {
		codeLookup.put(CONTINUE.statusCode, CONTINUE);
		codeLookup.put(SWITCHING_PROTOCOLS.statusCode, SWITCHING_PROTOCOLS);
		codeLookup.put(OK.statusCode, OK);
		codeLookup.put(CREATED.statusCode, CREATED);
		codeLookup.put(ACCEPTED.statusCode, ACCEPTED);
		codeLookup.put(NON_AUTHORITATIVE_INFORMATION.statusCode, NON_AUTHORITATIVE_INFORMATION);
		codeLookup.put(NO_CONTENT.statusCode, NO_CONTENT);
		codeLookup.put(RESET_CONTENT.statusCode, RESET_CONTENT);
		codeLookup.put(PARTIAL_CONTENT.statusCode, PARTIAL_CONTENT);
		codeLookup.put(MULTIPLE_CHOICES.statusCode, MULTIPLE_CHOICES);
		codeLookup.put(MOVED_PERMANENTLY.statusCode, MOVED_PERMANENTLY);
		codeLookup.put(FOUND.statusCode, FOUND);
		codeLookup.put(MOVED_TEMPORARILY.statusCode, MOVED_TEMPORARILY);
		codeLookup.put(SEE_OTHER.statusCode, SEE_OTHER);
		codeLookup.put(NOT_MODIFIED.statusCode, NOT_MODIFIED);
		codeLookup.put(USE_PROXY.statusCode, USE_PROXY);
		codeLookup.put(TEMPORARY_REDIRECT.statusCode, TEMPORARY_REDIRECT);
		codeLookup.put(BAD_REQUEST.statusCode, BAD_REQUEST);
		codeLookup.put(UNAUTHORIZED.statusCode, UNAUTHORIZED);
		codeLookup.put(PAYMENT_REQUIRED.statusCode, PAYMENT_REQUIRED);
		codeLookup.put(FORBIDDEN.statusCode, FORBIDDEN);
		codeLookup.put(NOT_FOUND.statusCode, NOT_FOUND);
		codeLookup.put(METHOD_NOT_ALLOWED.statusCode, METHOD_NOT_ALLOWED);
		codeLookup.put(NOT_ACCEPTABLE.statusCode, NOT_ACCEPTABLE);
		codeLookup.put(PROXY_AUTHENTICATION_REQUIRED.statusCode, PROXY_AUTHENTICATION_REQUIRED);
		codeLookup.put(REQUEST_TIMEOUT.statusCode, REQUEST_TIMEOUT);
		codeLookup.put(CONFLICT.statusCode, CONFLICT);
		codeLookup.put(GONE.statusCode, GONE);
		codeLookup.put(LENGTH_REQUIRED.statusCode, LENGTH_REQUIRED);
		codeLookup.put(PRECONDITION_FAILED.statusCode, PRECONDITION_FAILED);
		codeLookup.put(REQUEST_ENTITY_TOO_LARGE.statusCode, REQUEST_ENTITY_TOO_LARGE);
		codeLookup.put(REQUEST_URI_TOO_LONG.statusCode, REQUEST_URI_TOO_LONG);
		codeLookup.put(UNSUPPORTED_MEDIA_TYPE.statusCode, UNSUPPORTED_MEDIA_TYPE);
		codeLookup.put(REQUESTED_RANGE_NOT_SATISFIABLE.statusCode, REQUESTED_RANGE_NOT_SATISFIABLE);
		codeLookup.put(EXPECTATION_FAILED.statusCode, EXPECTATION_FAILED);
		codeLookup.put(UPGRADE_REQUIRED.statusCode, UPGRADE_REQUIRED);
		codeLookup.put(INTERNAL_SERVER_ERROR.statusCode, INTERNAL_SERVER_ERROR);
		codeLookup.put(NOT_IMPLEMENTED.statusCode, NOT_IMPLEMENTED);
		codeLookup.put(BAD_GATEWAY.statusCode, BAD_GATEWAY);
		codeLookup.put(SERVICE_UNAVAILABLE.statusCode, SERVICE_UNAVAILABLE);
		codeLookup.put(GATEWAY_TIMEOUT.statusCode, GATEWAY_TIMEOUT);
		codeLookup.put(HTTP_VERSION_NOT_SUPPORTED.statusCode, HTTP_VERSION_NOT_SUPPORTED);
	}
	
	private int statusCode;
	
	private String statusMessage;
	
	private HttpStatus(int statusCode, String statusMessage) {
		this.statusCode = statusCode;
		this.statusMessage = statusMessage;
	}
	
	/**
	 * Returns the HTTP status matching the specified status code.
	 * 
	 * @param statusCode the status code
	 * @return the HTTP status or <code>null</code> if no HTTP status found
	 */
	public static HttpStatus getHttpStatus(int statusCode) {
		return codeLookup.get(statusCode);
	}
	
	/**
	 * Returns HTTP status code for this HTTP status.
	 * 
	 * @return the HTTP status code
	 */
	public int getStatusCode() {
		return this.statusCode;
	}
	
	/**
	 * Returns HTTP status message for this HTTP status.
	 * 
	 * @return the HTTP status message
	 */
	public String getMessage() {
		return this.statusMessage;
	}
	
	/**
	 * Returns HTTP status line for this HTTP status.
	 * 
	 * @return the HTTP status line
	 */
	public String getResponse() {
		return this.statusMessage;
	}
	
	/**
	 * Returns whether or not this HTTP status us a success status.
	 * 
	 * @return <code>true</code> if this HTTP status is a success status, <code>false</code> otherwise
	 */
	public boolean isSuccess() {
		return this.statusCode >= OK.statusCode && this.statusCode < BAD_REQUEST.statusCode;
	}
	
	/**
	 * Returns whether or not this HTTP status is a client error status.
	 * 
	 * @return <code>true</code> if this HTTP status is a client error status, <code>false</code> otherwise
	 */
	public boolean isClientError() {
		return this.statusCode >= BAD_REQUEST.statusCode && this.statusCode < INTERNAL_SERVER_ERROR.statusCode;
	}
	
	/**
	 * Returns whether or not this HTTP status is a server error status.
	 * 
	 * @return <code>true</code>, if this HTTP status us a server error status, <code>false</code> otherwise
	 */
	public boolean isServerError() {
		return this.statusCode >= INTERNAL_SERVER_ERROR.statusCode && this.statusCode <= HTTP_VERSION_NOT_SUPPORTED.statusCode;
	}
}
