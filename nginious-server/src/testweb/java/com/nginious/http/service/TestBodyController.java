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
import java.io.InputStream;
import java.io.OutputStream;

import com.nginious.http.HttpMethod;
import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.annotation.Controller;
import com.nginious.http.annotation.Request;

@Controller(path = "/body")
public class TestBodyController {
	
	public TestBodyController() {
		super();
	}
	
	@Request(methods = { HttpMethod.POST })
	public void executePost(HttpRequest request, HttpResponse response) throws IOException {
		InputStream in = request.getInputStream();
		int len = request.getContentLength();
		
		response.setContentLength(len);
		
		byte[] data = new byte[len];
		in.read(data);
		
		OutputStream out = response.getOutputStream();
		out.write(data);
	}
}
