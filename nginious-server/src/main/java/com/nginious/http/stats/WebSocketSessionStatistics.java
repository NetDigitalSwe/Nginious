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

package com.nginious.http.stats;

/**
 * Maintains web socket session statistics from server start until current time. Statistics
 * is segmented into entries of one minute each.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 * @see WebSocketSessionStatisticsEntry
 */
public class WebSocketSessionStatistics extends Statistics<WebSocketSessionStatisticsEntry> {
	
	/**
	 * Constructs a new web socket session statistics.
	 */
	public WebSocketSessionStatistics() {
		super();
	}
	
	/**
	 * Increments the new session counter for the current minute entry by one.
	 */
	public void addNewSession() {
		WebSocketSessionStatisticsEntry entry = getEntry();
		entry.addNewSession();
	}
	
	/**
	 * Increments the closed session counter fo the current minute entry by one.
	 */
	public void addClosedSession() {
		WebSocketSessionStatisticsEntry entry = getEntry();
		entry.addClosedSession();
	}
	
	/**
	 * Adds the specified number of bytes to the sum of incoming bytes and increments
	 * number of incoming messages by one.
	 * 
	 * @param numBytes the number of bytes received
	 */
	public void addIncomingMessage(int numBytes) {
		WebSocketSessionStatisticsEntry entry = getEntry();
		entry.addIncomingMessage(numBytes);
	}
	
	/**
	 * Adds the specified number of bytes tot the sum of outgoing bytrs and increments
	 * number of outgoing messages by one.
	 * 
	 * @param numBytes the number of bytes sent
	 */
	public void addOutgoingMessage(int numBytes) {
		WebSocketSessionStatisticsEntry entry = getEntry();
		entry.addOutgoingMessage(numBytes);
	}
	
	protected WebSocketSessionStatisticsEntry createEntry(long minuteMillis) {
		return new WebSocketSessionStatisticsEntry(minuteMillis);
	}

	protected WebSocketSessionStatisticsEntry[] createArray(int size) {
		return new WebSocketSessionStatisticsEntry[size];
	}

	protected WebSocketSessionStatisticsEntry[][] create2Array(int size) {
		return new WebSocketSessionStatisticsEntry[size][];
	}
}
