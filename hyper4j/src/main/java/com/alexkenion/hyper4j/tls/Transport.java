package com.alexkenion.hyper4j.tls;

import java.nio.ByteBuffer;

public interface Transport {

	public int getBufferSize();
	public void send(ByteBuffer data) throws TlsException;

}
