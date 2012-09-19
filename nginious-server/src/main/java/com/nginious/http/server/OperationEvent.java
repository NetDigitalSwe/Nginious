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

package com.nginious.http.server;

/**
 * Operation events are queued by connection with their server to request operations on
 * the connections socket channel. Possible operations are:
 *
 * <ul>
 * <li>{@link Type#OPERATION_READ} - request server to inform connection once data is available for reading.</li>
 * <li>{@link Type#OPERATION_WRITE}} - request server to inform connection once channel is ready for writing.</li>
 * <li>{@link Type#OPERATION_CLOSE} - request server to tear down connection and close socket channel.</li>
 * </ul>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
class OperationEvent {
	
	enum Type {
		
		OPERATION_READ,
		
		OPERATION_WRITE,
		
		OPERATION_CLOSE;
	}
	
	private Connection conn;
	
	private Type operation;
	
	/**
	 * Constructs a new operation event for the specified connection and with
	 * the specified operation type. Operation type is one of {@link Type#OPERATION_READ},
	 * {@link Type#OPERATION_WRITE} or {@link Type#OPERATION_CLOSE}.
	 * 
	 * @param conn the connection
	 * @param operation the operation
	 */
	OperationEvent(Connection conn, Type operation) {
		this.conn = conn;
		this.operation = operation;
	}
	
	/**
	 * Returns connection for this operation event.
	 * 
	 * @return the connection
	 */
	Connection getConnection() {
		return this.conn;
	}
	
	/**
	 * Returns operation for this operation event
	 * 
	 * @return the operation
	 */
	Type getOperation() {
		return this.operation;
	}
	
	/**
	 * Creates a text description of this operation event.
	 * 
	 * @return a text description of this event
	 */
	public String toString() {
		return operation.toString();
	}
}
