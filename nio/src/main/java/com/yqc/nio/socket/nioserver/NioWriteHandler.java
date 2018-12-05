package com.yqc.nio.socket.nioserver;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * @author yangqc
 */
public class NioWriteHandler implements Runnable {

    private final SelectionKey selectionKey;

    NioWriteHandler(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    @Override
    public void run() {
        try {
            if (selectionKey.isValid()) {
                SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                byte[] bytes = "Hello,Client!".getBytes();
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                buffer.put(bytes);
                buffer.flip();
                socketChannel.write(buffer);
                selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
                socketChannel.close();
                System.out.println("write handler,this thread name is :" + Thread.currentThread().getName() + "!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
