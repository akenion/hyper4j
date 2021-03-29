package com.alexkenion.hyper4j.http;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class StringMessageBody implements MessageBody {
	
	private String data;
	private Charset charset;
	
	public StringMessageBody(String data, Charset charset) {
		this.data=data;
		this.charset=charset;
	}
	
	public StringMessageBody(String data) {
		this(data, Charset.forName("UTF-8"));
	}

	@Override
	public ByteBuffer getData() {
		return this.charset.encode(data);
	}

	@Override
	public int getLength() {
		return this.getData().capacity();
	}

}
