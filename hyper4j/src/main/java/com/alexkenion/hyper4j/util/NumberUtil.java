package com.alexkenion.hyper4j.util;

public class NumberUtil {
	
	public static final long MILLISECONDS_PER_SECOND = 1000;
	
	public static long millisecondsToSeconds(long milliseconds) {
		return milliseconds / MILLISECONDS_PER_SECOND;
	}
	
	public static long secondsToMilliseconds(long seconds) {
		return seconds * MILLISECONDS_PER_SECOND;
	}
	
	public static long secondsToMilliseconds(int seconds) {
		return secondsToMilliseconds((long)seconds);
	}

}
