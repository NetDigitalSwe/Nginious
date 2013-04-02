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
import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.annotation.Controller;
import com.nginious.http.annotation.Request;

@Controller(path = "/parameters")
public class TestParametersController {
	
	public TestParametersController() {
		super();
	}
	
	@Request(methods = { HttpMethod.GET })
	public void executeGet(HttpRequest request, HttpResponse response) throws IOException {
		response.setCharacterEncoding("utf-8");
		PrintWriter writer = response.getWriter();
		writer.print("param1=" + request.getParameter("param1"));
		writer.print("&param2=" + request.getParameter("param2"));
		String[] values = request.getParameterValues("param3");
		
		for(String value : values) {
			writer.print("&param3=" + value);
		}
		
		writer.print("&param4=" + request.getParameter("param4"));
		
		writer.println();
	}

	@Request(methods = { HttpMethod.POST })
	public void executePost(HttpRequest request, HttpResponse response) throws IOException {
		response.setCharacterEncoding("utf-8");
		PrintWriter writer = response.getWriter();
		writer.print("param1=" + request.getParameter("param1"));
		writer.print("&param2=" + request.getParameter("param2"));
		String[] values = request.getParameterValues("param3");
		
		for(String value : values) {
			writer.print("&param3=" + value);
		}
		
		writer.print("&param4=" + request.getParameter("param4"));
		
		writer.println();
	}

	@Request(methods = { HttpMethod.PUT })
	public void executePut(HttpRequest request, HttpResponse response) throws IOException {
		PrintWriter writer = response.getWriter();
		writer.println("PUT Hello World!");
	}

	@Request(methods = { HttpMethod.DELETE })
	public void executeDelete(HttpRequest request, HttpResponse response) throws IOException {
		PrintWriter writer = response.getWriter();
		writer.println("DELETE Hello World!");
	}
}
