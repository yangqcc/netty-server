package com.yqc.ssl.sslengine;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-12-27
 * @modified By yangqc
 */
public class SSLHandshakeServer {

    private static Logger logger = Logger.getLogger(SSLHandshakeServer.class.getName());
    /**
     * channel
     */
    private SocketChannel socketChannel;
    /**
     * SSLEngine引擎
     */
    private SSLEngine sslEngine;
    /**
     * NIO通道
     */
    private Selector selector;

    private SSLContext sslContext;

    public void run() throws Exception {
        char[] password = "123456".toCharArray();
        KeyStore keyStore = KeyStore.getInstance("JKS");
        InputStream in = new FileInputStream(new File("C:\\Users\\yangqc\\https.keystore"));
        keyStore.load(in, password);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore, "654321".toCharArray());

        sslContext = SSLContext.getInstance("SSL");
        sslContext.init(kmf.getKeyManagers(), null, null);
        sslEngine = sslContext.createSSLEngine();
        sslEngine.setUseClientMode(false);

        //初始化SSLEngine
        SSLSession session = sslEngine.getSession();

        //NIO的流程
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        selector = Selector.open();
        ServerSocket serverSocket = serverChannel.socket();
        serverSocket.bind(new InetSocketAddress(80));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        logger.info("Server listens on port 443... ...");
        while (true) {
            selector.select();
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey selectionKey = it.next();
                it.remove();
                //当SelectionKey有事件进来后，进行NIO的处理
                handleRequest(selectionKey);
            }
        }
    }

    private void handleRequest(SelectionKey key) throws Exception {
        if (key.isAcceptable()) {
            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
            SocketChannel channel = ssc.accept();
            channel.configureBlocking(false);
            //当rigister事件发生后，下一步就是读了
            channel.register(selector, SelectionKey.OP_READ, new SSLStreams(sslContext, channel));
        } else if (key.isReadable()) {
            socketChannel = (SocketChannel) key.channel();
            SSLStreams attachment = (SSLStreams) key.attachment();
            SSLStreams.WrapperResult wrapperResult = attachment.recvData(ByteBuffer.allocate(1024));
            System.out.println("accept message:" + new String(wrapperResult.buf.array(), StandardCharsets.UTF_8));
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        } else if (key.isWritable()) {
            byte[] bytes = ("HTTP/1.1 200 OK \r\n Transfer-Encoding: chunked\r\n\r\n hello!").getBytes();
            SSLStreams attachment = (SSLStreams) key.attachment();
            attachment.getOutputStream().write(bytes);
            logger.info("Server handshake completes... ...");
            key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
            socketChannel.close();
        }
    }

    public static void main(String[] args) throws Exception {
        new SSLHandshakeServer().run();
    }
}
