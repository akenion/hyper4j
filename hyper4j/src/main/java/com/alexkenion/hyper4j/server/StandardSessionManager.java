package com.alexkenion.hyper4j.server;

import java.nio.channels.SocketChannel;

public class StandardSessionManager implements SessionManager{

	private ServerSettings settings;
	
	public StandardSessionManager(ServerSettings settings) {
		this.settings=settings;
	}
	
	@Override
	public Session createSession(SocketChannel client) {
		return new Session(settings, client);
	}

}
