package com.alexkenion.hyper4j.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Iterator;

public class ChannelHttpResponseWriter implements HttpResponseWriter {
	
	private static enum State {
		WRITING_STATUS,
		WRITING_HEADERS,
		WRITING_BODY,
		BUFFERED;
	}
	
	private WritableByteChannel channel;
	private ByteBuffer buffer;
	
	public ChannelHttpResponseWriter(WritableByteChannel channel, int bufferSize) {
		this.channel=channel;
		this.buffer=ByteBuffer.allocate(bufferSize);
	}
	
	private void reset() {
		buffer.clear();
	}
	
	private void writeStatusLine(HttpVersion protocolVersion, HttpResponse response) {
		String statusLine=response.getStatusLine(protocolVersion);
		ByteBuffer encoded=Http.ASCII.encode(statusLine);
		buffer.put(encoded);
	}
	
	private boolean writeHeader(String key, String value) throws IOException {
		StringBuilder headerLine=new StringBuilder(key)
				.append(Http.HEADER_DELIMITER)
				.append(value)
				.append(Http.Delimiter.CRLF.getString());
		ByteBuffer encoded=Http.ASCII.encode(headerLine.toString());
		if(buffer.remaining()<encoded.remaining()) {
			if(encoded.remaining()>buffer.capacity())
				throw new IOException("Insufficient buffer capacity");
			return false;
		}
		else {
			buffer.put(encoded);
			return true;
		}
	}
	
	@Override
	public void write(HttpVersion protocolVersion, HttpResponse response) throws HttpException {
		State state=State.WRITING_STATUS;
		HttpHeaders headers=null;
		Iterator<String> headerKeys=null;
		String incompleteHeader=null;
		try {
			while(state!=State.BUFFERED) {
				boolean flush=false;
				while(state!=State.BUFFERED&&buffer.hasRemaining()&&!flush) {
					switch(state) {
					case WRITING_STATUS:
						writeStatusLine(protocolVersion, response);
						state=State.WRITING_HEADERS;
						headers=response.getHeaders();
						headerKeys=headers.keySet().iterator();
						break;
					case WRITING_HEADERS:
						String currentHeader;
						if(incompleteHeader!=null) {
							currentHeader=incompleteHeader;
						}
						else if(headerKeys.hasNext()) {
							currentHeader=headerKeys.next();
						}
						else {
							if(buffer.remaining()>=Http.Delimiter.CRLF.getSize()) {
								buffer.put(Http.Delimiter.CRLF.getBytes());
								state=State.WRITING_BODY;
							}
							else {
								flush=true;
							}
							break;
						}
						if(!writeHeader(currentHeader, headers.get(currentHeader))) {
							incompleteHeader=currentHeader;
							flush=true;
						}
						break;
					case WRITING_BODY:
						//TODO: Properly handle response body
						buffer.put(Http.ASCII.encode(response.getBody()));
						state=State.BUFFERED;
						break;
					default:
						break;
					}
				}
				buffer.flip();
				while(buffer.hasRemaining()) {
					channel.write(buffer);
				}
				buffer.clear();
			}
			reset();
		}
		catch(Exception e) {
			reset();
			throw new HttpException("Failed to write response", e);
		}
	}

}
