package com.alexkenion.hyper4j;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.concurrent.locks.ReentrantLock;

import com.alexkenion.hyper4j.util.BufferUtil;

public class Session {
	
	private SocketChannel channel;
	private ReentrantLock lock;
	private ByteBuffer buffer;
	private HttpParser parser;
	
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
		buffer.compact();
		lock.unlock();
		return request;
	}
	
	public SocketChannel getChannel() {
		return this.channel;
	}
	
	public void lock() {
		lock.lock();
	}
	
	public void unlock() {
		lock.unlock();
	}

}
