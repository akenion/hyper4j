package com.alexkenion.hyper4j.http.auth.basic;

import com.alexkenion.hyper4j.http.auth.Challenge;
import com.alexkenion.hyper4j.http.auth.Scheme;

public class BasicAuthenticationChallenge extends Challenge{
	
	public BasicAuthenticationChallenge(String realm) {
		super(Scheme.BASIC.getId(), realm);
	}
	
}
