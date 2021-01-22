package com.alexkenion.hyper4j.http;

import java.util.HashMap;
import java.util.Map;

public class Request extends Message {
	
	private String method;
	private String url;
	
	public Request(String method, String url, Map<String, String> headers) {
		super(headers);
		this.method=method;
		this.url=url;
	}
	
	public Request(String method, String url) {
		this(method, url, new HashMap<String, String>());
	}
	
	public String getMethod() {
		return method;
	}
	
	public String getUrl() {
		return url;
	}
	
	public int getContentLength() {
		if(!this.hasHeader("Content-Length"))
			return 0;
		try {
			return Integer.parseInt(this.getHeader("Content-Length"));
		}
		catch(NumberFormatException e) {
			return 0;
		}
	}
	
	public boolean hasBody() {
		return this.getContentLength()>0;
	}
	
}
