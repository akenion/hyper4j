package com.alexkenion.hyper4j.http;

public enum ConnectionOption {
	
	CLOSE("close");
	
	private String token;
	
	private ConnectionOption(String token) {
		this.token=token;
	}
	
	public String getToken() {
		return token;
	}
	
	public boolean matches(String header) {
		if(header==null)
			return false;
		return header.equalsIgnoreCase(token);
	}

}
