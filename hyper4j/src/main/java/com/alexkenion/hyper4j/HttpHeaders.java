package com.alexkenion.hyper4j;

import java.util.TreeMap;

/**
 * A map of headers for a given HTTP message
 * @author Alex Kenion
 *
 */
public class HttpHeaders extends TreeMap<String, Object> {
	
	private static final long serialVersionUID = 9029261965791550972L;

	public static class Value {
		//
	}
	
	public HttpHeaders() {
		super(String.CASE_INSENSITIVE_ORDER);
	}

}
