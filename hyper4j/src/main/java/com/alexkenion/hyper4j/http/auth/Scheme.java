package com.alexkenion.hyper4j.http.auth;

import com.alexkenion.hyper4j.http.auth.basic.BasicAuthenticationProvider;

public enum Scheme {
	
	BASIC("Basic", new BasicAuthenticationProvider());
	
	private String id;
	private Provider provider;
	
	private Scheme(String id, Provider provider) {
		this.id=id;
		this.provider=provider;
	}
	
	public String getId() {
		return id;
	}
	
	public Provider getProvider() {
		return provider;
	}
	
	public boolean matches(String scheme) {
		return id.equalsIgnoreCase(scheme);
	}
	
	public static Scheme forString(String scheme) {
		for(Scheme s:Scheme.values()) {
			if(s.matches(scheme))
				return s;
		}
		return null;
	}

}
