package com.yqc.ssl;

import org.junit.jupiter.api.Test;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * @author yangqc
 */
public class SSLContextTest {

    @Test
    public void test1() throws NoSuchAlgorithmException, KeyManagementException {
        //TrustManager的子接口，管理X509证书，验证远程安全套接字
        X509TrustManager x509TrustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        //获取一个SSLContext实例
        SSLContext sslContext = SSLContext.getInstance("SSL");
        //初始化SSLContext实例
        sslContext.init(null, new TrustManager[]{x509TrustManager}, new SecureRandom());
        System.out.println("缺省安全套接字使用协议:" + sslContext.getProtocol());
        //获取SSLContext实例相关的SSLEngine
        SSLEngine e = sslContext.createSSLEngine();
        System.out.println("支持的协议:" + Arrays.asList(e.getSupportedProtocols()));
        System.out.println("启用协议:" + Arrays.asList(e.getEnabledProtocols()));
        System.out.println("支持加密套件:" + Arrays.asList(e.getSupportedCipherSuites()));
        System.out.println("启用加密套件:" + Arrays.asList(e.getEnabledCipherSuites()));
    }

    @Test
    public void sslSocketServer() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, KeyManagementException {
        String keyName = "cmkey";
        char[] keyStorePwd = "123456".toCharArray();
        char[] keyPwd = "123456".toCharArray();
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        //装载当前目录下的key store,可用jdk中的keystore工具生成keystore
        InputStream inputStream = null;
        keyStore.load(SSLContextTest.class.getClassLoader().getResourceAsStream(keyName), keyPwd);
        inputStream.close();
        //初始化key manager factory
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keyPwd);
        //初始化ssl context
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(keyManagerFactory.getKeyManagers(), new TrustManager[]{new MyX509TrustManager()}, new SecureRandom());
        SSLServerSocketFactory serverSocketFactory = sslContext.getServerSocketFactory();
        ServerSocket serverSocket = serverSocketFactory.createServerSocket(8080);
        System.out.println("ok");
        Socket client = serverSocket.accept();
        System.out.println(client.getRemoteSocketAddress());

        // 向客户端发送接收到的字节序列
        OutputStream output = client.getOutputStream();

        // 当一个普通 socket 连接上来, 这里会抛出异常
        // Exception in thread "main" javax.net.ssl.SSLException: Unrecognized
        // SSL message, plaintext connection?
        InputStream input = client.getInputStream();
        byte[] buf = new byte[1024];
        int len = input.read(buf);
        System.out.println("received: " + new String(buf, 0, len));
        output.write(buf, 0, len);
        output.flush();
        output.close();
        input.close();

        // 关闭socket连接
        client.close();
        serverSocket.close();
    }

    class MyX509TrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
