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
 * A REST serializable web socket session statistics info which contains a list of web socket
 * session statistics items for a time period. Each item represents one minute of statistics. 
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 * @see WebSocketSessionStatisticsItem
 */
@Serializable
public class WebSocketSessionStatisticsInfo {
	
	private Collection<WebSocketSessionStatisticsItem> items;
	
	/**
	 * Constructs a new web socket session statistics info.
	 */
	public WebSocketSessionStatisticsInfo() {
		super();
		this.items = new ArrayList<WebSocketSessionStatisticsItem>();
	}
	
	/**
	 * Adds the specified web socket session statistics item to this web socket session
	 * statistics info.
	 * 
	 * @param item the web socket session statistics item
	 */
	void addItem(WebSocketSessionStatisticsItem item) {
		items.add(item);
	}
	
	/**
	 * Adds all web socket session statistics items in the specified collection to
	 * this web socket session statistics info.
	 * 
	 * @param items the collection of web socket session statistics items to add
	 */
	public void setItems(Collection<WebSocketSessionStatisticsItem> items) {
		this.items = items;
	}
	
	/**
	 * Returns all web socket session statistics items for this web socket session
	 * statistics info.
	 * 
	 * @return the collection of web socket session statistics items
	 */
	public Collection<WebSocketSessionStatisticsItem> getItems() {
		return this.items;
	}
}
