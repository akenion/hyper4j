package com.alexkenion.hyper4j.util;

import java.nio.ByteBuffer;

public class BufferUtil {

	public static ByteBuffer readToDelimiter(ByteBuffer buffer, byte[] delimiter) {
		int target=0;
		for(int i=buffer.position();i<buffer.limit();i++) {
			if(buffer.get(i)==delimiter[target]) {
				if(++target==delimiter.length) {
					ByteBuffer read=buffer.slice();
					read.limit(i-delimiter.length+1);
					buffer.position(Math.min(i+delimiter.length-1, buffer.limit()));
					return read;
				}
			}
		}
		return null;
	}
	
}