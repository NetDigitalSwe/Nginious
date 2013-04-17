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

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.nginious.http.HttpStatus;

/**
 * Handles execution and lifecycle of HTTP contexts once request has been received and parsed. HTTP contexts
 * are queued for execution. The queue can contain a maximum of 5000 contexts. Any exceeding contexts are
 * rejected and a HTTP 503 Service Unavailable is sent as a response immediately.
 * 
 * <p>
 * A minimum of 5 threads and a maximum of 500 threads ar used for handling the queued contexts. The thread
 * pool is increased / decreased as needed depending on the queue size.
 * </p>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
class HttpContextManager implements RejectedExecutionHandler {
	
	private ThreadPoolExecutor executor;

	private Set<HttpContext> pendingContexts;
	
	/**
	 * Constructs a new HTTP context manager.
	 */
	HttpContextManager() {
		super();
		this.executor = new ThreadPoolExecutor(5, 500, 10, 
				TimeUnit.SECONDS, 
				new ArrayBlockingQueue<Runnable>(5000),
				new HttpContextThreadFactory());
		this.pendingContexts = Collections.newSetFromMap(new ConcurrentHashMap<HttpContext,Boolean>());
	}
	
	/**
	 * Adds the specified HTTP context to the queue for execution.
	 * 
	 * @param context the HTTP context
	 */
	void manage(HttpContext context) {
		executor.execute(new Processor(context));
	}
	
	/**
	 * Removes the specified HTTP context from this HTTP context manager.
	 * 
	 * @param context the context to remove
	 * @return whether or not HTTP context was removed
	 */
	boolean unmanage(HttpContext context) {
		if(pendingContexts.remove(context)) {
			context.completed();
			return true;
		}
		
		return false;
	}
	
	/**
	 * Called when HTTP context queue is full. Sends a HTTP 503 Service Unavailable as a response.
	 * 
	 * @param runnable the HTTP context that should have been queued
	 * @param executor the queue
	 */
	public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
		Processor processor = (Processor)runnable;
		HttpContext context = processor.getContext();
		context.handleError(HttpStatus.SERVICE_UNAVAILABLE, "service unavailable");
	}
	
	private class Processor implements Runnable {
		
		private HttpContext context;
		
		Processor(HttpContext context) {
			super();
			this.context = context;
		}
		
		HttpContext getContext() {
			return this.context;
		}
		
		public void run() {
			boolean done = true;
			
			try {
				done = context.execute(HttpContextManager.this);
			} finally {
				if(done) {
					context.completed();
				} else {
					pendingContexts.add(context);
				}
			}
		}
	}
	
	private class HttpContextThreadFactory implements ThreadFactory {
		
		private AtomicInteger threadIdCreator;
		
		private HttpContextThreadFactory() {
			super();
			this.threadIdCreator = new AtomicInteger(1);
		}
		
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r);
			int threadId = threadIdCreator.getAndIncrement();
			thread.setName("http-" + threadId);
			return thread;
		}
	}	
}
