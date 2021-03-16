package com.alexkenion.hyper4j.server;

import java.nio.channels.SocketChannel;

import com.alexkenion.hyper4j.logging.Logger;

public class StandardSessionManager implements SessionManager{

	private ServerSettings settings;
	private Logger logger;
	
	public StandardSessionManager(ServerSettings settings, Logger logger) {
		this.settings=settings;
	}
	
	@Override
	public Session createSession(SocketChannel client) {
		return new Session(settings, client, logger);
	}

}
