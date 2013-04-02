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
import com.nginious.http.HttpMethod;
import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.HttpSession;
import com.nginious.http.annotation.Controller;
import com.nginious.http.annotation.Request;

@Controller(path = "/session")
@SuppressWarnings("unused")
public class TestSessionController {
	
	public TestSessionController() {
		super();
	}
	
	@Request(methods = { HttpMethod.GET })
	public void executeGet(HttpRequest request, HttpResponse response) throws IOException {
		HttpCookie[] cookies = request.getCookies();
		
		HttpSession session = request.getSession(false);
		String data = (String)session.getAttribute("data");
		
		response.setContentLength(data.length());
		response.setContentType("text/plain");
		response.setCharacterEncoding("iso-8859-1");
		
		PrintWriter writer = response.getWriter();
		writer.print(data);
	}
	
	@Request(methods = { HttpMethod.POST })
	public void executePost(HttpRequest request, HttpResponse response) throws IOException {
		String data = request.getParameter("data");
		HttpSession session = request.getSession(true);
		session.setAttribute("data", data);
		
		response.setContentLength(0);
		response.setContentType("text/plain");
		response.setCharacterEncoding("utf-8");
	}
}
