package com.alexkenion.hyper4j.tls;

import java.nio.ByteBuffer;

public interface TransportLayer {

	public int getBufferSize();
	public void send(ByteBuffer data) throws TlsException;

}
