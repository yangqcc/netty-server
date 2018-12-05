package com.yqc.nio.socket.nioserver;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * @author yangqc
 */
public class NioReadHandler implements Runnable {

    private final SelectionKey selectionKey;

    NioReadHandler(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    @Override
    public void run() {
        try {
            if (selectionKey.isValid()) {
                SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                StringBuilder stringBuilder = new StringBuilder();
                int len;
                if ((len = socketChannel.read(buffer)) > 0) {
                    byte[] bytes = new byte[1024];
                    buffer.flip();
                    buffer.get(bytes, 0, len);
                    stringBuilder.append(new String(bytes, 0, len, StandardCharsets.UTF_8));
                } else {
                    socketChannel.close();
                }
                System.out.println("read handler,this thread name is:" + stringBuilder.toString() + "!");
                buffer.clear();
                byte[] bytes = "Hello,Client!".getBytes();
                buffer.put(bytes);
                buffer.flip();
                socketChannel.write(buffer);
                selectionKey.interestOps(SelectionKey.OP_READ);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
