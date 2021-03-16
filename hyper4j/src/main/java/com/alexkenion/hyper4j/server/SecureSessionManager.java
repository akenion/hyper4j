package com.alexkenion.hyper4j.server;

import java.nio.channels.SocketChannel;

import com.alexkenion.hyper4j.logging.Logger;
import com.alexkenion.hyper4j.tls.TlsException;
import com.alexkenion.hyper4j.tls.TlsSettings;

public class SecureSessionManager implements SessionManager{

	private ServerSettings serverSettings;
	private TlsSettings tlsSettings;
	private Logger logger;
	
	public SecureSessionManager(ServerSettings serverSettings, TlsSettings tlsSettings, Logger logger) {
		this.serverSettings=serverSettings;
		this.tlsSettings=tlsSettings;
		this.logger=logger;
	}
	
	@Override
	public SecureSession createSession(SocketChannel client) {
		try {
			return new SecureSession(serverSettings, client, tlsSettings, logger);
		} catch (TlsException e) {
			return null;
		}
	}

}
