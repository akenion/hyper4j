package com.alexkenion.hyper4j.http;

public class HttpUtil {
	
	private static final String QUOTE="\"";
	private static final String ESCAPED_QUOTE="\\\"";
	
	private static String escapeQuotes(String string) {
		//TODO: Properly implement this
		return string.replace(QUOTE, ESCAPED_QUOTE);
	}
	
	/**
	 * Format the provided string as "quoted-string," escaping quotes as necessary
	 * @param string
	 * @return
	 */
	public static String quote(String string) {
		StringBuilder builder=new StringBuilder();
		builder.append(QUOTE).append(escapeQuotes(string)).append(QUOTE);
		return builder.toString();
	}

}
