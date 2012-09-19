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
 * Maintains HTTP request statistics from server start until current time. Statistics
 * is segmented into entries of one minute each.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 * @see HttpRequestStatisticsEntry
 */
public class HttpRequestStatistics extends Statistics<HttpRequestStatisticsEntry> {
	
	/**
	 * Constructs a new HTTP request statistics.
	 */
	public HttpRequestStatistics() {
		super();
	}
	
	/**
	 * Adds a new HTTP request to the HTTP request statistics entry for the current
	 * minute.
	 * 
	 * @return the HTTP request statistics entry where the HTTP request was added
	 */
	public HttpRequestStatisticsEntry add() {
		HttpRequestStatisticsEntry entry = getEntry();
		entry.add();
		return entry;
	}
	
	/*
	 * Creates a new HTTP request statisticd entry with the specified start minute time
	 * in milliseconds.
	 */
	protected HttpRequestStatisticsEntry createEntry(long minuteMillis) {
		return new HttpRequestStatisticsEntry(minuteMillis);
	}
	
	protected HttpRequestStatisticsEntry[] createArray(int size) {
		return new HttpRequestStatisticsEntry[size];
	}

	protected HttpRequestStatisticsEntry[][] create2Array(int size) {
		return new HttpRequestStatisticsEntry[24][];
	}	
}
