package com.alexkenion.hyper4j;

public class ListenerThread extends Thread {
	
	private Listener listener;
	
	public ListenerThread(Listener listener) {
		this.listener=listener;
	}
	
	public void run() {
		listener.listen();
	}

}
