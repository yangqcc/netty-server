package com.yqc.nio.socket.nioserver;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;

/**
 * 多线程服务器
 *
 * @author yangqc
 */
public class MultiThreadingNioServer {

    /**
     * selector
     */
    private Selector selector;

    /**
     * 线程池
     */
    private final ExecutorService executorService;

    private MultiThreadingNioServer() {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException("初始化selector出错!");
        }
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("demo-pool-%d").build();
        //自定义线程池
        executorService = new ThreadPoolExecutor(5, 20, 5,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(10), namedThreadFactory, (r, executor) -> r.run());

    }

    private void serve() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(8080));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (true) {
            int select = selector.select();
            if (select > 0) {
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    if (selectionKey.isValid() && selectionKey.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel1 = (ServerSocketChannel) selectionKey.channel();
                        SocketChannel socketChannel = serverSocketChannel1.accept();
                        if (socketChannel != null) {
                            socketChannel.configureBlocking(false);
                            socketChannel.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ | SelectionKey.OP_CONNECT);
                        }
                    }
                    if (selectionKey.isValid() && selectionKey.isReadable()) {
                        executorService.execute(new NioReadHandler(selectionKey));
                    }
                  /*  if (selectionKey.isValid() && selectionKey.isWritable()) {
                        executorService.execute(new NioWriteHandler(selectionKey));
                    }*/
                 /*   if (selectionKey.isValid() && selectionKey.isConnectable()) {
                        executorService.execute(new NioConnectHandler(selectionKey));
                    }*/
                }
                iterator.remove();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new MultiThreadingNioServer().serve();
    }
}
