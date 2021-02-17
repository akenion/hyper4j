package com.alexkenion.hyper4j.http;

public class HttpStatus {
	
	public static enum Series {
		//
	}
	
	public static class DefaultResolver implements HttpStatusResolver {

		@Override
		public String getReasonPhrase(short status) {
			//TODO: Implement
			return "OK";
		}

	}

}
