package com.alexkenion.hyper4j.http;

import java.nio.ByteBuffer;

public interface MessageBody {
	
	public ByteBuffer getData();
	public int getLength();

}
