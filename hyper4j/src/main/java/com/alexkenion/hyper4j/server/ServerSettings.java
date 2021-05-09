package com.alexkenion.hyper4j.server;

import com.alexkenion.hyper4j.Hyper4J;
import com.alexkenion.hyper4j.util.NumberUtil;

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
	public static final int DEFAULT_IDLE_TIMEOUT=30;
	
	private int bufferSize;
	private String serverIdentifier;
	private long idleTimeoutMilliseconds;
	private int workerCount;
	private Long readTimeout;
	
	/**
	 * Create a new settings instance using the default values
	 */
	public ServerSettings() {
		this.bufferSize=DEFAULT_BUFFER_SIZE;
		this.serverIdentifier=Hyper4J.SERVER_IDENTIFIER;
		this.idleTimeoutMilliseconds=NumberUtil.secondsToMilliseconds(DEFAULT_IDLE_TIMEOUT);
		this.workerCount=Runtime.getRuntime().availableProcessors();
		this.readTimeout=this.idleTimeoutMilliseconds;
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

	/**
	 * Set the server identifier to be returned in each response in the Server HTTP header
	 * @param serverIdentifier the identifier or null to exclude the Server header from responses
	 */
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

	/**
	 * @param idleTimeout the max connection duration without transfer (in seconds) or 0 for unlimited
	 */
	public ServerSettings setIdleTimeout(int idleTimeout) {
		this.idleTimeoutMilliseconds = NumberUtil.secondsToMilliseconds(idleTimeout);
		return this;
	}

	public int getIdleTimeout() {
		return (int) NumberUtil.millisecondsToSeconds(idleTimeoutMilliseconds);
	}
	
	public ServerSettings setIdleTimeoutMilliseconds(long idleTimeoutMilliseconds) {
		this.idleTimeoutMilliseconds = idleTimeoutMilliseconds;
		return this;
	}
	
	public long getIdleTimeoutMilliseconds() {
		return this.idleTimeoutMilliseconds;
	}
	
	public ServerSettings setWorkerCount(int workerCount) {
		this.workerCount=workerCount;
		return this;
	}
	
	public int getWorkerCount() {
		return workerCount;
	}
	
	/**
	 * @param readTimeout the read timeout (in milliseconds)
	 */
	public ServerSettings setReadTimeout(Long readTimeout) {
		this.readTimeout = readTimeout;
		return this;
	}
	
	public ServerSettings disableReadTimeout() {
		this.readTimeout = null;
		return this;
	}
	
	public boolean hasReadTimeout() {
		return readTimeout != null;
	}
	
	public Long getReadTimeout() {
		return readTimeout;
	}
	
}
