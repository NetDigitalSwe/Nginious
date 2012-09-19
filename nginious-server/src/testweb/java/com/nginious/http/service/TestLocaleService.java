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
import java.util.Locale;

import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.HttpService;
import com.nginious.http.HttpServiceResult;
import com.nginious.http.application.Service;

@Service(path = "/locale")
public class TestLocaleService extends HttpService {
	
	public TestLocaleService() {
		super();
	}
	
	public HttpServiceResult executeGet(HttpRequest request, HttpResponse response) throws IOException {
		Locale locale = request.getLocale();
		response.setContentType("text/plain");
		response.setCharacterEncoding("utf-8");
		
		PrintWriter writer = response.getWriter();
		
		if(locale.equals(new Locale("en-us"))) {
			response.setContentLength("en-us".getBytes().length);
			writer.write("en-us");
		} else if(locale.equals(new Locale("sv-se"))) {
			response.setContentLength("en-us".getBytes().length);
			writer.write("sv-se");			
		} else {
			response.setContentLength("unknown".getBytes().length);
			writer.write("unknown");
		}
		
		return HttpServiceResult.DONE;
	}
}
