package com.alexkenion.hyper4j.tls;

public class TlsSettings {
	
	private String protocol;
	
	public TlsSettings(String protocol) {
		this.protocol=protocol;
	}
	
	public String getProtocol() {
		return protocol;
	}

}
