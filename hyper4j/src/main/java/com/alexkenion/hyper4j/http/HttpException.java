package com.alexkenion.hyper4j.http;

public class HttpException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public HttpException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public HttpException(String message) {
		this(message, null);
	}

}
