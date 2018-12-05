package com.yqc.nio.socket.nioserver;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * @author yangqc
 */
public class NioConnectHandler implements Runnable {

    private final SelectionKey selectionKey;

    NioConnectHandler(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    @Override
    public void run() {
        try {
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            if (socketChannel.isConnected()) {
                socketChannel.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
