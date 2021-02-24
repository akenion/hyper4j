package com.alexkenion.hyper4j.server;

import com.alexkenion.hyper4j.logging.LogLevel;
import com.alexkenion.hyper4j.logging.Logger;

public class MonitoredRunnable implements Runnable{
	
	private final Runnable runnable;
	private Logger logger;
	
	public MonitoredRunnable(Runnable runnable, Logger logger) {
		this.runnable=runnable;
		this.logger=logger;
	}

	@Override
	public void run() {
		try {
			runnable.run();
		}
		catch(Throwable t) {
			t.printStackTrace();
			System.err.println("Uncaught throwable: "+t.getMessage());
			//logger.log(LogLevel.ERROR, "Uncaught exception: "+t.getMessage());
		}
	}

}
