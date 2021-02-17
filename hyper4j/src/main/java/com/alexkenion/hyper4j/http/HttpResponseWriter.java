package com.alexkenion.hyper4j.http;

public interface HttpResponseWriter {
	
	/**
	 * @throws HttpException;
	 */
	public void write(HttpVersion protocolVersion, HttpResponse response) throws HttpException;

}
