package com.alexkenion.hyper4j;

public interface RequestHandler {
	
	public HttpResponse handle(HttpRequest request);

}
