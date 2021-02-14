package com.alexkenion.hyper4j;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse extends HttpMessage {
	
	private short status;

	public HttpResponse(short status, Map<String, String> headers) {
		super(headers);
		this.status=status;
	}

	public HttpResponse(short status) {
		this(status, new HashMap<String, String>());
	}
	
	public HttpResponse(int status) {
		this((short)status);
	}
	
	public short getStatus() {
		return status;
	}

}
