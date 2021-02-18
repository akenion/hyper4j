package com.alexkenion.hyper4j.logging;

public class SystemLogger implements Logger {
	
	public void log(LogLevel level, String message) {
		if(level.compareTo(LogLevel.ERROR)>=0) {
			System.err.println(message);
		}
		else {
			System.out.println(message);
		}
	}

}
