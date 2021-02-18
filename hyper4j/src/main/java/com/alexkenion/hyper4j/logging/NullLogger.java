package com.alexkenion.hyper4j.logging;

/**
 * i.e. /dev/null
 * @author Alex Kenion
 */
public class NullLogger implements Logger {

	@Override
	public void log(LogLevel level, String message) {
		//Silently discard messages
	}

}
