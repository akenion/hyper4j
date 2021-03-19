package com.alexkenion.hyper4j.util;

import java.util.Base64;

/**
 * A static wrapper for base64 encoding/decoding to allow an alternate implementation
 * to be substituted at runtime(i.e. Android's, since older API versions do not include
 * the standard java.util version)
 * @author Alex Kenion
 *
 */
public class Base64Util {
	
	public interface Implementation {
		public byte[] decode(String base64);
		public byte[] encode(byte[] raw);
	}
	
	public static class JavaUtilImplementation implements Implementation {

		@Override
		public byte[] decode(String base64) {
			try {
				return Base64.getDecoder().decode(base64);
			}
			catch(IllegalArgumentException e) {
				//Invalid base 64 encoding, ignore
				return null;
			}
		}

		@Override
		public byte[] encode(byte[] raw) {
			return Base64.getEncoder().encode(raw);
		}

	}
	
	private static Implementation implementation;
	
	public static void setImplemenation(Implementation implementation) {
		Base64Util.implementation=implementation;
	}
	
	private static Implementation getImplementation() {
		if(implementation==null)
			implementation=new JavaUtilImplementation();
		return implementation;
	}
	
	public static byte[] decode(String base64) {
		return getImplementation().decode(base64);
	}
	
	public static byte[] encode(byte[] raw) {
		return getImplementation().encode(raw);
	}

}
