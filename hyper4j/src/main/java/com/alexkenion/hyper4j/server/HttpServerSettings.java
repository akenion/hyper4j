package com.alexkenion.hyper4j.server;

/**
 * Settings for receiving HTTP requests
 * @author Alex Kenion
 *
 */
public class HttpServerSettings {
	
	/**
	 * RFC7230 Recommends a minimum request line length of 8000 octets
	 * (The request line must be able to be held entirely within the buffer with the current implementation)
	 */
	public static final int DEFAULT_BUFFER_SIZE=8000;
	
	private int bufferSize;
	
	/**
	 * Create a new settings instance with the specified values
	 * @param bufferSize the size(in bytes) of the buffer
	 */
	public HttpServerSettings(int bufferSize) {
		this.bufferSize=bufferSize;
	}
	
	/**
	 * Create a new settings instance using the default values
	 */
	public HttpServerSettings() {
		this(DEFAULT_BUFFER_SIZE);
	}
	
	/**
	 * Get the specified receiving buffer size
	 * @return the buffer size(in bytes)
	 */
	public int getBufferSize() {
		return bufferSize;
	}

}
