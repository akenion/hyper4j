package com.alexkenion.hyper4j;

import com.alexkenion.hyper4j.http.Request;
import com.alexkenion.hyper4j.http.Response;

public interface RequestHandler {
	
	public Response handle(Request request);

}
