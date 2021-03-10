package com.alexkenion.hyper4j.tls;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

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
import com.alexkenion.hyper4j.server.ServerSettings;

public class SecureContext implements OutputBufferConsumer {
	
	private enum State {
		//
	}
	
	private final TlsSettings settings;
	private final TransportLayer transport;
	private SSLEngine engine;
	private SSLSession session;
	private ByteBuffer inputApp, inputNet, outputApp, outputNet;
	
	public SecureContext(TlsSettings settings, TransportLayer transport) throws TlsException {
		this.settings=settings;
		this.transport=transport;
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
			context.init(getKeyManagers(), getTrustManagers(), null);
		} catch (KeyManagementException e) {
			throw new TlsException("Failed to initialize SSL context", e);
		}
		engine=context.createSSLEngine();
		engine.setUseClientMode(false);
	}
	
	private void initializeSession() {
		session=engine.getSession();
		inputApp=ByteBuffer.allocate(session.getApplicationBufferSize());
		inputNet=ByteBuffer.allocate(Math.max(session.getPacketBufferSize(), ServerSettings.DEFAULT_BUFFER_SIZE));
		outputApp=ByteBuffer.allocate(session.getApplicationBufferSize());
		outputNet=ByteBuffer.allocate(session.getPacketBufferSize());
	}
	
	private KeyStore getKeyStore() throws TlsException {
		try {
			KeyStore keyStore=KeyStore.getInstance("PKCS12");
			char[] password="test".toCharArray();
			keyStore.load(new FileInputStream("/home/alex/hyper4j-test/keystore.pkcs12"), password);
			return keyStore;
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new TlsException("Unable to retrieve key store", e);
		}
	}
	
	private KeyManager[] getKeyManagers() throws TlsException {
		KeyStore keyStore=getKeyStore();
		try {
			KeyManagerFactory keyManagerFactory=KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(keyStore, "test".toCharArray());
			return keyManagerFactory.getKeyManagers();
		} catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
			throw new TlsException("Unable to retrieve key manager factory", e);
		}
	}
	
	private TrustManager[] getTrustManagers() throws TlsException {
		try {
			KeyStore keyStore=getKeyStore();
			TrustManagerFactory trustManagerFactory=TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init(keyStore);
			return trustManagerFactory.getTrustManagers();
		}
		catch(Exception e) {
			throw new TlsException("Unable to retrieve trust manager factory", e);
		}
	}
	
	private void wrap() throws SSLException {
		outputNet.clear();
		SSLEngineResult result=engine.wrap(outputApp, outputNet);
		if(result.getStatus()!=Status.OK) {
			System.err.println(result.getStatus()+" on wrap");
		}
		outputNet.flip();
		transport.send(outputNet);
	}
	
	private boolean unwrap() throws SSLException {
		inputNet.flip();
		SSLEngineResult result=engine.unwrap(inputNet, inputApp);
		inputNet.compact();
		if(result.getStatus()!=Status.OK) {
			System.err.println(result.getStatus()+" on unwrap");
			return false;
		}
		System.out.println(inputApp.remaining()+"/"+inputApp.capacity());
		//inputApp.flip();
		if(result.getStatus()==Status.BUFFER_OVERFLOW) {
			System.out.println("Underflow, need more data");
			return false;
		}
		else if(result.getStatus()==Status.OK) {
			System.out.println("Unwrap successful, need to process data...");
			return true;
		}
		System.out.println("Unwrap successful");
		return true;
	}
	
	public void performHandshake() throws SSLException {
		HandshakeStatus handshakeStatus;
		do {
			handshakeStatus=engine.getHandshakeStatus();
			System.out.println("Handshake Status: "+handshakeStatus);
			switch(handshakeStatus) {
			case NEED_UNWRAP:
				System.out.println("Performing unwrap");
				if(!unwrap())
					return;
				break;
			case NEED_WRAP:
				System.out.println("Performing wrap");
				wrap();
				break;
			case NEED_TASK:
				System.out.println("Performing task");
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
		System.out.println("Pushing data: "+data.remaining()+", "+inputNet.remaining()+"/"+inputNet.capacity());
		this.inputNet.put(data);
		data.compact();
	}
	
	public void processData(boolean initial) throws TlsException {
		try {
			if(initial) {
				inputNet.clear();
				engine.beginHandshake();
			}
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
		System.out.println("Wrapping "+buffer.remaining());
		outputApp.put(buffer);
		outputApp.flip();
		buffer.clear();
		wrap();
	}

}
