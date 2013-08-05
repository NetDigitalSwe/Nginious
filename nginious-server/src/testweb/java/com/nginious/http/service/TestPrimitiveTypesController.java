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

import com.nginious.http.HttpMethod;
import com.nginious.http.HttpResponse;
import com.nginious.http.annotation.Controller;
import com.nginious.http.annotation.Parameter;
import com.nginious.http.annotation.Request;

@Controller(path = "/primitive")
public class TestPrimitiveTypesController {
	
	@Request(methods = { HttpMethod.GET })
	public void executeGet(HttpResponse response, @Parameter(name = "test") String value) throws IOException {
		response.setContentLength(value.getBytes().length + 1);
		PrintWriter writer = response.getWriter();
		writer.println(value);
	}

	@Request(methods = { HttpMethod.POST })
	public void executePost(HttpResponse response, @Parameter(name = "test") int value) throws IOException {
		response.setContentLength(Integer.toString(value).getBytes().length + 1);
		PrintWriter writer = response.getWriter();
		writer.println(Integer.toString(value));
	}

	@Request(methods = { HttpMethod.PUT })
	public void executePut(HttpResponse response, @Parameter(name = "test") Long value) throws IOException {
		response.setContentLength(value.toString().getBytes().length + 1);
		PrintWriter writer = response.getWriter();
		writer.println(value.toString());
	}

	@Request(methods = { HttpMethod.DELETE })
	public void executeDelete(HttpResponse response, @Parameter(name = "test") boolean value) throws IOException {
		response.setContentLength(Boolean.toString(value).getBytes().length + 1);
		PrintWriter writer = response.getWriter();
		writer.println(Boolean.toString(value));
	}
}
