package com.alexkenion.hyper4j.http;

public class HttpResponse extends HttpMessage {
	
	private short status;

	public HttpResponse(short status, HttpHeaders headers) {
		super(headers);
		this.status=status;
	}

	public HttpResponse(short status) {
		this(status, new HttpHeaders());
	}
	
	public HttpResponse(int status) {
		this((short)status);
	}
	
	public short getStatus() {
		return status;
	}
	
	public String getStatusLine(HttpVersion version, HttpStatusResolver statusResolver) {
		StringBuilder line=new StringBuilder(version.toString());
		line.append(Http.SPACE);
		line.append(status);
		line.append(Http.SPACE);
		line.append(statusResolver.getReasonPhrase(status));
		line.append(Http.Delimiter.CRLF.getString());
		return line.toString();
	}
	
	public String getStatusLine(HttpVersion version) {
		return getStatusLine(version, new HttpStatus.DefaultResolver());
	}
	
	@Override
	public void setBody(String body) {
		this.setHeader(Http.HEADER_CONTENT_LENGTH, ""+body.length());
		super.setBody(body);
	}

}
