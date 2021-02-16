package com.alexkenion.hyper4j;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import com.alexkenion.hyper4j.util.BufferUtil;

public class HttpParser {

	private enum State {
		AWAITING_REQUEST_LINE,
		READING_HEADERS,
		READING_BODY,
		RECEIVED
	}

	private static final byte[] CRLF=new byte[]{13, 10};
	private static final byte[] COLON=new byte[]{58};
	private static final String SPACE=" ";
	private static final Charset CHARSET_ASCII=Charset.forName("ASCII");
	private static final CharsetDecoder ASCII_DECODER=CHARSET_ASCII.newDecoder();
	private static final String HEADER_HOST="Host";

	private ByteBuffer buffer;
	private State state;
	private boolean needsInput=false;
	private HttpVersion protocolVersion=null;
	private HttpRequest request;
	
	public HttpParser(ByteBuffer buffer) {
		this.buffer=buffer;
		reset();
	}
	
	public void reset() {
		state=State.AWAITING_REQUEST_LINE;
		needsInput=true;
		protocolVersion=null;
		request=null;
	}
	
	public boolean hasRequest() {
		return request!=null;
	}
	
	public HttpRequest getRequest() {
		return request;
	}
	
	public boolean needsInput() {
		return needsInput;
	}
	
	private boolean isBufferFull() {
		return buffer.limit()<buffer.capacity();
	}
	
	private boolean checkBufferLimit() throws HttpException {
		if(!isBufferFull()) {
			needsInput=true;
			return false;
		}
		return true;
	}
	
	private void parseRequestLine(String line) throws HttpException {
		String[] components=line.split(SPACE, 3);
		if(components.length!=3)
			throw new HttpException("Invalid request line: "+line);
		protocolVersion=new HttpVersion(components[2]);
		if(protocolVersion.getMajor()!=1) {
			throw new HttpException("Unsupported protocol version: "+protocolVersion);
		}
		System.out.println("Protocol Version: "+protocolVersion);
		request=new HttpRequest(components[0], Url.parseRelative(components[1]));
	}
	
	private void parseRequestLine() throws HttpException {
		ByteBuffer requestLine=BufferUtil.readToDelimiter(buffer, CRLF);
		if(requestLine==null) {
			if(checkBufferLimit())
				throw new HttpException("Request line too long");
			return;
		}
		try {
			parseRequestLine(ASCII_DECODER.decode(requestLine).toString());
			state=State.READING_HEADERS;
		} catch (CharacterCodingException e) {
			throw new HttpException("Failed to parse request line", e);
		}
	}
	
	private void parseRequestHeaders() throws HttpException {
		ByteBuffer line=BufferUtil.readToDelimiter(buffer, CRLF);
		if(line==null) {
			if(checkBufferLimit())
				throw new HttpException("Request header too long");
			return;
		}
		if(line.remaining()==0) {
			System.out.println("Headers received");
			state=State.READING_BODY;
			return;
		}
		ByteBuffer nameBuffer=BufferUtil.readToDelimiter(line, COLON);
		if(nameBuffer==null||nameBuffer.remaining()==1)
			throw new HttpException("Malformed header: no name found");
		try {
			String name=ASCII_DECODER.decode(nameBuffer).toString();
			String value=ASCII_DECODER.decode(line).toString().trim();
			System.out.println("Read header "+name+" | "+value);
			request.setHeader(name, value);
			if(name.equalsIgnoreCase(HEADER_HOST)) {
				request.getUrl().setAuthority(value);
			}
		} catch (CharacterCodingException e) {
			throw new HttpException("Malformed header: character encoding issue", e);
		}
	}
	
	private void readBody() {
		state=State.RECEIVED;
	}

	/**
	 * Attempt to parse the contents of the buffer as an HttpRequest
	 * @return the parsed request or null if the request is incomplete
	 * @throws HttpException if the request is malformed
	 * 
	 * For incomplete requests, the currently parsed state is maintained,
	 * allowing additional data to be added to the buffer and parsing to
	 * continue.
	 */
	public HttpRequest parse() throws HttpException {		
		needsInput=false;
		try {
			while(state!=State.RECEIVED&&buffer.hasRemaining()&&!needsInput) {
				System.out.println("Loop");
				switch(state) {
				case AWAITING_REQUEST_LINE:
					System.out.println("Awaiting request line");
					parseRequestLine();
					break;
				case READING_HEADERS:
					System.out.println("Reading headers");
					parseRequestHeaders();
					break;
				case READING_BODY:
					System.out.println("Reading body");
					readBody();
					break;
				default:
					System.out.println("Default");
					break;
				}
				System.out.println("End Loop: "+state.toString());
				System.out.println("Has Remaining? "+buffer.hasRemaining());
			}
		}
		catch(HttpException e) {
			System.out.println("HTTP Exception");
			System.err.println("Caught exception "+e.getMessage());
			reset();
			throw e;
		}
		System.out.println("End parsing: "+(request==null));
		return request;
	}

}