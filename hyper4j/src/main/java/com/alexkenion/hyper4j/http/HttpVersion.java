package com.alexkenion.hyper4j.http;

import java.util.regex.Pattern;

public class HttpVersion {
	
	private static final String PREFIX="HTTP/";
	private static final String SEPARATOR=".";
	private static final String TEMPLATE=PREFIX+"%d"+SEPARATOR+"%d";
	
	private int major, minor;
	
	public HttpVersion(int major, int minor) {
		this.major=major;
		this.minor=minor;
	}
	
	public HttpVersion(String version) throws HttpException {
		if(version.startsWith(PREFIX))
			version=version.substring(PREFIX.length());
		String[] parts=version.split(Pattern.quote(SEPARATOR));
		if(parts.length!=2)
			throw new HttpException("Malformed HTTP version: "+version);
		try {
			major=Integer.parseInt(parts[0]);
			minor=Integer.parseInt(parts[1]);
		}
		catch(NumberFormatException e) {
			throw new HttpException("Malformed HTTP version", e);
		}
	}
	
	public int getMajor() {
		return major;
	}
	
	public int getMinor() {
		return minor;
	}
	
	public int compare(HttpVersion other) {
		if(other.major!=major)
			return other.major-major;
		if(other.minor!=minor)
			return other.minor-minor;
		return 0;
	}
	
	@Override
	public String toString() {
		return String.format(TEMPLATE, major, minor);
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof HttpVersion) {
			return compare((HttpVersion)other)==0;
		}
		try {
			return compare(new HttpVersion(other.toString()))==0;
		}
		catch(HttpException e) {
			return false;
		}
	}

}
