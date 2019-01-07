package com.yqc.ssl.custom;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author yangqc
 */
public class SSLEngineWrapper {

    private SSLContext sslContext;

    private SSLEngine sslEngine;

    private SocketChannel socketChannel;

    private ReentrantLock handshaking = new ReentrantLock();

    private final Object wrapLock = new Object();

    private final Object unwrapLock = new Object();


    private ByteBuffer unwrapSrc = ByteBuffer.allocate(1024), wrapDst = ByteBuffer.allocate(1024);
    private int uRemaining = 0;


    public SSLEngineWrapper(SSLContext sslContext, SocketChannel socketChannel) {
        try {
            this.socketChannel = socketChannel;
            this.sslContext = sslContext;
            this.sslEngine = sslContext.createSSLEngine();
            sslEngine.setUseClientMode(false);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public String read() {
        String result = "";
        try {
            result = this.recvAndUnwrap(wrapDst);
            SSLEngineResult.HandshakeStatus handshakeStatus = sslEngine.getHandshakeStatus();
            if (handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED &&
                    handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
                doHandShake();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private String recvAndUnwrap(ByteBuffer dst) throws IOException {
        SSLEngineResult.Status status = SSLEngineResult.Status.OK;
        // SSLStreams.WrapperResult wrapperResult = new SSLStreams.WrapperResult();
        boolean needData;
        if (uRemaining > 0) {
            unwrapSrc.compact();
            unwrapSrc.flip();
            needData = false;
        } else {
            unwrapSrc.clear();
            needData = true;
        }
        synchronized (unwrapLock) {
            int x;
            do {
                if (needData) {
                    do {
                        x = socketChannel.read(unwrapSrc);
                    } while (x == 0);
                    //返回-1说明连接关闭了
                    if (x == -1) {
                        throw new IOException("connection closed for reading");
                    }
                    unwrapSrc.flip();
                }
                SSLEngineResult sslEngineResult = sslEngine.unwrap(unwrapSrc, dst);
                status = sslEngineResult.getStatus();
                if (status == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                    if (unwrapSrc.limit() == unwrapSrc.capacity()) {
                        ByteBuffer b = ByteBuffer.allocate(unwrapSrc.capacity() * 2);
                        b.put(unwrapSrc);
                        unwrapSrc = b;
                        unwrapSrc.flip();
                    } else {
                        /* Buffer not full, just need to read more
                         * data off the channel. Reset pointers
                         * for reading off SocketChannel
                         */
                        unwrapSrc.position(unwrapSrc.limit());
                        unwrapSrc.limit(unwrapSrc.capacity());
                    }
                    needData = true;
                } else if (status == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                    ByteBuffer d = ByteBuffer.allocate(dst.capacity() * 2);
                    dst.flip();
                    d.put(dst);
                    dst = d;
                    needData = false;
                } else if (status == SSLEngineResult.Status.CLOSED) {
                    return "";
                }
            } while (status != SSLEngineResult.Status.OK);
        }
        uRemaining = unwrapSrc.remaining();
        dst.flip();
        return new String(dst.array());
    }

    private void writeAndWrap(ByteBuffer writeBuffer) throws IOException {
        SSLEngineResult.Status status;
        ByteBuffer wrapDst = ByteBuffer.allocate(writeBuffer.capacity());
        synchronized (wrapLock) {
            do {
                SSLEngineResult sslEngineResult = sslEngine.wrap(writeBuffer, wrapDst);
                status = sslEngineResult.getStatus();
                if (status == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                    ByteBuffer buffer = ByteBuffer.allocate(wrapDst.capacity() * 2);
                    wrapDst.flip();
                    buffer.put(wrapDst.array());
                    wrapDst = buffer;
                }
            } while (status == SSLEngineResult.Status.BUFFER_OVERFLOW);

            wrapDst.flip();
            int l = wrapDst.remaining();
            while (l > 0) {
                l -= socketChannel.write(wrapDst);
            }
        }
    }

    public void write(String writeContent) throws IOException {
        SSLEngineResult.HandshakeStatus handshakeStatus = sslEngine.getHandshakeStatus();
        if (handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED &&
                handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            doHandShake();
        }
        try {
            ByteBuffer buffer = ByteBuffer.allocate(writeContent.getBytes().length);
            buffer.put(writeContent.getBytes());
            writeAndWrap(buffer);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void doHandShake() throws IOException {
        try {
            handshaking.lock();
            SSLEngineResult.HandshakeStatus handshakeStatus = sslEngine.getHandshakeStatus();
            while (handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED &&
                    handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
                switch (handshakeStatus) {
                    case NEED_TASK:
                        Runnable task;
                        while ((task = sslEngine.getDelegatedTask()) != null) {
                            /* run in current thread, because we are already
                             * running an external Executor
                             */
                            task.run();
                        }
                        break;
                    case NEED_WRAP:
                        writeAndWrap(ByteBuffer.allocate(1024));
                        break;
                    case NEED_UNWRAP:
                        wrapDst.clear();
                        this.recvAndUnwrap(wrapDst);
                        break;
                    default:
                        throw new NullPointerException("握手不支持'" + handshakeStatus + "'操作!");
                }
                handshakeStatus = sslEngine.getHandshakeStatus();
            }
        } finally {
            handshaking.unlock();
        }
    }

    public static KeyManager[] createKeyManagers(String filePath, String keystorePassword, String keyPassword) {
        InputStream keystoreIs = null;
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keystoreIs = new FileInputStream(filePath);
            keyStore.load(keystoreIs, keystorePassword.toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, keyPassword.toCharArray());
            return kmf.getKeyManagers();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if (keystoreIs != null) {
                try {
                    keystoreIs.close();
                } catch (IOException e) {
                    System.out.println("读取keystore流关闭失败!");
                }
            }
        }
    }
}
