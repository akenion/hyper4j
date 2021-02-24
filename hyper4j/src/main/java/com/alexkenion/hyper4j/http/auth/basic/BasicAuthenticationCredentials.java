package com.alexkenion.hyper4j.http.auth.basic;

import com.alexkenion.hyper4j.http.auth.Credentials;
import com.alexkenion.hyper4j.http.auth.Scheme;

public class BasicAuthenticationCredentials extends Credentials {
	
	private String username, password;
	
	public BasicAuthenticationCredentials(String username, String password) {
		super(Scheme.BASIC);
		this.username=username;
		this.password=password;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}

}
