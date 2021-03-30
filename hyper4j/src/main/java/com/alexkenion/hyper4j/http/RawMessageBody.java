package com.alexkenion.hyper4j.http;

import java.nio.ByteBuffer;

public class RawMessageBody implements MessageBody {
	
	private ByteBuffer data;
	
	public RawMessageBody(int size) {
		data=ByteBuffer.allocate(size);
	}
	
	public void append(ByteBuffer buffer) {
		if(buffer.hasRemaining()&&data.hasRemaining()) {
			if(buffer.remaining()>data.remaining()) {
				byte[] temp=new byte[data.remaining()];
				buffer.get(temp, 0, data.remaining());
				data.put(temp);
			}
			else {
				data.put(buffer);
			}
		}
	}

	@Override
	public ByteBuffer getData() {
		ByteBuffer buffer=data.duplicate();
		buffer.flip();
		return buffer;
	}
	
	public boolean isFull() {
		return !data.hasRemaining();
	}
	
	@Override
	public int getLength() {
		return data.capacity();
	}

}