package com.yqc.nio.reactor.singelthread;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * reactor模型中的acceptor
 *
 * @author yangqc
 */
public class Acceptor implements Runnable {

    private final ServerSocketChannel serverSocketChannel;

    private final Selector selector;

    Acceptor(ServerSocketChannel serverSocketChannel, Selector selector) {
        this.selector = selector;
        this.serverSocketChannel = serverSocketChannel;
    }

    @Override
    public void run() {
        try {
            SocketChannel socketChannel = serverSocketChannel.accept();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
