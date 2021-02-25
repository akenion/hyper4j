package com.alexkenion.hyper4j.http.auth;

import com.alexkenion.hyper4j.http.Http;
import com.alexkenion.hyper4j.http.HttpRequest;

public class Credentials {
	
	private String scheme;
	
	protected Credentials(String scheme) {
		this.scheme=scheme;
	}
	
	protected Credentials(Scheme scheme) {
		this(scheme.getId());
	}
	
	public String getScheme() {
		return scheme;
	}
	
	public static Credentials fromRequest(HttpRequest request, String desiredScheme) {
		String authorization=request.getHeader(Http.HEADER_AUTHORIZATION);
		if(authorization==null)
			return null;
		String[] parts=authorization.split(Http.SPACE, 2);
		if(parts.length!=2)
			return null;
		Scheme scheme=Scheme.forString(parts[0]);
		if(desiredScheme!=null&&!scheme.matches(desiredScheme))
			return null;
		if(scheme==null)
			return new GenericCredentials(parts[0], parts[1]);
		return scheme.getProvider().parseCredentials(parts[1]);
	}
	
	public static Credentials fromRequest(HttpRequest request, Scheme desiredScheme) {
		return fromRequest(request, desiredScheme.toString());
	}
	
	public static Credentials fromRequest(HttpRequest request) {
		String desiredScheme=null;
		return fromRequest(request, desiredScheme);
	}

}
