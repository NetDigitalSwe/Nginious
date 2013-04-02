package com.nginious.http.application;

import com.nginious.http.HttpMethod;
import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.annotation.Controller;
import com.nginious.http.annotation.Request;

@Controller(path = "/")
public class TestController {
	
	@Request(methods = { HttpMethod.GET })
	public TestControllerBean doGet(HttpRequest request, HttpResponse response, TestControllerBean bean) {
		TestControllerBean outBean = new TestControllerBean();
		outBean.setOne(bean.getOne());
		outBean.setTwo(bean.getTwo());
		return outBean;
	}
}
