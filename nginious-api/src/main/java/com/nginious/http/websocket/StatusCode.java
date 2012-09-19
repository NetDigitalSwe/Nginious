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

package com.nginious.http.websocket;

import java.util.HashMap;

/**
 * An enumeration of status codes for use in close frames where an endpoint can
 * indicate a reason for closure.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public enum StatusCode {
	
	/**
	 * Normal closure.
	 */
	NORMAL_CLOSURE(1000),
	
	/**
	 * The endpoint is going away such as server is shutting down.
	 */
	GOING_AWAY(1001),
	
	/**
	 * The endpoint is closing the connection because of a protocol error.
	 */
	PROTOCOL_ERROR(1002),
	
	/**
	 * The endpoint is closing the connection because it received data it
	 * doesn't understand.
	 */
	UNSUPPORTED_DATA(1003),
	
	/**
	 * Reserved, must not be used.
	 */
	RESERVED_1(1004),
	
	/**
	 * Reserved, must not be used.
	 */
	NO_STATUS_RECEIVED(1005),
	
	/**
	 * Reserved, must not be used.
	 */
	ABNORMAL_CLOSURE(1006),
	
	/**
	 * The endpoint is closing the connection because it received data that was not
	 * consistent with the type of message.
	 */
	INVALID_FRAME_PAYLOAD_DATA(1007),
	
	/**
	 * The endpoint is closing the connection because it received a message which
	 * violates its policy.
	 */
	POLICY_VIOLATION(1008),
	
	/**
	 * The endpoint is closing the connection because it received a message which
	 * is to big to process.
	 */
	MESSAGE_TOO_BIG(1009),
	
	/**
	 * The endpoint is closing the connection because the server did not respond
	 * with a mandatory extension.
	 */
	MANDATORY_EXTENSION(1010),
	
	/**
	 * The server is closing the connection because of an error.
	 */
	INTERNAL_SERVER_ERROR(1011),
	
	/**
	 * Reserved, must not be used.
	 */
	TLS_HANDSHAKE(1015);
	
	private static HashMap<Integer, StatusCode> lookup = new HashMap<Integer, StatusCode>();
	
	static {
		lookup.put(NORMAL_CLOSURE.value, NORMAL_CLOSURE);
		lookup.put(GOING_AWAY.value, GOING_AWAY);
		lookup.put(PROTOCOL_ERROR.value, PROTOCOL_ERROR);
		lookup.put(UNSUPPORTED_DATA.value, UNSUPPORTED_DATA);
		lookup.put(RESERVED_1.value, RESERVED_1);
		lookup.put(NO_STATUS_RECEIVED.value, NO_STATUS_RECEIVED);
		lookup.put(ABNORMAL_CLOSURE.value, ABNORMAL_CLOSURE);
		lookup.put(INVALID_FRAME_PAYLOAD_DATA.value, INVALID_FRAME_PAYLOAD_DATA);
		lookup.put(POLICY_VIOLATION.value, POLICY_VIOLATION);
		lookup.put(MESSAGE_TOO_BIG.value, MESSAGE_TOO_BIG);
		lookup.put(MANDATORY_EXTENSION.value, MANDATORY_EXTENSION);
		lookup.put(INTERNAL_SERVER_ERROR.value, INTERNAL_SERVER_ERROR);
		lookup.put(TLS_HANDSHAKE.value, TLS_HANDSHAKE);
	}
	
	public int value;
	
	/**
	 * Constructs a new status code with the specified value.
	 * 
	 * @param value the status code value
	 */
	private StatusCode(int value) {
		this.value = value;
	}
	
	/**
	 * Returns status code for the specified status code value.
	 * 
	 * @param status the status code value
	 * @return the status code or <code>null</code> if status code value is invalid
	 */
	public static StatusCode get(int status) {
		return lookup.get(status);
	}
}
