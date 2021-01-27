package com.alexkenion.hyper4j;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.concurrent.locks.ReentrantLock;

public class Session {
	
	private enum State {
		INITIAL,
		READING_HEADERS,
		READING_BODY,
		RECEIVED,
		INVALID
	}
	
	private static int BUFFER_SIZE=1024;

	private SocketChannel channel;
	private CharsetDecoder decoder;
	private ByteBuffer buffer;
	private StringBuilder line;
	private State state;
	private Request request=null;
	private ReentrantLock lock;
	
	public Session(SocketChannel channel) {
		this.lock=new ReentrantLock();
		this.channel=channel;
		buffer=ByteBuffer.allocate(BUFFER_SIZE);
		Charset charset=Charset.forName("ISO-8859-1");
		this.decoder=charset.newDecoder();
		this.state=State.INITIAL;
	}
	
	public ByteBuffer getBuffer() {
		return buffer;
	}
	
	private void parseRequestLine(String line) throws HttpException {
		String[] components=line.split(" ", 3);
		if(components.length!=3)
			throw new HttpException("Invalid request line: "+line);
		String version=components[2];
		if(version.equals("HTTP/1.1")) {
			throw new HttpException("Unsupported protocol version: "+version);
		}
		System.out.println("Protocol: "+version);
		request=new Request(components[0], components[1]);
		System.out.println("Request for "+request.getUrl()+" with method "+request.getMethod());
	}
	
	private void parseHeader(String line) throws HttpException {
		String[] components=line.split(": ", 2);
		if(components.length!=2)
			throw new HttpException("Invalid header line: "+line);
		request.setHeader(components[0], components[1]);
		System.out.println("Header: "+line);
	}
	
	private void handleLine(String line) throws HttpException {
		switch(state) {
		case INITIAL:
			parseRequestLine(line);
			state=State.READING_HEADERS;
			System.out.println("Switching state to: "+state.toString());
			break;
		case READING_HEADERS:
			if(line.trim().isEmpty()) {
				if(request.hasBody()) {
					state=State.READING_BODY;
				}
				else {
					state=State.RECEIVED;
				}
				System.out.println("Switching state to: "+state.toString());
			}
			else {
				parseHeader(line);
			}
			break;
		case READING_BODY:
			break;
		default:
			break;
		}
	}
	
	public Request processInput() {
		lock.lock();
		try {
			int position=buffer.position();
			buffer.flip();
			CharBuffer charBuffer=decoder.decode(buffer);
			char[] chars=charBuffer.array();
			int i=0;
			int start=0, end=0;
			for(;i<charBuffer.limit();i++) {
				if(charBuffer.get(i)=='\n') {
					String line=charBuffer.subSequence(start, i).toString();
					System.out.println("Read line: "+line);
					try {
						this.handleLine(line);
					}
					catch(HttpException e) {
						e.printStackTrace();
					}
					start=i+1;
					end=i;
				}
			}
			buffer.position(Math.min(end, buffer.capacity()));
			buffer.compact();
		} catch (CharacterCodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
