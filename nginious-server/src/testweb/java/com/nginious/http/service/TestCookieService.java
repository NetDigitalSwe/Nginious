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

import com.nginious.http.HttpCookie;
import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.HttpService;
import com.nginious.http.HttpServiceResult;
import com.nginious.http.application.Service;

@Service(path = "/cookie")
public class TestCookieService extends HttpService {
	
	public TestCookieService() {
		super();
	}
	
	public HttpServiceResult executeGet(HttpRequest request, HttpResponse response) throws IOException {
		HttpCookie cookie1 = request.getCookie("lang");
		String value1 = cookie1.getValue();
		HttpCookie cookie2 = request.getCookie("__utma");
		String value2 = cookie2.getValue();
		
		response.setContentType("text/plain");
		response.setCharacterEncoding("utf-8");
		response.setContentLength(value1.getBytes().length + 2 + value2.getBytes().length);
		
		PrintWriter writer = response.getWriter();
		writer.println(value1);
		writer.print(value2);
		return HttpServiceResult.DONE;
	}

	public HttpServiceResult executePost(HttpRequest request, HttpResponse response) throws IOException {
		HttpCookie cookie = new HttpCookie();
		cookie.setName("Test");
		cookie.setValue("testing");
		cookie.setDomain("test.com");
		cookie.setPath("/test/cookie");
		cookie.setVersion(2);
		cookie.setMaxAge(2);
		response.addCookie(cookie);
		
		response.setContentType("text/plain");
		response.setCharacterEncoding("utf-8");
		response.setContentLength(0);
		return HttpServiceResult.DONE;
	}
}
