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

import java.io.IOException;

/**
 * A null output consumer which doesn't output its log messages anywhere.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class NullOutputConsumer implements LogOutputConsumer {
	
	/**
	 * Starts this log output consumer.
	 * 
	 * @throws IOException if unable to start consumer
	 */
	public void start() throws IOException {
		return;
	}
	
	/**
	 * Stops this log output consumer
	 * 
	 * @throws IOException if unable to stop
	 */
	public void stop() throws IOException {
		return;
	}
	
	/**
	 * Consumes the specified log line.
	 * 
	 * @param logLine the log line
	 */
	public void consume(byte[] logLine) {
		return;
	}

}
