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

import java.util.ArrayList;
import java.util.Collection;

import com.nginious.http.annotation.Serializable;

/**
 * A REST serializable HTTP request statistics info which contains a list of HTTP request 
 * statistics items for a time period. Each item represents one minute of statistics. 
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 * @see WebSocketSessionStatisticsItem
 */
@Serializable
public class HttpRequestStatisticsInfo {
	
	private Collection<HttpRequestStatisticsItem> items;
	
	/**
	 * Constructs a new HTTP request statistics info.
	 */
	public HttpRequestStatisticsInfo() {
		super();
		this.items = new ArrayList<HttpRequestStatisticsItem>();
	}

	/**
	 * Adds the specified HTTP request statistics item to this HTTP request
	 * statistics info.
	 * 
	 * @param item the HTTP request statistics item
	 */
	void addItem(HttpRequestStatisticsItem item) {
		items.add(item);
	}
	
	/**
	 * Adds all HTTP request statistics items in the specified collection to
	 * this HTTP request statistics info.
	 * 
	 * @param items the collection of HTTP request statistics items to add
	 */
	public void setItems(Collection<HttpRequestStatisticsItem> items) {
		this.items = items;
	}
	
	/**
	 * Returns all HTTP request statistics items for this HTTP request
	 * statistics info.
	 * 
	 * @return the collection of HTTP request statistics items
	 */
	public Collection<HttpRequestStatisticsItem> getItems() {
		return this.items;
	}
}
