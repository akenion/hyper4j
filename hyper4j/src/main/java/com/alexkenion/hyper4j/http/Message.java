package com.alexkenion.hyper4j.http;

import java.util.HashMap;
import java.util.Map;

public abstract class Message {

	private Map<String, String> headers;
	private String body="";
	
	public Message(Map<String, String> headers) {
		this.headers=headers;
	}
	
	public Message() {
		this(new HashMap<String, String>());
	}

	public void setHeader(String key, String value) {
		this.headers.put(key.toUpperCase(), value);
	}
	
	public String getHeader(String key) {
		return this.headers.get(key.toUpperCase());
	}
	
	public boolean hasHeader(String key) {
		return this.headers.containsKey(key);
	}
	
	public Map<String, String> getHeaders() {
		return headers;
	}
	
	public void setBody(String body) {
		this.body=body;
	}
	
	public String getBody() {
		return body;
	}

}
