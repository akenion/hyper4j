package com.alexkenion.hyper4j.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.ReentrantLock;

import com.alexkenion.hyper4j.http.HttpException;
import com.alexkenion.hyper4j.http.HttpParser;
import com.alexkenion.hyper4j.http.HttpRequest;
import com.alexkenion.hyper4j.http.HttpVersion;

public class Session {
	
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
	
	public HttpRequest processInput() throws HttpException {
		lock();
		touch();
		buffer.flip();
		HttpRequest request=parser.parse();
		currentProtocolVersion=parser.getCurrentProtocolVersion();
		buffer.compact();
		unlock();
		return request;
	}
	
	public SocketChannel getChannel() {
		return this.channel;
	}
	
	public HttpVersion getCurrentProtocolVersion() {
		return currentProtocolVersion;
	}
	
	public void lock() {
		lock.lock();
	}
	
	public void unlock() {
		lock.unlock();
	}
	
	public void terminate() {
		lock();
		if(channel!=null) {
			try {
				channel.close();
			}
			catch(IOException e) {
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
