package com.alexkenion.hyper4j.tls;

public class TlsSettings {
	
	private String protocol;
	private KeyStoreProvider keyStoreProvider;
	
	public TlsSettings(String protocol, KeyStoreProvider keyStoreProvider) {
		this.protocol=protocol;
		this.keyStoreProvider=keyStoreProvider;
	}
	
	public String getProtocol() {
		return protocol;
	}
	
	public KeyStoreProvider getKeyStoreProvider() {
		return keyStoreProvider;
	}

}
