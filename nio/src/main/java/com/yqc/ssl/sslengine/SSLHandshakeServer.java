package com.yqc.ssl.sslengine;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
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
    private SocketChannel sc;//channel
    private SSLEngine sslEngine;//SSLEngine引擎
    private Selector selector;//NIO通道

    private ByteBuffer myNetData;
    private ByteBuffer myAppData;
    private ByteBuffer peerNetData;
    //四个buffer缓冲区
    private ByteBuffer peerAppData;

    private ByteBuffer dummy = ByteBuffer.allocate(0);

    //SSLEngineResult.HandShakeStatus
    private SSLEngineResult.HandshakeStatus hsStatus;
    //SSLEngineResult.Status
    private SSLEngineResult.Status status;

    public void run() throws Exception {
        char[] password = "123456".toCharArray();
        KeyStore keyStore = KeyStore.getInstance("JKS");
        InputStream in = new FileInputStream(new File("C:\\Users\\yangqc\\https.keystore"));
        keyStore.load(in, password);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore, "654321".toCharArray());

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(kmf.getKeyManagers(), null, null);
        sslEngine = sslContext.createSSLEngine();
        sslEngine.setUseClientMode(false);

        //初始化SSLEngine
        SSLSession session = sslEngine.getSession();

        myAppData = ByteBuffer.allocate(session.getApplicationBufferSize());
        myNetData = ByteBuffer.allocate(session.getPacketBufferSize());
        peerAppData = ByteBuffer.allocate(session.getApplicationBufferSize());
        peerNetData = ByteBuffer.allocate(session.getPacketBufferSize());
        peerNetData.clear();//定义四个缓冲区

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
            channel.register(selector, SelectionKey.OP_READ);
        } else if (key.isReadable()) {
            sc = (SocketChannel) key.channel();
            logger.info("Server handshake begins... ...");
            //从这里，SSL的交互就开始了
            sslEngine.beginHandshake();//开始begin握手
            hsStatus = sslEngine.getHandshakeStatus();
            doHandshake();//开始进行正式的SSL握手
            //当握手阶段告一段落，握手完毕
            if (hsStatus == SSLEngineResult.HandshakeStatus.FINISHED) {
                key.cancel();
                sc.close();
            }
            logger.info("Server handshake completes... ...");
        }
    }

    //这个方法就是服务器端的握手
    private void doHandshake() throws IOException {
        SSLEngineResult result;
        //一个大的while循环，
        while (hsStatus != SSLEngineResult.HandshakeStatus.FINISHED) {
            logger.info("handshake status: " + hsStatus);
            //判断handshakestatus，下一步的动作是什么？
            switch (hsStatus) {
                //指定delegate任务
                case NEED_TASK:
                    Runnable runnable;
                    while ((runnable = sslEngine.getDelegatedTask()) != null) {
                        runnable.run();//因为耗时比较长，所以需要另起一个线程
                    }
                    hsStatus = sslEngine.getHandshakeStatus();
                    break;
                //需要进行入站了，说明socket缓冲区中有数据包进来了
                case NEED_UNWRAP:
                    //从socket中进行读取
                    int count = sc.read(peerNetData);
                    if (count < 0) {
                        logger.info("no data is read for unwrap.");
                        break;
                    } else {
                        logger.info("data read: " + count);
                    }
                    peerNetData.flip();
                    peerAppData.clear();
                    do {
                        //调用SSLEngine进行unwrap操作
                        result = sslEngine.unwrap(peerNetData, peerAppData);
                        logger.info("Unwrapping:\n" + result);
                        // During an handshake renegotiation we might need to
                        // perform
                        // several unwraps to consume the handshake data.
                    //判断状态
                    } while (result.getStatus() == SSLEngineResult.Status.OK
                            && result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_UNWRAP
                            && result.bytesProduced() == 0);
                    if (peerAppData.position() == 0 && result.getStatus() == SSLEngineResult.Status.OK
                            && peerNetData.hasRemaining()) {
                        result = sslEngine.unwrap(peerNetData, peerAppData);
                        logger.info("Unwrapping:\n" + result);
                    }
                    hsStatus = result.getHandshakeStatus();
                    status = result.getStatus();
                    assert status != status.BUFFER_OVERFLOW : "buffer not overflow." + status.toString();
                    // Prepare the buffer to be written again.
                    peerNetData.compact();
                    // And the app buffer to be read.
                    peerAppData.flip();
                    break;
                //需要出栈
                case NEED_WRAP:
                    myNetData.clear();
                    //意味着从应用程序中发送数据到socket缓冲区中，先wrap
                    result = sslEngine.wrap(dummy, myNetData);
                    hsStatus = result.getHandshakeStatus();
                    status = result.getStatus();
                    while (status != SSLEngineResult.Status.OK) {
                        logger.info("status: " + status);
                        switch (status) {
                            case BUFFER_OVERFLOW:
                                break;
                            case BUFFER_UNDERFLOW:
                                break;
                            default:
                                break;
                        }
                    }
                    myNetData.flip();
                    //最后再发送socketchannel
                    sc.write(myNetData);
                    break;
                default:
                    break;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new SSLHandshakeServer().run();
    }
}
