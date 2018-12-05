package com.yqc.nio.socket.nioserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

/**
 * 多线程客户端
 *
 * @author yangqc
 */
public class MultiThreadingNioClient {

    public void start() throws IOException {
        Selector selector = Selector.open();
        int i = 0;
        //注册10个通道
        while (i < 10) {
            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);
            //请求连接
            channel.connect(new InetSocketAddress(8080));
            channel.register(selector, SelectionKey.OP_CONNECT);
            i++;
        }
        while (true) {
            try {
                selector.select();
                Iterator ite = selector.selectedKeys().iterator();
                while (ite.hasNext()) {
                    SelectionKey key = (SelectionKey) ite.next();
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    ite.remove();
                    if (key.isConnectable()) {
                        if (socketChannel.isConnectionPending()) {
                            if (socketChannel.finishConnect()) {
                                //只有当连接成功后才能注册OP_READ事件
                                key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                            } else {
                                key.cancel();
                            }
                        }
                    } else if (key.isReadable()) {
                        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
                        int len;
                        while ((len = socketChannel.read(byteBuffer)) > 0) {
                            byteBuffer.flip();
                            byte[] bytes = new byte[len];
                            byteBuffer.get(bytes);
                            byteBuffer.clear();
                            byteBuffer.flip();
                            System.out.println(new String(bytes, StandardCharsets.UTF_8));
                        }
                        socketChannel.close();
                    } else if (key.isWritable()) {
                        socketChannel = (SocketChannel) key.channel();
                        byte[] bytes = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()).getBytes();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        buffer.put(bytes);
                        buffer.flip();
                        socketChannel.write(buffer);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new MultiThreadingNioClient().start();
    }
}
