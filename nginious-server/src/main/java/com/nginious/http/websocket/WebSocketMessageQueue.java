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

package com.nginious.http.websocket;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The web socket message queue is a singleton which handles queueing and execution of web socket messages once 
 * they have been parsed. The queue can contain a maximum of 5000 messages. Any exceeding messages connections are 
 * closed with a status code of internal server error.
 * 
 * <p>
 * A minimum of 5 threads and a maximum of 500 threads are used for handling the queued contexts. The thread
 * pool is increased / decreased as needed depending on the queue size.
 * </p>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
class WebSocketMessageQueue implements RejectedExecutionHandler {
	
	private static WebSocketMessageQueue queue = null;
	
	private static Object lock = new Object();
	
	private ThreadPoolExecutor executor;
	
	/**
	 * Constructs a new web socket message queue.
	 */
	private WebSocketMessageQueue() {
		super();
		this.executor = new ThreadPoolExecutor(5, 500, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(5000));
	}
	
	/**
	 * Returns the web socket message queue instance.
	 * 
	 * @return the web socket message queue instance
	 */
	static WebSocketMessageQueue getInstance() {
		if(queue == null) {
			synchronized(lock) {
				if(queue == null) {
					queue = new WebSocketMessageQueue();
				}
			}
		}
		
		return queue;
	}
	
	/**
	 * Queues the specified web socket message to be executed by the specified web socket session.
	 * 
	 * @param session the web socket session
	 * @param message the web socket message
	 */
	void queue(WebSocketSessionImpl session, WebSocketMessage message) {
		executor.execute(new Processor(session, message));
	}
	
	/**
	 * Called when queue is full to close the underlying connection for the message
	 * that was rejected.
	 * 
	 * @param runnable the processor for handling the message
	 * @param executor the queue
	 */
	public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
		Processor processor = (Processor)runnable;
		WebSocketSessionImpl session = processor.getSession();
		
		try {
			session.sendClose(StatusCode.INTERNAL_SERVER_ERROR, "Too many messages");
		} catch(IOException e) {}
	}
	
	private class Processor implements Runnable {
		
		private WebSocketSessionImpl session;
		
		private WebSocketMessage message;
		
		Processor(WebSocketSessionImpl session, WebSocketMessage message) {
			super();
			this.session = session;
			this.message = message;
		}
		
		WebSocketSessionImpl getSession() {
			return this.session;
		}
		
		public void run() {
			session.execute(message);
		}
	}
}
