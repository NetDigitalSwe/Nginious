package com.nginious.http.application;

import com.nginious.http.annotation.Serializable;

@Serializable
public class TestControllerBean {
	
	private String one;
	
	private int two;
	
	public TestControllerBean() {
		super();
	}

	public String getOne() {
		return this.one;
	}

	public void setOne(String one) {
		this.one = one;
	}

	public int getTwo() {
		return this.two;
	}

	public void setTwo(int two) {
		this.two = two;
	}
}
