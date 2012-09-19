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

import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.HttpService;
import com.nginious.http.HttpServiceResult;
import com.nginious.http.application.Service;

@Service(pattern = "/chain[0-9]+")
public class TestChainFilter extends HttpService {
	
	public TestChainFilter() {
		super();
	}
	
	public HttpServiceResult executeGet(HttpRequest request, HttpResponse response) throws IOException {
		String path = request.getPath();
		
		if(path.equals("/test/chain1")) {
			response.addHeader("Chain", "chain1");
			return HttpServiceResult.CONTINUE;
		} else if(path.equals("/test/chain2")) {
			response.addHeader("Chain", "chain2");
			return HttpServiceResult.CONTINUE;
			
		}
		
		return HttpServiceResult.DONE;
	}
}
