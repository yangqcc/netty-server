package com.yqc.ssl.custom;

import com.yqc.ssl.sslengine.SSLStreams;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Set;

/**
 * @author yangqc
 */
public class HttpsServer {

    private int port;

    private Selector selector;

    private ServerSocketChannel serverSocketChannel;

    public HttpsServer(int port) {
        try {
            this.port = port;
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    public void start() throws IOException, NoSuchAlgorithmException, KeyManagementException {
        while (!Thread.currentThread().isInterrupted()) {
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                if (selectionKey.isAcceptable()) {
                    serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    KeyManager[] keyManagers = SSLEngineWrapper.createKeyManagers("C:\\Users\\hanke\\my.keystore", "123456", "654321");
                    SSLContext sslContext = SSLContext.getInstance("SSL");
                    sslContext.init(keyManagers, null, new SecureRandom());
                    socketChannel.configureBlocking(false);
                    SSLStreams sslStreams = new SSLStreams(sslContext, socketChannel);
                    socketChannel.register(selector, SelectionKey.OP_READ, sslStreams);
                } else if (selectionKey.isReadable()) {
                    SSLStreams sslStreams = (SSLStreams) selectionKey.attachment();
                    SSLStreams.WrapperResult wrapperResult = sslStreams.recvData(ByteBuffer.allocate(1024));
                    System.out.println("receive messages:" + new String(wrapperResult.getBuf().array()));
                    selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
                } else if (selectionKey.isWritable()) {
                    SSLStreams sslStreams = (SSLStreams) selectionKey.attachment();
                    String responseText = this.getResponseText();
                    ByteBuffer destBuffer = ByteBuffer.allocate(responseText.getBytes().length);
                    destBuffer.put(responseText.getBytes());
                    destBuffer.flip();
                    sslStreams.sendData(destBuffer);
                    selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
                    selectionKey.channel().close();
                }
                iterator.remove();
            }
        }
    }

    private String getResponseText() {
        StringBuffer sb = new StringBuffer();
        sb.append("HTTP/1.1 200 OK\n");
        sb.append("Content-Type: text/html; charset=UTF-8\n");
        sb.append("\n");
        sb.append("<html>");
        sb.append("  <head>");
        sb.append("    <title>");
        sb.append("      NIO Http Server");
        sb.append("    </title>");
        sb.append("  </head>");
        sb.append("  <body>");
        sb.append("    <h1>Hello World!</h1>");
        sb.append("  </body>");
        sb.append("</html>");
        return sb.toString();
    }

    private void doRead(SSLEngineWrapper sslEngineWrapper) throws IOException {
        String readStr = sslEngineWrapper.read();
        System.out.println("received message:" + readStr);
        sslEngineWrapper.write("ok!");
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException, KeyManagementException {
        new HttpsServer(80).start();
    }
}
