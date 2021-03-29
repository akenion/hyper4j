package com.alexkenion.hyper4j.http;

import java.nio.ByteBuffer;

public class EmptyMessageBody implements MessageBody {

	@Override
	public ByteBuffer getData() {
		return ByteBuffer.allocate(0);
	}

	@Override
	public int getLength() {
		return 0;
	}

}
