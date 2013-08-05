package com.nginious.http.application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.nginious.http.HttpMethod;
import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.annotation.Controller;
import com.nginious.http.annotation.Request;

@Controller(path = "/return")
public class TestReturnController {
	
	@Request(methods = { HttpMethod.GET })
	public TestControllerBean executeGet(HttpRequest request, HttpResponse response) throws IOException {
		TestControllerBean test = new TestControllerBean();
		test.setOne("one");
		test.setTwo(2);
		return test;
	}
	
	@Request(methods = { HttpMethod.POST })
	public List<TestControllerBean> executePost(HttpRequest request, HttpResponse response) throws IOException {
		ArrayList<TestControllerBean> list = new ArrayList<TestControllerBean>();
		TestControllerBean test = new TestControllerBean();
		test.setOne("first");
		test.setTwo(20);
		list.add(test);
		return list;
	}
}
