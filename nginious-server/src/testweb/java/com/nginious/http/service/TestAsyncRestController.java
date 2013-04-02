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

import com.nginious.http.HttpException;
import com.nginious.http.HttpMethod;
import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.annotation.Controller;
import com.nginious.http.annotation.Request;

@Controller(path = "/rasync")
public class TestAsyncRestController {
	
	@Request(methods = { HttpMethod.GET }, async = true)
	public void executeGet(TestBean1 bean1, HttpRequest request, HttpResponse response) throws HttpException, IOException {
		Async async = new Async(bean1, response);
		Thread thread = new Thread(async);
		thread.start();
	}

	private class Async implements Runnable {
		
		private HttpResponse response;
		
		private TestBean1 bean1;
		
		Async(TestBean1 bean1, HttpResponse response) {
			this.bean1 = bean1;
			this.response = response;
		}
		
		public void run() {
			try { Thread.sleep(2000); } catch(InterruptedException e) {}

			TestBean2 out = new TestBean2();
			out.setFirst(bean1.getFirst());
			out.setSecond(bean1.getSecond());
			out.setThird(bean1.getThird());
			out.setFourth(bean1.getFourth());
			out.setFifth(bean1.getFifth());
			out.setSixth(bean1.getSixth());
			out.setSeventh(bean1.getSeventh());
			out.setEight(bean1.getEight());
			out.setNinth(bean1.getNinth());
			
			response.setData(out);
			response.completed();
		}
	}
}
