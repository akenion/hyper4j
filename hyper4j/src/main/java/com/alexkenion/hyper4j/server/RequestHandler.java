package com.alexkenion.hyper4j.server;

import com.alexkenion.hyper4j.http.HttpRequest;
import com.alexkenion.hyper4j.http.HttpResponse;

public interface RequestHandler {
	
	public HttpResponse handle(HttpRequest request);

}
