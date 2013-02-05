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
import com.nginious.http.stats.HttpRequestStatisticsEntry;

/**
 * A REST serializable HTTP request statistics item which contains HTTP request
 * statistics for one minute.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
@Serializable
public class HttpRequestStatisticsItem {
	
	private Date minute;
	
	private int numSuccessRequests;
	
	private int numClientErrorRequests;
	
	private int numServerErrorRequests;
	
	private long sumRequestsDuration;
	
	private int sumResponseBytes;
	
	/**
	 * Constructs a new HTTP request statistics item.
	 */
	public HttpRequestStatisticsItem() {
		super();
	}
	
	/**
	 * Constructs a new HTTP request statistics item with values from the
	 * specified HTTP request statistics entry.
	 * 
	 * @param entry the HTTP request statistics entry
	 */	
	HttpRequestStatisticsItem(HttpRequestStatisticsEntry entry) {
		this.minute = new Date(entry.getMinuteMillis());
		this.numSuccessRequests = entry.getNumSuccessRequests();
		this.numClientErrorRequests = entry.getNumClientErrorRequests();
		this.numServerErrorRequests = entry.getNumServerErrorRequests();
		this.sumRequestsDuration = entry.getSumRequestsDuration();
		this.sumResponseBytes = entry.getSumResponseBytes();
	}
	
	/**
	 * Returns the start minute timestamp for this HTTP request statistics item.
	 * 
	 * @return the start minute timestamp
	 */
	public Date getMinute() {
		return this.minute;
	}
	
	/**
	 * Sets the start minute timestamp to the specified timestamp for this HTTP request
	 * statistics item.
	 * 
	 * @param minute the start minute timestamp
	 */
	public void setMintue(Date minute) {
		this.minute = minute;
	}
	
	/**
	 * Returns the number of successful HTTP requests during the minute period that this HTTP
	 * request statistics item represents.
	 * 
	 * @return the number of successful HTTP requests
	 */	
	public int getNumSuccessRequests() {
		return this.numSuccessRequests;
	}

	/**
	 * Sets the number of successful HTTP requests to the specified number of requests.
	 * 
	 * @param numSuccessRequests the number of successful requests
	 */
	public void setNumSuccessRequests(int numSuccessRequests) {
		this.numSuccessRequests = numSuccessRequests;
	}
	
	/**
	 * Returns the number of HTTP request that resulted in a client error status code during
	 * the minute period that this HTTP request statistics item represents.
	 * 
	 * @return the number of HTTP requests that resulted in a client error status code
	 */
	public int getNumClientErrorRequests() {
		return this.numClientErrorRequests;
	}
	
	/**
	 * Sets the number of HTTP requests that resulted in a client error status code to the
	 * specified number of requests.
	 * 
	 * @param numClientErrorRequests the number of requests that resulted in a client error status code
	 */
	public void setNumClientErrorRequests(int numClientErrorRequests) {
		this.numClientErrorRequests = numClientErrorRequests;
	}

	/**
	 * Returns the number of HTTP request that resulted in a server error status code during
	 * the minute period that this HTTP request statistics item represents.
	 * 
	 * @return the number of HTTP requests that resulted in a server error status code
	 */
	public int getNumServerErrorRequests() {
		return this.numServerErrorRequests;
	}

	/**
	 * Sets the number of HTTP requests that resulted in a server error status code to the
	 * specified number of requests.
	 * 
	 * @param numServerErrorRequests the number of requests that resulted in a server error status code
	 */
	public void setNumServerErrorRequests(int numServerErrorRequests) {
		this.numServerErrorRequests = numServerErrorRequests;
	}
	
	/**
	 * Returns the sum of request duration during the minute period that this HTTP request statistics
	 * represents.
	 * 
	 * @return the sum of request duration
	 */
	public long getSumRequestsDuration() {
		return this.sumRequestsDuration;
	}
	
	/**
	 * Sets the sum of request duration to the specified sum requests duration.
	 * 
	 * @param sumRequestsDuration the sum requests duration
	 */
	public void setSumRequestsDuration(long sumRequestsDuration) {
		this.sumRequestsDuration = sumRequestsDuration;
	}
	
	/**
	 * Returns the sum response bytes during the muinute period that this HTTP request statistics
	 * represents.
	 * 
	 * @return the sum response bytes
	 */
	public int getSumResponseBytes() {
		return this.sumResponseBytes;
	}
	
	/**
	 * Sets the sum response bytes to the specified sum response bytes.
	 * 
	 * @param sumResponseBytes the sum response bytes
	 */
	public void setSumResponseBytes(int sumResponseBytes) {
		this.sumResponseBytes = sumResponseBytes;
	}
}
