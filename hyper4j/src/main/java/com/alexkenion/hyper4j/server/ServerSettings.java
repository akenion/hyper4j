package com.alexkenion.hyper4j.server;

import com.alexkenion.hyper4j.Hyper4J;

/**
 * Settings for receiving HTTP requests
 * @author Alex Kenion
 *
 */
public class ServerSettings {
	
	/**
	 * RFC7230 Recommends a minimum request line length of 8000 octets
	 * (The request line must be able to be held entirely within the buffer with the current implementation)
	 */
	public static final int DEFAULT_BUFFER_SIZE=8000;
	
	private int bufferSize;
	private String serverIdentifier;
	
	/**
	 * Create a new settings instance with the specified values
	 * @param bufferSize the size(in bytes) of the buffer
	 */
	public ServerSettings(int bufferSize, String serverIdentifier) {
		this.bufferSize=bufferSize;
		this.serverIdentifier=serverIdentifier;
	}

	/**
	 * Create a new settings instance using the default values
	 */
	public ServerSettings() {
		this(DEFAULT_BUFFER_SIZE, Hyper4J.SERVER_IDENTIFIER);
	}
	
	public ServerSettings setBufferSize(int bufferSize) {
		this.bufferSize=bufferSize;
		return this;
	}
	
	/**
	 * Get the specified receiving buffer size
	 * @return the buffer size(in bytes)
	 */
	public int getBufferSize() {
		return bufferSize;
	}
	
	public ServerSettings setServerIdentifier(String serverIdentifier) {
		this.serverIdentifier=serverIdentifier;
		return this;
	}
	
	public boolean hasServerIdentifier() {
		return serverIdentifier!=null;
	}
	
	public String getServerIdentifier() {
		return serverIdentifier;
	}

}
