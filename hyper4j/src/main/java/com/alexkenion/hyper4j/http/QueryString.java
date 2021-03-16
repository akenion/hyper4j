package com.alexkenion.hyper4j.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class QueryString {
	
	private static final String KEY_VALUE_DELIMITER="=";
	private static final String PAIR_DELIMITER="&";
	private static final String ENCODING="UTF-8";
	
	private Map<String, String> parameters;
	
	public QueryString(String query) {
		parameters=parse(query);
	}
	
	public Map<String, String> getParameters() {
		return parameters;
	}
	
	public String get(String key) {
		return parameters.get(key);
	}
	
	@Override
	public String toString() {
		StringBuilder builder=new StringBuilder();
		boolean first=true;
		for(String key:parameters.keySet()) {
			if(!first)
				builder.append(PAIR_DELIMITER);
			String value=parameters.get(key);
			builder.append(urlEncode(key)).append(KEY_VALUE_DELIMITER).append(urlEncode(value));
			if(first)
				first=false;
		}
		return builder.toString();
	}
	
	private static String urlEncode(String raw) {
		try {
			return URLEncoder.encode(raw, ENCODING);
		} catch (UnsupportedEncodingException e) {
			//TODO: This should not be reachable, but it still needs to be handled
			e.printStackTrace();
			return null;
		}
	}
	
	private static String urlDecode(String encoded) {
		try {
			return URLDecoder.decode(encoded, ENCODING);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	protected static Map<String, String> parse(String query) {
		Map<String, String> params=new HashMap<String, String>();
		String[] pairs=query.split(PAIR_DELIMITER);
		for(String pair:pairs) {
			String[] kv=pair.split(KEY_VALUE_DELIMITER, 2);
			String key=urlDecode(kv[0]);
			if(key.isEmpty())
				continue;
			params.put(key, kv.length==2?urlDecode(kv[1]):null);
		}
		return params;
	}
	
	public static QueryString fromRequest(HttpRequest request) {
		return new QueryString(request.getUrl().getQuery());
	}

}
