package com.alexkenion.hyper4j.http.auth.basic;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.alexkenion.hyper4j.http.auth.Provider;

public class BasicAuthenticationProvider implements Provider {

	@Override
	public BasicAuthenticationCredentials parseCredentials(String header) {
		try {
			byte[] raw=Base64.getDecoder().decode(header);
			String decoded=new String(raw, StandardCharsets.UTF_8);
			String[] split=decoded.split(":", 2);
			if(split.length==2)
				return new BasicAuthenticationCredentials(split[0], split[1]);
		}
		catch(IllegalArgumentException e) {
			//
		}
		return null;
	}

}
