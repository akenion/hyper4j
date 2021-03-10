package com.alexkenion.hyper4j.http;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface OutputBufferConsumer {
	
	/**
	 * @param buffer
	 */
	public void consume(ByteBuffer buffer) throws IOException;

}
