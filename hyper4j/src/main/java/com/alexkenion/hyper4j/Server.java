package com.alexkenion.hyper4j;

import java.util.ArrayList;
import java.util.List;

public class Server {
	
	private List<Listener> listeners=new ArrayList<Listener>();
	private boolean serving=false;
	
	public Server() {
	}
	
	public Server addListener(Listener listener) {
		this.listeners.add(listener);
		return this;
	}
	
	public void serve() {
		System.out.println("Serving...");
		serving=true;
		List<ListenerThread> threads=new ArrayList<ListenerThread>();
		for(Listener listener:listeners) {
			ListenerThread thread=new ListenerThread(listener);
			thread.start();
			threads.add(thread);
		}
		while(serving) {
			serving=false;
			for(ListenerThread thread:threads) {
				if(thread.isAlive())
					serving=true;
			}
			try {
				this.wait();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Terminating...");
	}

}
