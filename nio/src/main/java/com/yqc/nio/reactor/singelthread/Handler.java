package com.yqc.nio.reactor.singelthread;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * reactor模型中的handler
 *
 * @author yangqc
 */
public class Handler implements Runnable {

    private final SocketChannel socketChannel;

    private final Selector selector;

    ByteBuffer input = ByteBuffer.allocate(1024);

    ByteBuffer output = ByteBuffer.allocate(1024);

    private static final int READING = 0, SENDING = 1;

    int state = READING;

    boolean inputIsComplete() { /* ... */ }

    boolean outputIsComplete() { /* ... */ }

    void process() { /* ... */ }

    Handler(SocketChannel socketChannel, Selector selector) throws ClosedChannelException {
        this.socketChannel = socketChannel;
        this.selector = selector;
        SelectionKey selectionKey = socketChannel.register(selector, 0);
        selectionKey.attach(this);
        selectionKey.interestOps(SelectionKey.OP_READ);
        selector.wakeup();
    }

    private void read() {

    }

    private void write() {
    }

    @Override
    public void run() {

    }
}
