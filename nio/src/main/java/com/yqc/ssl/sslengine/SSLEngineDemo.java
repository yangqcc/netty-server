package com.yqc.ssl.sslengine;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-12-26
 * @modified By yangqc
 */
public class SSLEngineDemo {

    private SSLEngine sslEngine;
    private ByteBuffer serverOut;
    private ByteBuffer serverIn;
    private SSLContext sslContext;

    private static String keyStoreFile = "server.keystore";

    public SSLEngineDemo() {
        try {
            KeyStore keyStore = KeyStore.getInstance("jks");
            char[] passWorld = "123456".toCharArray();
            String userHome = System.getProperty("user.name");
            File file = new File(userHome + "/" + keyStoreFile);
            keyStore.load(new FileInputStream(file), passWorld);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("X.509");
            keyManagerFactory.init(keyStore, passWorld);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X.509");
            trustManagerFactory.init(keyStore);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            this.sslContext = sslContext;
        } catch (NoSuchAlgorithmException | CertificateException | KeyStoreException | UnrecoverableKeyException | IOException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    private void createSSLEngines() {
        sslEngine = sslContext.createSSLEngine();
        sslEngine.setUseClientMode(false);
        sslEngine.setNeedClientAuth(true);
    }
}
