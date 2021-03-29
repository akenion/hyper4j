package com.alexkenion.hyper4j.http;

public abstract class HttpMessage {

	private HttpHeaders headers;
	private MessageBody body;
	
	public HttpMessage(HttpHeaders headers) {
		this.headers=headers;
	}
	
	public HttpMessage() {
		this(new HttpHeaders());
	}

	public void setHeader(String key, String value) {
		this.headers.put(key, value);
	}
	
	public void setHeader(String key, int value) {
		this.headers.put(key, Integer.toString(value));
	}
	
	public String getHeader(String key) {
		return this.headers.get(key);
	}
	
	public boolean hasHeader(String key) {
		return this.headers.containsKey(key);
	}
	
	public HttpHeaders getHeaders() {
		return headers;
	}
	
	public void setBody(MessageBody body) {
		this.body=body;
	}
	
	public MessageBody getBody() {
		return body;
	}
	
	public int getContentLength() {
		if(!this.hasHeader(Http.HEADER_CONTENT_LENGTH))
			return 0;
		try {
			return Integer.parseInt(this.getHeader(Http.HEADER_CONTENT_LENGTH));
		}
		catch(NumberFormatException e) {
			return 0;
		}
	}
	
	public boolean hasBody() {
		return this.getContentLength()>0;
	}

}
