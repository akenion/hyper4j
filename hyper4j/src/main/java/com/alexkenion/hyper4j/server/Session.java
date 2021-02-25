package com.alexkenion.hyper4j.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.alexkenion.hyper4j.http.HttpException;
import com.alexkenion.hyper4j.http.HttpParser;
import com.alexkenion.hyper4j.http.HttpRequest;
import com.alexkenion.hyper4j.http.HttpVersion;

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
	
	public Session(ServerSettings settings, SocketChannel channel) {
		this.channel=channel;
		this.lock=new ReentrantLock();
		buffer=ByteBuffer.allocate(settings.getBufferSize());
		this.parser=new HttpParser(buffer);
		touch();
		this.setClientAddress();
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
	
	public HttpRequest processInput() throws HttpException, LockException {
		lock();
		try {
			touch();
			buffer.flip();
			HttpRequest request=parser.parse();
			currentProtocolVersion=parser.getCurrentProtocolVersion();
			if(request!=null)
				parser.reset();
			buffer.compact();
			unlock();
			return request;
		}
		catch(HttpException e) {
			unlock();
			throw e;
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
				System.err.println("Failed to close channel");
				e.printStackTrace();
				//Ignore errors on close
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

}
