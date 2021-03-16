package com.alexkenion.hyper4j.tls;

import java.security.KeyStore;

public interface KeyStoreProvider {
	
	public KeyStore loadKeyStore() throws TlsException;
	public char[] getPassword();

}
