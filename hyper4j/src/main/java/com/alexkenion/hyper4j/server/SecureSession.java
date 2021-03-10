package com.alexkenion.hyper4j.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.alexkenion.hyper4j.http.HttpException;
import com.alexkenion.hyper4j.http.HttpParser;
import com.alexkenion.hyper4j.http.HttpRequest;
import com.alexkenion.hyper4j.http.OutputBufferConsumer;
import com.alexkenion.hyper4j.tls.SecureContext;
import com.alexkenion.hyper4j.tls.TlsException;
import com.alexkenion.hyper4j.tls.TlsSettings;
import com.alexkenion.hyper4j.tls.TransportLayer;

public class SecureSession extends Session implements TransportLayer {
	
	private SecureContext secureContext;

	public SecureSession(ServerSettings settings, SocketChannel channel) throws TlsException {
		super(settings, channel);
		secureContext=new SecureContext(new TlsSettings("TLSv1.2"), this);
		System.out.println("Processing new TLS data");
		secureContext.processData(true);
		this.parser=new HttpParser(secureContext.getInputBuffer());
	}

	@Override
	public HttpRequest processInput() throws HttpException, LockException {
		System.out.println("Processing TLS input");
		lock();
		touch();
		try {
			secureContext.pushData(this.getBuffer());
			secureContext.processData(false);
			ByteBuffer in=secureContext.getInputBuffer();
			System.out.println("Input bytes: "+in.remaining());
			//try {
			//	CharBuffer decoded=Charset.forName("ASCII").newDecoder().decode(in);
			//	System.out.println(decoded);
			//} catch (CharacterCodingException e) {
			//	// TODO Auto-generated catch block
			//	e.printStackTrace();
			//}
			HttpRequest request=handleHttp(secureContext.getInputBuffer());
			return request;
		}
		catch (HttpException e) {
			throw e;
		}
		catch (TlsException e) {
			e.printStackTrace();
			return null;
		}
		finally {
			unlock();
		}
	}

	@Override
	public void send(ByteBuffer data) {
		try {
			System.out.println("Sending "+data.remaining()+" bytes");
			getChannel().write(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void receive(ByteBuffer data) {
	}
	
	@Override
	public OutputBufferConsumer getOutputBufferConsumer() {
		return this.secureContext;
	}

}
