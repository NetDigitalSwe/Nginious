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
 * Possible message levels for use with {@link MessageLog}. A message level has a name and
 * a numeric value.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public enum MessageLevel {
	
	/**
	 * Message level error indicating an error condition.
	 */
	ERROR(1),
	
	/**
	 * Message level warning indicating a warning.
	 */
	WARN(2),
	
	/**
	 * Message level info for informational messages.
	 */
	INFO(3),
	
	/**
	 * Message level event for event information.
	 */
	EVENT(4),
	
	/**
	 * Trace message level for method tracing.
	 */
	TRACE(5),
	
	/**
	 * Message level debug for detailed debug information.
	 */
	DEBUG(6);
	
	private int level;
	
	/**
	 * Constructs a new message level with the specified numeric level.
	 * 
	 * @param level the numeric level
	 */
	private MessageLevel(int level) {
		this.level = level;
	}
	
	/**
	 * Returns the numeric message level.
	 * 
	 * @return the numeric message level
	 */
	public int getLevel() {
		return this.level;
	}
}
