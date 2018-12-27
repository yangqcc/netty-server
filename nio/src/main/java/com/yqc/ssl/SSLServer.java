package com.yqc.ssl;

import org.junit.jupiter.api.Test;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-12-24
 * @modified By yangqc
 */
public class SSLServer {

    @Test
    public void init() throws Exception {
        //获取用户账户名称
        String user_home = System.getProperty("user.home");
        System.out.println(user_home);
        char[] passphrase = "123456".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(user_home + "/server.keystore"), passphrase);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        //如果keystore中只有一个keyEntry，则此处表示keyEntry的密码可以与keystore的密码不同。
        kmf.init(ks, "654321".toCharArray());
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);
        ServerSocketFactory factory = sslContext.getServerSocketFactory();
        ServerSocket serverSocket = factory.createServerSocket(8080);
        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(new Worker(socket)).run();
        }
    }

    private class Worker implements Runnable {
        Socket s;

        public Worker(Socket s) {
            this.s = s;
        }

        @Override
        public void run() {
            try {
                byte[] buf = new byte[10240];
                int len = s.getInputStream().read(buf);
                if (len > 0) {
                    System.out.println(new String(buf, 0, len));
                    s.getOutputStream().write("200ok\r\n".getBytes());
                    s.getOutputStream().write("Content-length:6\r\n\r\n".getBytes());
                    s.getOutputStream().write("1234567".getBytes());
                    s.getOutputStream().close();
                    s.getInputStream().close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
