package com.nginious.http.server;

import java.util.HashMap;

public enum HttpParserHeader {
	
	CONTENT_TYPE("content-type"),
	
	CONTENT_LENGTH("content-length"),
	
	TRANSFER_ENCODING("transfer-encoding"),
	
	EXPECT("expect");
	
	private static HashMap<String, HttpParserHeader> lookup = new HashMap<String, HttpParserHeader>();
	
	static {
		lookup.put("content-type", CONTENT_TYPE);
		lookup.put("content-length", CONTENT_LENGTH);
		lookup.put("transfer-encoding", TRANSFER_ENCODING);
		lookup.put("expect", EXPECT);
	}
	
	private String name;
	
	private HttpParserHeader(String name) {
		this.name = name;
	}
	
	public static HttpParserHeader getHttpParserHeader(String name) {
		if(name == null) {
			return null;
		}
		
		return lookup.get(name.toLowerCase());
	}
	
	public String getName() {
		return this.name;
	}
}
