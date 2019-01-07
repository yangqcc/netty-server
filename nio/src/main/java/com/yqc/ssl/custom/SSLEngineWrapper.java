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
            result = this.readAndUnWrap();
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

    private String readAndUnWrap() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        ByteBuffer desBuffer = ByteBuffer.allocate(buffer.capacity());
        boolean needData = true;
        while (true) {
            if (needData) {
                int len = socketChannel.read(buffer);
                if (len == -1) {
                    throw new RuntimeException("远程主机关闭了连接!");
                }
                while (len != 0) {
                    ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                    buffer.flip();
                    newBuffer.put(buffer);
                    buffer = newBuffer;
                    len = socketChannel.read(newBuffer);
                }
                buffer.flip();
            }
            SSLEngineResult result = sslEngine.unwrap(buffer, desBuffer);
            if (result.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                ByteBuffer newBuffer = ByteBuffer.allocate(desBuffer.capacity() * 2);
                newBuffer.put(desBuffer);
                desBuffer = newBuffer;
                desBuffer.flip();
                needData = false;
            } else if (result.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                if (buffer.limit() == buffer.capacity()) {
                    /* buffer not big enough */
                    ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                    buffer.flip();
                    newBuffer.put(buffer);
                    buffer = newBuffer;
                } else {
                    /* Buffer not full, just need to read more
                     * data off the channel. Reset pointers
                     * for reading off SocketChannel
                     */
                    buffer.position(buffer.limit());
                    buffer.limit(buffer.capacity());
                }
                buffer.capacity();
                needData = true;
            } else if (result.getStatus() == SSLEngineResult.Status.OK) {
                break;
            }
        }
        return new String(desBuffer.array());
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
                        readAndUnWrap();
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
