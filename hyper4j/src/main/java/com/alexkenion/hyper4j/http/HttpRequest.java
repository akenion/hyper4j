package com.alexkenion.hyper4j.http;

public class HttpRequest extends HttpMessage {
	
	private String method;
	private Url url;
	
	public HttpRequest(String method, Url url, HttpHeaders headers) {
		super(headers);
		this.method=method;
		this.url=url;
	}
	
	public HttpRequest(String method, Url url) {
		this(method, url, new HttpHeaders());
	}
	
	public String getMethod() {
		return method;
	}
	
	public Url getUrl() {
		return url;
	}
	
	public boolean shouldClose() {
		return ConnectionOption.CLOSE.matches(getHeader(Http.HEADER_CONNECTION));
	}
	
}
