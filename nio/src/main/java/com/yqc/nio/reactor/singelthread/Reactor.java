package com.yqc.nio.reactor.singelthread;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

/**
 * reactor服务端模型
 *
 * @author yangqc
 */
public class Reactor implements Runnable {

    private final int port;

    private final Selector selector;

    private final ServerSocketChannel serverSocketChannel;

    public Reactor(int port) throws IOException {
        this.port = port;
        SelectorProvider selectorProvider = SelectorProvider.provider();
        selector = selectorProvider.openSelector();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        //记得设置阻塞
        serverSocketChannel.configureBlocking(false);
        //注册到selector
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, new Acceptor());
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    if (selectionKey.isAcceptable()) {
                        Acceptor acceptor = (Acceptor) selectionKey.attachment();

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
