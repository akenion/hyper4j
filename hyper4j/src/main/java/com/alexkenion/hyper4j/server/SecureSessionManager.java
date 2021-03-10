package com.alexkenion.hyper4j.server;

import java.nio.channels.SocketChannel;

import com.alexkenion.hyper4j.tls.TlsException;
import com.alexkenion.hyper4j.tls.TlsSettings;

public class SecureSessionManager implements SessionManager{

	private ServerSettings serverSettings;
	private TlsSettings tlsSettings;
	
	public SecureSessionManager(ServerSettings serverSettings, TlsSettings tlsSettings) {
		this.serverSettings=serverSettings;
		this.tlsSettings=tlsSettings;
	}
	
	@Override
	public SecureSession createSession(SocketChannel client) {
		try {
			return new SecureSession(serverSettings, client);
		} catch (TlsException e) {
			return null;
		}
	}

}
