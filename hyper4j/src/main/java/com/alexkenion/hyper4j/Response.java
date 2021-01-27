package com.alexkenion.hyper4j;

import java.util.HashMap;
import java.util.Map;

public class Response extends Message {
	
	private short status;

	public Response(short status, Map<String, String> headers) {
		super(headers);
		this.status=status;
	}

	public Response(short status) {
		this(status, new HashMap<String, String>());
	}
	
	public Response(int status) {
		this((short)status);
	}
	
	public short getStatus() {
		return status;
	}

}
