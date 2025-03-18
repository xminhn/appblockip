package com.example.blockweb.utils;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.SecureRandom;

public class SSLSocketFactoryImpl extends SSLSocketFactory {
    private SSLSocketFactory delegate;

    public SSLSocketFactoryImpl() {
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new X509TrustManagerImpl()}, new SecureRandom());
            delegate = sc.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return delegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return delegate.getSupportedCipherSuites();
    }

    @Override
    public java.net.Socket createSocket(java.net.Socket s, String host, int port, boolean autoClose) throws java.io.IOException {
        return delegate.createSocket(s, host, port, autoClose);
    }

    @Override
    public java.net.Socket createSocket(String host, int port) throws java.io.IOException {
        return delegate.createSocket(host, port);
    }

    @Override
    public java.net.Socket createSocket(String host, int port, java.net.InetAddress localHost, int localPort) throws java.io.IOException {
        return delegate.createSocket(host, port, localHost, localPort);
    }

    @Override
    public java.net.Socket createSocket(java.net.InetAddress host, int port) throws java.io.IOException {
        return delegate.createSocket(host, port);
    }

    @Override
    public java.net.Socket createSocket(java.net.InetAddress address, int port, java.net.InetAddress localAddress, int localPort) throws java.io.IOException {
        return delegate.createSocket(address, port, localAddress, localPort);
    }
}