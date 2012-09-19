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
import com.nginious.http.HttpServiceResult;
import com.nginious.http.application.Service;
import com.nginious.http.rest.RestRequest;
import com.nginious.http.rest.RestResponse;
import com.nginious.http.rest.RestService;

@Service(path = "/rasync")
public class TestAsyncRestService extends RestService<TestBean1, TestBean2> {
	
	public HttpServiceResult executeGet(RestRequest<TestBean1> request, RestResponse<TestBean2> response) throws HttpException, IOException {
		Async async = new Async(request, response);
		Thread thread = new Thread(async);
		thread.start();
		return HttpServiceResult.ASYNC;
	}

	private class Async implements Runnable {
		
		private RestRequest<TestBean1> request;
		
		private RestResponse<TestBean2> response;
		
		Async(RestRequest<TestBean1> request, RestResponse<TestBean2> response) {
			this.request = request;
			this.response = response;
		}
		
		public void run() {
			try { Thread.sleep(2000); } catch(InterruptedException e) {}

			TestBean1 bean = request.getBean();
			TestBean2 out = new TestBean2();
			out.setFirst(bean.getFirst());
			out.setSecond(bean.getSecond());
			out.setThird(bean.getThird());
			out.setFourth(bean.getFourth());
			out.setFifth(bean.getFifth());
			out.setSixth(bean.getSixth());
			out.setSeventh(bean.getSeventh());
			out.setEight(bean.getEight());
			out.setNinth(bean.getNinth());
			
			response.setBean(out);
			response.completed();
		}
	}
}
