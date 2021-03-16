package com.alexkenion.hyper4j.tls;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class FileKeyStoreProvider implements KeyStoreProvider {
	
	private final String path, password;
	
	public FileKeyStoreProvider(String path, String password) {
		this.path=path;
		this.password=password;
	}

	@Override
	public KeyStore loadKeyStore() throws TlsException {
		try {
			KeyStore keyStore=KeyStore.getInstance("PKCS12");
			keyStore.load(new FileInputStream(path), password.toCharArray());
			return keyStore;
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new TlsException("Unable to retrieve key store", e);
		}
	}
	
	@Override
	public char[] getPassword() {
		return password.toCharArray();
	}

}
