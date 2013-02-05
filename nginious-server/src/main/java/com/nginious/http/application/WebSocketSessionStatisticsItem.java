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

import java.util.Date;

import com.nginious.http.annotation.Serializable;
import com.nginious.http.stats.WebSocketSessionStatisticsEntry;

/**
 * A REST serializable web socket session statistics item which contains web socket session
 * statistics for one minute.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
@Serializable
public class WebSocketSessionStatisticsItem {
	
	private Date minute;
	
	private int numNewSessions;
	
	private int numClosedSessions;
	
	private int numIncomingMessages;
	
	private int numOutgoingMessages;
	
	private int sumIncomingBytes;
	
	private int sumOutgoingBytes;
	
	/**
	 * Constructs a new uninitialized web socket session statistics item.
	 */
	public WebSocketSessionStatisticsItem() {
		super();
	}
	
	/**
	 * Constructs a new web socket session statistics item with values from the
	 * specified web socket session statistics entry.
	 * 
	 * @param entry the web socket session statistics entry
	 */
	WebSocketSessionStatisticsItem(WebSocketSessionStatisticsEntry entry) {
		this.minute = new Date(entry.getMinuteMillis());
		this.numNewSessions = entry.getNumNewSessions();
		this.numClosedSessions = entry.getNumClosedSessions();
		this.numIncomingMessages = entry.getNumIncomingMessages();
		this.numOutgoingMessages = entry.getNumOutgoingMessages();
		this.sumIncomingBytes = entry.getSumIncomingBytes();
		this.sumOutgoingBytes = entry.getSumOutgoingBytes();
	}
	
	/**
	 * Returns the start minute timestamp for this web socket session statistics item.
	 * 
	 * @return the start minute timestamp
	 */
	public Date getMinute() {
		return this.minute;
	}

	/**
	 * Sets the start minute timestamp to the specified timestamp for this web socket session 
	 * statistics item.
	 * 
	 * @param minute the start minute timestamp
	 */
	public void setMinute(Date minute) {
		this.minute = minute;
	}
	
	/**
	 * Returns the number of new web socket sessions created during the minute period that this
	 * web socket session statistics item represents.
	 * 
	 * @return the number of new web socket sessions
	 */
	public int getNumNewSessions() {
		return this.numNewSessions;
	}
	
	/**
	 * Sets the number of new web socket sessions to the specified number of new sessions.
	 * 
	 * @param numNewSessions the number of new sessions
	 */
	public void setNumNewSessions(int numNewSessions) {
		this.numNewSessions = numNewSessions;
	}
	
	
	/**
	 * Returns the number of closed sessions during the minute period that this web socket
	 * session statistics item represents.
	 * 
	 * @return the number of closed web socket sessions
	 */
	public int getNumClosedSessions() {
		return this.numClosedSessions;
	}

	/**
	 * Sets the number of closed web socket sessions to the specified number of closed sessions.
	 * 
	 * @param numClosedSessions the number of closed sessions
	 */
	public void setNumClosedSessions(int numClosedSessions) {
		this.numClosedSessions = numClosedSessions;
	}

	/**
	 * Returns the number of incoming messages during the minute period that this web socket
	 * session statistics item represents.
	 * 
	 * @return the number of incoming messages
	 */
	public int getNumIncomingMessages() {
		return this.numIncomingMessages;
	}

	/**
	 * Sets the number of incoming messages to the specified number of incoming messages.
	 * 
	 * @param numIncomingMessages the number of incoming messages
	 */
	public void setNumIncomingMessages(int numIncomingMessages) {
		this.numIncomingMessages = numIncomingMessages;
	}
	
	/**
	 * Returns the number of outgoing messages during the minute period that this web socket
	 * session statistics item represents.
	 * 
	 * @return the number of outgoing messages
	 */
	public int getNumOutgoingMessages() {
		return this.numOutgoingMessages;
	}

	/**
	 * Sets the number of outgoing messages to the specified number of outgoing messages.
	 * 
	 * @param numOutgoingMessages the number of outgoing messages
	 */
	public void setNumOutgoingMessages(int numOutgoingMessages) {
		this.numOutgoingMessages = numOutgoingMessages;
	}
	
	/**
	 * Returns the sum of incoming bytes during the minute period that this web socket session
	 * statistics item represents.
	 * 
	 * @return the sum of incoming bytes
	 */
	public int getSumIncomingBytes() {
		return this.sumIncomingBytes;
	}
	
	/**
	 * Sets the sum of incoming bytes to the specified sum of incoming bytes.
	 * 
	 * @param sumIncomingBytes the sum of incoming bytes
	 */
	public void setSumIncomingBytes(int sumIncomingBytes) {
		this.sumIncomingBytes = sumIncomingBytes;
	}

	/**
	 * Returns the sum of outgoing bytes during the minute period that this web socket session
	 * statistics item represents.
	 * 
	 * @return the sum of outgoing bytes
	 */
	public int getSumOutgoingBytes() {
		return this.sumOutgoingBytes;
	}

	/**
	 * Sets the sum of outgoing bytes to the specified sum of incoming bytes.
	 * 
	 * @param sumOutgoingBytes the sum of outgoing bytes
	 */
	public void setSumOutgoingBytes(int sumOutgoingBytes) {
		this.sumOutgoingBytes = sumOutgoingBytes;
	}
}
