package com.alexkenion.hyper4j.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.alexkenion.hyper4j.http.HttpException;
import com.alexkenion.hyper4j.http.HttpParser;
import com.alexkenion.hyper4j.http.HttpRequest;
import com.alexkenion.hyper4j.http.OutputBufferConsumer;
import com.alexkenion.hyper4j.logging.LogLevel;
import com.alexkenion.hyper4j.logging.Logger;
import com.alexkenion.hyper4j.tls.SecureContext;
import com.alexkenion.hyper4j.tls.TlsException;
import com.alexkenion.hyper4j.tls.TlsSettings;
import com.alexkenion.hyper4j.tls.Transport;

public class SecureSession extends Session implements Transport {
	
	private SecureContext secureContext;

	public SecureSession(ServerSettings settings, SocketChannel channel, TlsSettings tlsSettings, Logger logger) throws TlsException {
		super(settings, channel, logger);
		secureContext=new SecureContext(tlsSettings, this, logger);
		secureContext.startHandshake();
	}
	
	protected HttpParser initializeHttpParser() {
		return new HttpParser(secureContext.getInputBuffer());
	}

	@Override
	public HttpRequest processInput() throws SessionException, HttpException, LockException {
		lock();
		touch();
		try {
			secureContext.pushData(this.getBuffer());
			secureContext.processData();
			return handleHttp(secureContext.getInputBuffer());
		}
		catch (TlsException e) {
			throw new SessionException("Failed to process TLS input", e);
		}
		finally {
			unlock();
		}
	}

	@Override
	public void send(ByteBuffer data) {
		try {
			int dataSize=data.remaining();
			System.out.println("Writing "+dataSize+" bytes");
			int written=getChannel().write(data);
			System.out.println("Wrote: "+written+" of "+dataSize+" bytes");
			if(written!=dataSize) {
				logger.log(LogLevel.WARNING, "Failed to write "+(dataSize-written)+" bytes");
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.log(LogLevel.ERROR, String.format("Failed to send TLS encrypted data to client: %s", getClientAddress()));
		}
	}
	
	@Override
	public OutputBufferConsumer getOutputBufferConsumer() {
		return this.secureContext;
	}

	@Override
	public int getBufferSize() {
		return settings.getBufferSize();
	}

}
