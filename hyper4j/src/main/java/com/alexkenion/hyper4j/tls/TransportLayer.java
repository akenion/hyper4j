package com.alexkenion.hyper4j.tls;

import java.nio.ByteBuffer;

public interface TransportLayer {
	
	public void send(ByteBuffer data);
	public void receive(ByteBuffer data);

}
