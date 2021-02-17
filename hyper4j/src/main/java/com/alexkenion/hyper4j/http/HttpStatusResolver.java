package com.alexkenion.hyper4j.http;

public interface HttpStatusResolver {
	
	public String getReasonPhrase(short status);

}
