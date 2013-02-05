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

import java.io.IOException;

import com.nginious.http.HttpException;
import com.nginious.http.HttpMethod;
import com.nginious.http.annotation.Controller;
import com.nginious.http.annotation.Request;
import com.nginious.http.stats.WebSocketSessionStatistics;
import com.nginious.http.stats.WebSocketSessionStatisticsEntry;

/*
 * A REST controller which returns web socket session statistics for a time period in the HTTP response. The
 * returned data contains entries for each minute within the time period.
 * 
 * <p>
 * In parameters
 * 
 * <ul>
 * <li>startTime - the start time in format yyyy-MM-dd'T'HH:mm:ssZ to return statistics entries for.</li>
 * <li>endTime - the end time in format yyyy-MM-dd'T'HH:mm:ssZ to return statistics entries for.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Example request
 * 
 * <code>
 * http://127.0.0.1/admin/wsstats?startTime=2012-05-20T13:00:00+02:00&endTime=2012-05-20T15:00:00+02:00
 * </code>
 * </p>
 * 
 */
@Controller(path = "/wsstats")
public class WebSocketSessionStatisticsService {
	
	private ApplicationManagerImpl manager;
	
	/*
	 * Constructs a new web socket session statistics service reading statistics from the specified
	 * application manager.
	 */
	WebSocketSessionStatisticsService(ApplicationManagerImpl manager) {
		this.manager = manager;
	}
	
	/*
	 * Returns web socket session statistics for the time period found in the statistics range in the specified REST request.
	 * The response is returned in the specified REST response. 
	 */
	@Request(methods = { HttpMethod.GET })
	public WebSocketSessionStatisticsInfo executeGet(StatisticsRange range) throws HttpException, IOException {
		if(range == null) {
			range = new StatisticsRange();
		}
		
		WebSocketSessionStatistics stats = manager.getWebSocketSessionStatistics();
		WebSocketSessionStatisticsEntry[] entries = stats.getEntries(range.getStartTime(), range.getEndTime());
		
		WebSocketSessionStatisticsInfo info = new WebSocketSessionStatisticsInfo();
		
		for(WebSocketSessionStatisticsEntry entry : entries) {
			WebSocketSessionStatisticsItem item = new WebSocketSessionStatisticsItem(entry);
			info.addItem(item);
		}
		
		return info;
	}
}
