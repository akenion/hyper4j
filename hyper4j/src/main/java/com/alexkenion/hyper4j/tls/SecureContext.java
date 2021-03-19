package com.alexkenion.hyper4j.tls;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import com.alexkenion.hyper4j.http.OutputBufferConsumer;
import com.alexkenion.hyper4j.logging.LogLevel;
import com.alexkenion.hyper4j.logging.Logger;
import com.alexkenion.hyper4j.server.ServerSettings;

public class SecureContext implements OutputBufferConsumer {
	
	private final TlsSettings settings;
	private final Transport transport;
	private final Logger logger;
	private KeyStore keyStore;
	private SSLEngine engine;
	private SSLSession session;
	private ByteBuffer inputApp, inputNet, outputApp, outputNet;
	
	public SecureContext(TlsSettings settings, Transport transport, Logger logger) throws TlsException {
		this.settings=settings;
		this.transport=transport;
		this.logger=logger;
		this.keyStore=settings.getKeyStoreProvider().loadKeyStore();
		initializeEngine();
		initializeSession();
	}
	
	private void initializeEngine() throws TlsException {
		SSLContext context;
		try {
			context = SSLContext.getInstance(settings.getProtocol());
		} catch (NoSuchAlgorithmException e) {
			throw new TlsException("Failed to get SSL context", e);
		}
		try {
			context.init(getKeyManagers(), null,/*getTrustManagers(),*/ null);
		} catch (KeyManagementException e) {
			throw new TlsException("Failed to initialize SSL context", e);
		}
		engine=context.createSSLEngine();
		engine.setUseClientMode(false);
	}
	
	private void initializeSession() {
		session=engine.getSession();
		inputApp=ByteBuffer.allocate(session.getApplicationBufferSize());
		inputNet=ByteBuffer.allocate(Math.max(session.getPacketBufferSize(), transport.getBufferSize()));
		outputApp=ByteBuffer.allocate(Math.max(session.getApplicationBufferSize(), transport.getBufferSize()));
		System.out.println(String.format("Initialized outputApp. %d, %d, %d", outputApp.position(), outputApp.limit(), outputApp.capacity()));
		outputNet=ByteBuffer.allocate(session.getPacketBufferSize());
	}

	private KeyManager[] getKeyManagers() throws TlsException {
		try {
			KeyManagerFactory keyManagerFactory=KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(keyStore, settings.getKeyStoreProvider().getPassword());
			return keyManagerFactory.getKeyManagers();
		} catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
			throw new TlsException("Unable to retrieve key manager factory", e);
		}
	}
	
	private void wrap() throws SSLException, TlsException {
		outputNet.clear();
		outputApp.flip();
		SSLEngineResult result=engine.wrap(outputApp, outputNet);
		if(result.getStatus()==Status.OK) {
			outputApp.clear();
			outputNet.flip();
			transport.send(outputNet);
		}
		else {
			logger.log(LogLevel.WARNING, "TLS wrap failed: "+result.getStatus());
		}
	}
	
	private boolean unwrap() throws SSLException{
		return unwrap(true);
	}
	
	private boolean unwrap(boolean all) throws SSLException {
		inputNet.flip();
		boolean underflow=false, closed=false;
		do {
			System.out.println("Unwrapping...");
			SSLEngineResult result=engine.unwrap(inputNet, inputApp);
			System.out.println("SSLEngineResult: "+result.getStatus()+", "+result.bytesConsumed()+" | "+result.bytesProduced());
			Status status=result.getStatus();
			switch(status) {
			case OK:
				logger.log(LogLevel.DEBUG, "TLS unwrap succeeded, buffered data: "+result.bytesProduced());
				break;
			case CLOSED:
				logger.log(LogLevel.DEBUG, "TLS connection closed");
				closed=true;
				break;
			case BUFFER_UNDERFLOW:			
				logger.log(LogLevel.DEBUG, "TLS unwrap buffer underflow, awaiting more data");
				underflow=true;
				break;
			default:
				logger.log(LogLevel.WARNING, "TLS unwrap failed: "+status);
				break;
			}
			if(!all)
				break;
		} while(inputNet.hasRemaining()&&!underflow&&!closed&&all);
		inputNet.compact();
		return !underflow;
	}
	
	public void performHandshake() throws SSLException, TlsException {
		HandshakeStatus handshakeStatus;
		do {
			handshakeStatus=engine.getHandshakeStatus();
			switch(handshakeStatus) {
			case NEED_UNWRAP:
				if(!unwrap(false))
					return;
				break;
			case NEED_WRAP:
				wrap();
				break;
			case NEED_TASK:
				Runnable task=engine.getDelegatedTask();
				task.run();
				break;
			case FINISHED:
				break;
			default:
				return;
			}
		} while(handshakeStatus!=HandshakeStatus.FINISHED && handshakeStatus!=HandshakeStatus.NOT_HANDSHAKING);
	}
	
	public ByteBuffer getInputBuffer() {
		return inputApp;
	}
	
	public ByteBuffer getOutputBuffer() {
		return outputApp;
	}
	
	public void pushData(ByteBuffer data) {
		data.flip();
		this.inputNet.put(data);
		data.compact();
	}
	
	public void startHandshake() throws TlsException {
		try {
			inputNet.clear();
			engine.beginHandshake();
		}
		catch(Exception e) {
			throw new TlsException("Failed to start TLS handshake", e);
		}
	}
	
	public void processData() throws TlsException {
		try {
			performHandshake();
			unwrap();
		}
		catch(Exception e) {
			throw new TlsException("Exception occurred while processing data", e);
		}
	}

	@Override
	public void consume(ByteBuffer buffer) throws IOException {
		buffer.flip();
		System.out.println(String.format(
				"Consuming buffer with %d bytes remaining. Overall length: %d, limit: %d. Destination: %d, %d, %d",
				buffer.remaining(), buffer.capacity(), buffer.limit(),
				outputApp.remaining(), outputApp.capacity(), outputApp.limit()
			)
		);
		if(buffer.remaining()>outputApp.remaining()) {
			System.err.println("Buffer overflow");
			buffer=buffer.slice();
			int offset=buffer.position()-(buffer.remaining()-outputApp.remaining());
			if(offset<=0) {
				System.err.println("Negative offset: "+offset);
				buffer.compact();
				return;
			}
			buffer.limit(offset);
		}
		outputApp.put(buffer);
		buffer.compact();
		try {
			wrap();
		}
		catch(TlsException e) {
			throw new IOException("Failed to send output", e);
		}
	}

}
