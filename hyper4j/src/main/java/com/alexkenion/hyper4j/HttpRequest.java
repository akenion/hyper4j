package com.alexkenion.hyper4j;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest extends HttpMessage {
	
	private String method;
	private Url url;
	
	public HttpRequest(String method, Url url, Map<String, String> headers) {
		super(headers);
		this.method=method;
		this.url=url;
	}
	
	public HttpRequest(String method, Url url) {
		this(method, url, new HashMap<String, String>());
	}
	
	public String getMethod() {
		return method;
	}
	
	public Url getUrl() {
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
