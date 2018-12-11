package com.yqc.nio.reactor.mainsubreactor;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * hander
 *
 * @author yangqc
 */
public class Handler implements Runnable {

    private final SocketChannel socketChannel;

    private final Selector selector;

    private final static int READ = 1;

    private final static int WRITE = 2;

    private final static int PROCESS = 3;

    private static int state = READ;

    public Handler(Selector selector, SocketChannel socketChannel) {
        try {
            this.selector = selector;
            this.socketChannel = socketChannel;
            socketChannel.register(selector, SelectionKey.OP_READ);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {

    }
}
