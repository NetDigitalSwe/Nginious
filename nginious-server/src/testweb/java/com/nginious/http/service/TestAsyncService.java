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

package com.nginious.http.service;

import java.io.IOException;
import java.io.PrintWriter;

import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.HttpService;
import com.nginious.http.HttpServiceResult;
import com.nginious.http.application.Service;

@Service(path = "/async")
public class TestAsyncService extends HttpService {
	
	public HttpServiceResult executeGet(HttpRequest request, HttpResponse response) throws IOException {
		Async async = new Async(request, response);
		Thread thread = new Thread(async);
		thread.start();
		return HttpServiceResult.ASYNC;
	}

	private class Async implements Runnable {
		
		private HttpRequest request;
		
		private HttpResponse response;
		
		Async(HttpRequest request, HttpResponse response) {
			this.request = request;
			this.response = response;
		}
		
		public void run() {
			try { Thread.sleep(2000); } catch(InterruptedException e) {}
			
			response.setContentLength("GET Hello Async!\n".getBytes().length);
			PrintWriter writer = response.getWriter();
			writer.println("GET Async World!");
			response.completed();
		}
	}
}