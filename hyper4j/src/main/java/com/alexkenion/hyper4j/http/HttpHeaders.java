package com.alexkenion.hyper4j.http;

import java.util.TreeMap;

/**
 * A map of headers for a given HTTP message
 * @author Alex Kenion
 *
 */
public class HttpHeaders extends TreeMap<String, String> {
	
	private static final long serialVersionUID = 9029261965791550972L;

	public HttpHeaders() {
		super(String.CASE_INSENSITIVE_ORDER);
	}

}
