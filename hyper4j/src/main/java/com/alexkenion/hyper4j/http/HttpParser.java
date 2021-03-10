package com.alexkenion.hyper4j.http;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;

import com.alexkenion.hyper4j.logging.LogLevel;
import com.alexkenion.hyper4j.logging.Logger;
import com.alexkenion.hyper4j.logging.NullLogger;
import com.alexkenion.hyper4j.util.BufferUtil;

public class HttpParser {

	private enum State {
		AWAITING_REQUEST_LINE,
		READING_HEADERS,
		READING_BODY,
		RECEIVED
	}

	private ByteBuffer buffer;
	private State state;
	private boolean needsInput=false;
	private HttpVersion protocolVersion=null;
	private HttpRequest request;
	private RawMessageBody body;
	private Logger logger;
	private CharsetDecoder asciiDecoder;
	
	public HttpParser(ByteBuffer buffer, Logger logger) {
		this.buffer=buffer;
		this.logger=logger;
		this.asciiDecoder=Http.ASCII.newDecoder();
		reset();
	}
	
	public HttpParser(ByteBuffer buffer) {
		this(buffer, new NullLogger());
	}
	
	public void reset() {
		state=State.AWAITING_REQUEST_LINE;
		needsInput=false;
		protocolVersion=null;
		request=null;
		body=null;
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
		return buffer.limit()==buffer.capacity();
	}
	
	private boolean checkBufferLimit() throws HttpException {
		System.out.println("Remaining in buffer: "+buffer.remaining()+", "+buffer.limit()+", "+buffer.capacity());
		if(!isBufferFull()) {
			System.out.println("Buffer not full");
			needsInput=true;
			return false;
		}
		System.out.println("Buffer full");
		return true;
	}
	
	private void parseRequestLine(String line) throws HttpException {
		String[] components=line.split(Http.SPACE, 3);
		if(components.length!=3)
			throw new HttpException("Invalid request line: "+line);
		protocolVersion=new HttpVersion(components[2]);
		if(protocolVersion.getMajor()!=1) {
			throw new HttpException("Unsupported protocol version: "+protocolVersion);
		}
		request=new HttpRequest(components[0], Url.parseRelative(components[1]));
	}
	
	private void parseRequestLine() throws HttpException {
		ByteBuffer requestLine=BufferUtil.readToDelimiter(buffer, Http.Delimiter.CRLF.getBytes());
		if(requestLine==null) {
			if(checkBufferLimit())
				throw new HttpException("Request line too long");
			return;
		}
		try {
			parseRequestLine(asciiDecoder.decode(requestLine).toString());
			state=State.READING_HEADERS;
		} catch (CharacterCodingException e) {
			throw new HttpException("Failed to parse request line", e);
		}
	}
	
	private void parseRequestHeaders() throws HttpException {
		ByteBuffer line=BufferUtil.readToDelimiter(buffer, Http.Delimiter.CRLF.getBytes());
		if(line==null) {
			if(checkBufferLimit())
				throw new HttpException("Request header too long");
			return;
		}
		if(line.remaining()==0) {
			logger.log(LogLevel.DEBUG, "Headers received");
			state=request.hasBody()?State.READING_BODY:State.RECEIVED;
			return;
		}
		ByteBuffer nameBuffer=BufferUtil.readToDelimiter(line, Http.Delimiter.COLON.getBytes());
		if(nameBuffer==null||nameBuffer.remaining()==1)
			throw new HttpException("Malformed header: no name found");
		try {
			String name=asciiDecoder.decode(nameBuffer).toString();
			String value=asciiDecoder.decode(line).toString().trim();
			logger.log(LogLevel.DEBUG, String.format("Read header %s: %s", name, value));
			request.setHeader(name, value);
			if(name.equalsIgnoreCase(Http.HEADER_HOST)) {
				request.getUrl().setAuthority(value);
			}
		} catch (Exception e) {
			throw new HttpException("Malformed header: character encoding issue", e);
		}
	}
	
	private void readBody() {
		if(request.hasBody()) {
			if(body==null)
				body=new RawMessageBody(request.getContentLength());
			body.append(buffer);
			if(body.isFull()) {
				System.out.println("Read "+request.getContentLength()+" bytes of body data");
				try {
					ByteBuffer bodyData=body.getData();
					bodyData.flip();
					String bodyString=asciiDecoder.decode(bodyData).toString();
					request.setBody(bodyString);
					System.out.println("Read body: "+bodyString);
				} catch (CharacterCodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				state=State.RECEIVED;
			}
		}
		else {
			state=State.RECEIVED;
		}
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
				switch(state) {
				case AWAITING_REQUEST_LINE:
					parseRequestLine();
					break;
				case READING_HEADERS:
					parseRequestHeaders();
					break;
				case READING_BODY:
					readBody();
					break;
				default:
					break;
				}
			}
		}
		catch(HttpException e) {
			logger.log(LogLevel.ERROR, String.format("HTTP parsing failed, caught: %s", e.getMessage()));
			reset();
			throw e;
		}
		if(state==State.RECEIVED)
			return request;
		return null;
	}
	
	public HttpVersion getCurrentProtocolVersion() {
		return protocolVersion;
	}

}