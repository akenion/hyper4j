package com.alexkenion.hyper4j.http.auth;

public class GenericCredentials extends Credentials {
	
	private String parameters;
	
	public GenericCredentials(String scheme, String parameters) {
		super(scheme);
		this.parameters=parameters;
	}
	
	public String getParameters() {
		return parameters;
	}

}
