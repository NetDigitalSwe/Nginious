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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A web socket session statistics entry maintains a number of values for a given minute time period. Below is 
 * the list of values.
 * 
 * <ul>
 * <li>numNewSessions - number of new sessions during time period.</li>
 * <li>numClosedSessions - number of closes sessions during time period.</li>
 * <li>numIncomingMessages - number of received messages in time period.</li>
 * <li>numOutgoingmessages - number of sent messages in time period.</li>
 * <li>sumIncomingBytes - number of received bytes during time period.</li>
 * <li>sumOutgoingBytes - number of sent bytes during time period.</li>
 * </ul>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class WebSocketSessionStatisticsEntry extends StatisticsEntry {
	
	private AtomicInteger numNewSessionsCounter;
	
	private AtomicInteger numClosedSessionsCounter;
	
	private AtomicInteger numIncomingMessagesCounter;
	
	private AtomicInteger numOutgoingMessagesCounter;
	
	private AtomicInteger sumIncomingBytesCounter;
	
	private AtomicInteger sumOutgoingBytesCounter;
	
	private long minuteMillis;
	
	private int numNewSessions;
	
	private int numClosedSessions;
	
	private int numIncomingMessages;
	
	private int numOutgoingMessages;
	
	private int sumIncomingBytes;
	
	private int sumOutgoingBytes;
	
	private boolean current;
	
	/**
	 * Constructs a new web socket session statistics entry starting at the minute
	 * defined by the specified minute milliseconds.
	 * 
	 * @param minuteMillis the start minute time in milliseconds
	 */
	WebSocketSessionStatisticsEntry(long minuteMillis) {
		super();
		this.minuteMillis = minuteMillis;
		this.current = true;
		this.numNewSessionsCounter = new AtomicInteger(0);
		this.numClosedSessionsCounter = new AtomicInteger(0);
		this.numIncomingMessagesCounter = new AtomicInteger(0);
		this.numOutgoingMessagesCounter = new AtomicInteger(0);
		this.sumIncomingBytesCounter = new AtomicInteger(0);
		this.sumOutgoingBytesCounter = new AtomicInteger(0);
	}
	
	/**
	 * Returns the start minute time in milliseconds for this web socket session statistics entry.
	 * 
	 * @return the start minute time in milliseconds.
	 */
	public long getMinuteMillis() {
		return this.minuteMillis;
	}
	
	/**
	 * Returns the number of new sessions established during the time period for this web socket session
	 * statistics entry.
	 * 
	 * @return the number of new sessions
	 */
	public int getNumNewSessions() {
		return this.numNewSessionsCounter == null ? this.numNewSessions : numNewSessionsCounter.get();
	}
	
	/**
	 * Returns the number of closed sessions during the time period for this web socket session statistics
	 * entry.
	 * 
	 * @return the number of closed sessions
	 */
	public int getNumClosedSessions() {
		return this.numClosedSessionsCounter == null ? this.numClosedSessions : numClosedSessionsCounter.get();
	}

	/**
	 * Returns the number of incoming messages during the time period for this web socket session statistics
	 * entry.
	 * 
	 * @return the number of incoming messages
	 */
	public int getNumIncomingMessages() {
		return this.numIncomingMessagesCounter == null ? this.numIncomingMessages : numIncomingMessagesCounter.get();
	}
	
	/**
	 * Returns the number of outgoing messages during the time period for this web socket session statistics
	 * entry.
	 * 
	 * @return the number of outgoing messages
	 */
	public int getNumOutgoingMessages() {
		return this.numOutgoingMessagesCounter == null ? this.numOutgoingMessages : numOutgoingMessagesCounter.get();
	}
	
	/**
	 * Returns the sum of incoming bytes during the time period for this web socket session statistics entry.
	 * 
	 * @return the sum of incoming bytes
	 */
	public int getSumIncomingBytes() {
		return this.sumIncomingBytesCounter == null ? this.sumIncomingBytes : sumIncomingBytesCounter.get();
	}
	
	/**
	 * Returns the sum of outgoing bytes during the time period for this web socket session statistics entry.
	 * 
	 * @return the sum of outgoing bytes
	 */
	public int getSumOutgoingBytes() {
		return this.sumOutgoingBytesCounter == null ? this.sumOutgoingBytes : sumOutgoingBytesCounter.get();
	}
	
	/*
	 * Increments number of new session by one.
	 */
	void addNewSession() {
		numNewSessionsCounter.incrementAndGet();
	}
	
	/*
	 * Increments number of closed session by one.
	 */
	void addClosedSession() {
		numClosedSessionsCounter.incrementAndGet();
	}
	
	/*
	 * Adds num incoming bytes to the total sum of incoming bytes.
	 */
	void addIncomingMessage(int numBytes) {
		numIncomingMessagesCounter.incrementAndGet();
		sumIncomingBytesCounter.addAndGet(numBytes);
	}
	
	/*
	 * Adds num outgoing bytes to the toal sum of outgoing bytes.
	 */
	void addOutgoingMessage(int numBytes) {
		numOutgoingMessagesCounter.incrementAndGet();
		sumOutgoingBytesCounter.addAndGet(numBytes);
	}
	
	protected void add() {
		numNewSessionsCounter.incrementAndGet();		
	}
	
	protected void setCurrent(boolean current) {
		this.current = current;
		
		if(!this.current) {
			set();
		}
	}
	
	void set() {
		this.numNewSessions = numNewSessionsCounter.get();
		this.numClosedSessions = numClosedSessionsCounter.get();
		this.numIncomingMessages = numIncomingMessagesCounter.get();
		this.numOutgoingMessages = numOutgoingMessagesCounter.get();
		this.sumIncomingBytes = sumIncomingBytesCounter.get();
		this.sumOutgoingBytes = sumOutgoingBytesCounter.get();
		
		this.numNewSessionsCounter = null;
		this.numClosedSessionsCounter = null;
		this.numIncomingMessagesCounter = null;
		this.numOutgoingMessagesCounter = null;
		this.sumIncomingBytesCounter = null;
		this.sumOutgoingBytesCounter = null;
	}
}
