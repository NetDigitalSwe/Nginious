package com.nginious.http.service;

import java.io.IOException;
import java.io.PrintWriter;

import com.nginious.http.HttpException;
import com.nginious.http.HttpMethod;
import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.annotation.Controller;
import com.nginious.http.annotation.Request;

@Controller(path = "/servicetest")
public class TestServiceController {
	
	@Request(methods = { HttpMethod.GET })
	public void doGet(HttpRequest request, HttpResponse response, TestService service) throws HttpException, IOException {
		int value = service.getValue();
		String valueStr = Integer.toString(value);
		
		response.setContentLength(valueStr.length() + 1);
		response.setContentType("text/plain");
		response.setCharacterEncoding("utf-8");
		PrintWriter writer = response.getWriter();
		writer.println(valueStr);
	}
}
