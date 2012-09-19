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
import java.util.concurrent.atomic.AtomicLong;

import com.nginious.http.HttpStatus;

/**
 * A HTTP request statistics entry maintains a number of values for a given minute time period. Below is 
 * the list of values.
 * 
 * <ul>
 * <li>numSuccessRequests - number of successful requests within time period.</li>
 * <li>numClientErrorRequests - number of requests within time period with client error status codes.</li>
 * <li>numServerErrorRequests - number of requests within time period with server error status codes.</li>
 * <li>sumRequestsDuration - sum of request duration for the time period.</li>
 * <li>sumResponseBytes - sum of response bytes for the time period.</li>
 * </ul>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class HttpRequestStatisticsEntry extends StatisticsEntry {
	
	private AtomicInteger numPendingRequestsCounter;
	
	private AtomicInteger numSuccessRequestsCounter;
	
	private AtomicInteger numClientErrorRequestsCounter;
	
	private AtomicInteger numServerErrorRequestsCounter;
	
	private AtomicLong sumRequestsDurationCounter;
	
	private AtomicInteger sumResponseBytesCounter;
	
	private long minuteMillis;
	
	private int numSuccessRequests;
	
	private int numClientErrorRequests;
	
	private int numServerErrorRequests;
	
	private long sumRequestsDuration;
	
	private int sumResponseBytes;
	
	private boolean current;
	
	/**
	 * Constructs a new HTTP request statistics entry starting at the minute
	 * defined by the specified minute milliseconds.
	 * 
	 * @param minuteMillis the start minute time in milliseconds
	 */
	HttpRequestStatisticsEntry(long minuteMillis) {
		super();
		this.current = true;
		this.minuteMillis = minuteMillis;
		this.numPendingRequestsCounter = new AtomicInteger(0);
		this.numSuccessRequestsCounter = new AtomicInteger(0);
		this.numClientErrorRequestsCounter = new AtomicInteger(0);
		this.numServerErrorRequestsCounter = new AtomicInteger(0);
		this.sumRequestsDurationCounter = new AtomicLong(0);
		this.sumResponseBytesCounter = new AtomicInteger(0);
	}
	
	/**
	 * Returns the start minute time in milliseconds for this HTTP request statistics entry.
	 * 
	 * @return the start minute time in milliseconds.
	 */
	public long getMinuteMillis() {
		return this.minuteMillis;
	}
	
	/**
	 * Returns the number of successful requests during the time period for this HTTP request statistics entry.
	 * 
	 * @return the number of successful requests
	 */
	public int getNumSuccessRequests() {
		return this.numSuccessRequestsCounter == null ? this.numSuccessRequests : numSuccessRequestsCounter.get();
	}
	
	/**
	 * Returns the number of requests with client error status during the time period for this HTTP request 
	 * statistics entry.
	 * 
	 * @return the number of requests with client error status
	 */
	public int getNumClientErrorRequests() {
		return this.numClientErrorRequestsCounter == null ? this.numClientErrorRequests : numClientErrorRequestsCounter.get();
	}

	/**
	 * Returns the number of requests with server error status during the time period for this HTTP request 
	 * statistics entry.
	 * 
	 * @return the number of requests with server error status
	 */
	public int getNumServerErrorRequests() {
		return this.numServerErrorRequestsCounter == null ? this.numServerErrorRequests : numServerErrorRequestsCounter.get();
	}
	
	/**
	 * Returns sum of requests duration for the time period for this HTTP request statistics entry.
	 * 
	 * @return the sum requests duration
	 */
	public long getSumRequestsDuration() {
		return this.sumRequestsDurationCounter == null ? this.sumRequestsDuration : sumRequestsDurationCounter.get();
	}
	
	/**
	 * Returns sum of response bytes for the time period for this HTTP request statistics entry.
	 * 
	 * @return the sum respone bytes
	 */
	public int getSumResponseBytes() {
		return this.sumResponseBytesCounter == null ? this.sumResponseBytes : sumResponseBytesCounter.get();
	}
	
	protected void setCurrent(boolean current) {
		this.current = current;
		
		if(!this.current && numPendingRequestsCounter.get() == 0) {
			set();
		}
	}
	
	protected void add() {
		numPendingRequestsCounter.incrementAndGet();
	}
	
	/**
	 * Updates this HTTP request statistics entry with specified request data including duration
	 * in milliseconds, HTTP status and response bytes.
	 * 
	 * @param durationMillis the request duration in milliseconds
	 * @param status the HTTP status code
	 * @param responseBytes the response bytes
	 */
	public void update(long durationMillis, HttpStatus status, int responseBytes) {
		if(status.isSuccess()) {
			numSuccessRequestsCounter.incrementAndGet();
		} else if(status.isClientError()) {
			numClientErrorRequestsCounter.incrementAndGet();
		} else if(status.isServerError()) {
			numServerErrorRequestsCounter.incrementAndGet();
		}
		
		sumRequestsDurationCounter.addAndGet(durationMillis);
		sumResponseBytesCounter.addAndGet(responseBytes);
		
		if(numPendingRequestsCounter.decrementAndGet() == 0 && !this.current) {
			set();
		}
	}
	
	void set() {
		this.numSuccessRequests = numSuccessRequestsCounter.get();
		this.numClientErrorRequests = numClientErrorRequestsCounter.get();
		this.numServerErrorRequests = numServerErrorRequestsCounter.get();
		this.sumRequestsDuration = sumRequestsDurationCounter.get();
		this.sumResponseBytes = sumResponseBytesCounter.get();
		
		this.numPendingRequestsCounter = null;
		this.numSuccessRequestsCounter = null;
		this.numClientErrorRequestsCounter = null;
		this.numServerErrorRequestsCounter = null;
		this.sumRequestsDurationCounter = null;
		this.sumResponseBytesCounter = null;
	}
}
