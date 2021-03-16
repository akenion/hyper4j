package com.alexkenion.hyper4j.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.alexkenion.hyper4j.http.ChannelOutputBufferConsumer;
import com.alexkenion.hyper4j.http.HttpException;
import com.alexkenion.hyper4j.http.HttpParser;
import com.alexkenion.hyper4j.http.HttpRequest;
import com.alexkenion.hyper4j.http.HttpVersion;
import com.alexkenion.hyper4j.http.OutputBufferConsumer;
import com.alexkenion.hyper4j.logging.LogLevel;
import com.alexkenion.hyper4j.logging.Logger;

public class Session {
	
	private static final long LOCK_TIMEOUT=60;
	
	private SocketChannel channel;
	private SessionObserver observer;
	private ReentrantLock lock;
	private ByteBuffer buffer;
	private HttpParser parser;
	private HttpVersion currentProtocolVersion=HttpVersion.V1_1;
	private long lastInteraction;
	private SocketAddress clientAddress;
	
	protected ServerSettings settings;
	protected Logger logger;
	
	public Session(ServerSettings settings, SocketChannel channel, Logger logger) {
		this.settings=settings;
		this.channel=channel;
		this.logger=logger;
		this.lock=new ReentrantLock();
		buffer=ByteBuffer.allocate(settings.getBufferSize());
		touch();
		this.setClientAddress();
	}
	
	protected HttpParser initializeHttpParser() {
		return new HttpParser(buffer);
	}
	
	private HttpParser getHttpParser() {
		if(parser==null)
			parser=initializeHttpParser();
		return parser;
	}
	
	public void setObserver(SessionObserver observer) {
		this.observer=observer;
	}
	
	public void touch() {		
		this.lastInteraction=System.currentTimeMillis();
	}
	
	public long getLastInteraction() {
		return lastInteraction;
	}
	
	public ByteBuffer getBuffer() {
		return buffer;
	}
	
	protected HttpRequest handleHttp(ByteBuffer buffer) throws HttpException {
		buffer.flip();
		HttpParser parser=getHttpParser();
		HttpRequest request=parser.parse();
		currentProtocolVersion=parser.getCurrentProtocolVersion();
		if(request!=null)
			parser.reset();
		buffer.compact();
		return request;
	}
	
	public HttpRequest processInput() throws SessionException, HttpException, LockException {
		lock();
		touch();
		try {
			return handleHttp(buffer);
		}
		finally {
			unlock();
		}
	}
	
	public SocketChannel getChannel() {
		return this.channel;
	}
	
	public HttpVersion getCurrentProtocolVersion() {
		return currentProtocolVersion;
	}
	
	public void lock() throws LockException {
		try {
			if(!lock.tryLock(LOCK_TIMEOUT, TimeUnit.SECONDS)) {
				throw new LockException(String.format("Lock timed out after %d seconds", LOCK_TIMEOUT));
			}
		} catch (InterruptedException e) {
			throw new LockException("Thread interrupted");
		}
	}
	
	public void unlock() {
		lock.unlock();
	}
	
	public void terminate() throws LockException {
		lock();
		if(channel!=null) {
			try {
				channel.close();
			}
			catch(IOException e) {
				//Ignore errors on close
				logger.log(LogLevel.WARNING, String.format("Failed to close channel for client %s", clientAddress));
			}
			if(observer!=null)
				observer.onTermination(this);
		}
		unlock();
	}
	
	private void setClientAddress() {		
		try {
			clientAddress=channel.getRemoteAddress();
		}
		catch(IOException e) {
			clientAddress=null;
		}
	}
	
	public SocketAddress getClientAddress() {
		return clientAddress;
	}
	
	public OutputBufferConsumer getOutputBufferConsumer() {
		return new ChannelOutputBufferConsumer(channel);
	}

}
