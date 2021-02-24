package com.alexkenion.hyper4j.http;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class Http {

	/**
	 * Standard header names
	 */
	public static final String
		HEADER_HOST="Host",
		HEADER_CONTENT_LENGTH="Content-Length",
		HEADER_SERVER="Server",
		HEADER_CONNECTION="Connection",
		HEADER_WWW_AUTHENTICATE="WWW-Authenticate",
		HEADER_AUTHORIZATION="Authorization";
	
	/**
	 * Misc. protocol constants
	 */
	public static final Charset ASCII=Charset.forName("ASCII");
	public static final String SPACE=" ";
	public static final String HEADER_DELIMITER=": ";
	
	public static enum Delimiter {
		CRLF(new byte[] {13, 10}),
		COLON(new byte[] {58});
		
		private byte[] raw;
		private String string;
		
		private Delimiter(byte[] raw) {
			this.raw=raw;
			this.string=ASCII.decode(ByteBuffer.wrap(raw)).toString();
		}
		
		public byte[] getBytes() {
			return raw;
		}
		
		public String getString() {
			return string;
		}
		
		public int getSize() {
			return raw.length;
		}
		
	}

}
