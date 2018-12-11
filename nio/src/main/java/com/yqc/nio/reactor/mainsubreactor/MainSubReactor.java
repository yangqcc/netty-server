package com.yqc.nio.reactor.mainsubreactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 一个主Reactor和多个从Reactor
 *
 * @author yangqc
 */
public class MainSubReactor implements Runnable {

    public static void main(String[] args) {
        new MainSubReactor(8080).run();
    }

    private final Selector mainSelector;

    private final Selector[] subSelector = new Selector[6];

    private int count = 0;

    private final Executor executor = Executors.newFixedThreadPool(subSelector.length);

    MainSubReactor(int port) {
        try {
            SelectorProvider selectorProvider = SelectorProvider.provider();
            ServerSocketChannel serverSocketChannel = selectorProvider.openServerSocketChannel();
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            mainSelector = selectorProvider.openSelector();
            serverSocketChannel.register(mainSelector, SelectionKey.OP_ACCEPT, new Acceptor(serverSocketChannel, mainSelector));
            for (int i = 0; i < subSelector.length; i++) {
                subSelector[i] = selectorProvider.openSelector();
                //提交subSelector
                executor.execute(new SubReactor(subSelector[i]));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                mainSelector.select();
                Iterator<SelectionKey> iterator = mainSelector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    dispatch(key);
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dispatch(SelectionKey selectionKey) {
        Object attachment = selectionKey.attachment();
        if (attachment != null) {
            ((Runnable) attachment).run();
        }
    }

    class Acceptor implements Runnable {

        ServerSocketChannel serverSocketChannel;

        Selector selector;

        Acceptor(ServerSocketChannel serverSocketChannel, Selector selector) {
            this.serverSocketChannel = serverSocketChannel;
            this.selector = selector;
        }

        @Override
        public void run() {
            try {
                Selector selector = subSelector[count++ % subSelector.length];
                SocketChannel socketChannel = serverSocketChannel.accept();
                socketChannel.configureBlocking(false);
                socketChannel.register(selector, SelectionKey.OP_READ, new Handler(selector, socketChannel));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
