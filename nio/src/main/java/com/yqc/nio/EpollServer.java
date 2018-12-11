package com.yqc.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import static javax.swing.UIManager.getInt;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-10-21
 * @modified By yangqc
 */
public class EpollServer {

    public static void main(String[] args) {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress("127.0.0.1", 8000));
            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            ByteBuffer writeBuffer = ByteBuffer.allocate(128);

            writeBuffer.put("received".getBytes());
            writeBuffer.flip();

            while (true) {
                int nReady = selector.select();
                Set<SelectionKey> keySet = selector.selectedKeys();
                Iterator<SelectionKey> it = keySet.iterator();

                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();
                    if (key.isAcceptable()) {
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
                        selectionKey.attach(new EpollTask(socketChannel, selectionKey));
                    } else if (key.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        readBuffer.clear();
                        socketChannel.read(readBuffer);
                        readBuffer.flip();

                        EpollTask conn = (EpollTask) key.attachment();
                        conn.onRead(getInt(readBuffer));

                        key.interestOps(SelectionKey.OP_WRITE);
                    } else if (key.isWritable()) {
                        writeBuffer.rewind();
                        SocketChannel socketChannel = (SocketChannel) key.channel();

                        EpollTask conn = (EpollTask) key.attachment();
                        key.interestOps(SelectionKey.OP_READ);
                        conn.onWrite();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
