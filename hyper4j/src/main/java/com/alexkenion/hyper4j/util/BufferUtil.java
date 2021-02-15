package com.alexkenion.hyper4j.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class BufferUtil {

	public static ByteBuffer readToDelimiter(ByteBuffer buffer, byte[] delimiter) {
		int target=0;
		for(int i=buffer.position();i<buffer.limit();i++) {
			if(buffer.get(i)==delimiter[target]) {
				if(++target==delimiter.length) {
					ByteBuffer read=buffer.slice();
					read.limit(i-delimiter.length+1-buffer.position());
					buffer.position(Math.min(i+1, buffer.limit()));
					return read;
				}
			}
			else if(target>0) {
				target=0;
			}
		}
		return null;
	}
	
	public static void printBuffer(ByteBuffer buffer) {
		int count=0;
		for(int i=buffer.position();i<buffer.limit();i++) {
			System.out.print(buffer.get(i));
			System.out.print(' ');
			count++;
			if(count==10) {
				System.out.println();
				count=0;
			}
		}
	}
	
	public static void printBufferAscii(ByteBuffer buffer) {
		int count=0;
		CharsetDecoder decoder=Charset.forName("ASCII").newDecoder();
		try {
			CharBuffer charBuffer=decoder.decode(buffer);
			for(int i=charBuffer.position();i<charBuffer.limit();i++) {
				System.out.print(Integer.valueOf(charBuffer.get(i)));
				System.out.print(' ');
				count++;
				if(count==10) {
					System.out.println();
					count=0;
				}
			}
		} catch (CharacterCodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}