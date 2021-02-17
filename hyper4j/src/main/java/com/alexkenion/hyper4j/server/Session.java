package com.alexkenion.hyper4j.server;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.ReentrantLock;

import com.alexkenion.hyper4j.http.HttpException;
import com.alexkenion.hyper4j.http.HttpParser;
import com.alexkenion.hyper4j.http.HttpRequest;
import com.alexkenion.hyper4j.http.HttpVersion;

public class Session {
	
	private SocketChannel channel;
	private ReentrantLock lock;
	private ByteBuffer buffer;
	private HttpParser parser;
	private HttpVersion currentProtocolVersion;
	
	public Session(HttpServerSettings settings, SocketChannel channel) {
		this.channel=channel;
		this.lock=new ReentrantLock();
		buffer=ByteBuffer.allocate(settings.getBufferSize());
		this.parser=new HttpParser(buffer);
	}
	
	public ByteBuffer getBuffer() {
		return buffer;
	}
	
	public HttpRequest processInput() throws HttpException {
		lock.lock();
		buffer.flip();
		HttpRequest request=parser.parse();
		System.out.println("Parsed request: "+request);
		currentProtocolVersion=parser.getCurrentProtocolVersion();
		buffer.compact();
		lock.unlock();
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

}
