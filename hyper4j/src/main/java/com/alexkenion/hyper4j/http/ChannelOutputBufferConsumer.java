package com.alexkenion.hyper4j.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public class ChannelOutputBufferConsumer implements OutputBufferConsumer {

	private WritableByteChannel channel;
	
	public ChannelOutputBufferConsumer(WritableByteChannel channel) {
		this.channel=channel;
	}

	@Override
	public void consume(ByteBuffer buffer) throws IOException {
		buffer.flip();
		while(buffer.hasRemaining()) {
			channel.write(buffer);
		}
		buffer.clear();
	}

}
