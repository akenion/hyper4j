package com.alexkenion.hyper4j.http;

import java.nio.channels.WritableByteChannel;

public class ChannelHttpResponseWriter extends BufferHttpResponseWriter {

	public ChannelHttpResponseWriter(WritableByteChannel channel, int bufferSize) {
		super(bufferSize, new ChannelOutputBufferConsumer(channel));
	}

}
