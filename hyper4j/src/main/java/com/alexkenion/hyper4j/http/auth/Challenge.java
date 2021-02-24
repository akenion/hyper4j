package com.alexkenion.hyper4j.http.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alexkenion.hyper4j.http.Http;
import com.alexkenion.hyper4j.http.HttpResponse;
import com.alexkenion.hyper4j.http.HttpUtil;

public class Challenge {
	
	private static final String REALM_KEY="realm";
	
	private String scheme;
	private String realm;
	
	public Challenge(String scheme, String realm) {
		this.scheme=scheme;
		this.realm=realm;
	}
	
	public String getScheme() {
		return scheme;
	}
	
	public String getRealm() {
		return realm;
	}
	
	public Map<String, String> getParameters() {
		Map<String, String> parameters=new HashMap<String, String>();
		parameters.put(REALM_KEY, getRealm());
		return parameters;
	}
	
	public Set<Challenge> asSet() {
		Set<Challenge> set=new HashSet<Challenge>();
		set.add(this);
		return set;
	}
	
	public String toString() {
		StringBuilder builder=new StringBuilder(getScheme());
		Map<String, String> parameters=getParameters();
		if(parameters.size()>0)
			builder.append(Http.SPACE);
		boolean first=true;
		for(String key:parameters.keySet()) {
			if(!first)
				builder.append(", ");
			builder.append(key).append('=').append(HttpUtil.quote(parameters.get(key)));
			first=false;
		}
		return builder.toString();
	}
	
	public static HttpResponse generateResponse(Set<Challenge> challenges) {
		HttpResponse response=new HttpResponse(401);
		List<String> challengeStrings=new ArrayList<String>();
		for(Challenge challenge:challenges)
			challengeStrings.add(challenge.toString());
		String challengeString=String.join(", ", challengeStrings);
		response.setHeader(Http.HEADER_WWW_AUTHENTICATE, challengeString);
		response.setBody("");
		return response;
	}
	
	public static HttpResponse generateResponse(Challenge challenge) {
		return generateResponse(challenge.asSet());
	}

}
