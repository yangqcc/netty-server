package com.yqc.nio.reactor;

import java.nio.channels.SocketChannel;

/**
 * @author yangqc
 */
public class Acceptor implements Runnable {

    private Reactor reactor;

    public Acceptor(Reactor reactor) {
        this.reactor = reactor;
    }

    @Override
    public void run() {
        try {
            SocketChannel socketChannel = reactor.serverSocketChannel.accept();
            if (socketChannel != null) {
                new SocketReadHandler(reactor.selector, socketChannel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
