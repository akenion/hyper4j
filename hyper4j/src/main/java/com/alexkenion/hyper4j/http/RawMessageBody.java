package com.alexkenion.hyper4j.http;

import java.nio.ByteBuffer;

public class RawMessageBody implements MessageBody {
	
	private int size;
	private ByteBuffer data;
	
	public RawMessageBody(int size) {
		this.size=size;
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
		return data;
	}
	
	public boolean isFull() {
		return !data.hasRemaining();
	}
	
	@Override
	public int getLength() {
		return size;
	}

}
