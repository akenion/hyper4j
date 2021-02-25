package com.alexkenion.hyper4j.server;

public class SessionTerminator implements Runnable {
	
	private Server server;
	private Session session;
	
	public SessionTerminator(Server server, Session session) {
		this.server=server;
		this.session=session;
	}

	@Override
	public void run() {
		server.removeSession(session);
	}

}
