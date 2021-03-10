package com.alexkenion.hyper4j.server;

import java.nio.channels.SocketChannel;

public interface SessionManager {
	
	public Session createSession(SocketChannel client);

}
