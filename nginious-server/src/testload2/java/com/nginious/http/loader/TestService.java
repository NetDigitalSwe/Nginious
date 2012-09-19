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

package com.nginious.http.loader;

import java.io.IOException;

import com.nginious.http.HttpException;
import com.nginious.http.HttpServiceResult;
import com.nginious.http.application.Service;
import com.nginious.http.loader.TestBean1;
import com.nginious.http.loader.TestBean2;
import com.nginious.http.rest.RestRequest;
import com.nginious.http.rest.RestResponse;
import com.nginious.http.rest.RestService;

@Service(path="/test")
public class TestService extends RestService<TestBean1, TestBean2> {

	public HttpServiceResult executeGet(RestRequest<TestBean1> request, RestResponse<TestBean2> response) throws HttpException, IOException {
		TestBean1 bean1 = request.getBean();
		TestBean2 bean2 = new TestBean2();
		
		bean2.setFirst(bean1.getFirst());
		bean2.setSecond(bean1.getSecond());
		bean2.setThird(bean1.getThird());
		response.setBean(bean2);
		return HttpServiceResult.DONE;
	}
}
