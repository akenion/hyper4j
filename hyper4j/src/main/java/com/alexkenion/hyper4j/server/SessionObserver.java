package com.alexkenion.hyper4j.server;

public interface SessionObserver {
	
	/**
	 * Called when the session is terminated
	 * @param session
	 */
	public void onTermination(Session session);

}
