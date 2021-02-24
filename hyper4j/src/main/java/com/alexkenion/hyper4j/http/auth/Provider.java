package com.alexkenion.hyper4j.http.auth;

public interface Provider {
	
	public Credentials parseCredentials(String header);

}
