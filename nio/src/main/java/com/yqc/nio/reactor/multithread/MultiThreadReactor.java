package com.yqc.nio.reactor.multithread;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 多线程reactor
 *
 * @author yangqc
 */
public class MultiThreadReactor implements Runnable {

    public static void main(String[] args) throws IOException {
        new MultiThreadReactor(8888).run();
    }

    private Selector selector;

    private ServerSocketChannel serverSocketChannel;

    private MultiThreadReactor(int port) throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(port));
        //操作系统级别提供的SelectorProvider
        SelectorProvider selectorProvider = SelectorProvider.provider();
        selector = selectorProvider.openSelector();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, new Acceptor());
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    dispatch(iterator.next());
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void dispatch(SelectionKey selectionKey) {
        if (selectionKey != null) {
            Runnable runnable = (Runnable) selectionKey.attachment();
            runnable.run();
        }
    }


    final class Acceptor implements Runnable {

        @Override
        public void run() {
            try {
                SocketChannel socketChannel = serverSocketChannel.accept();
                new Handler(socketChannel, selector);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    final class Handler implements Runnable {

        private final SocketChannel socketChannel;

        private final SelectionKey selectionKey;

        public static final int READ = 1;

        public static final int WRITE = 2;

        private static final int PROCESS = 3;

        private int state = READ;

        private ByteBuffer readBuffer = ByteBuffer.allocate(1024);

        private ByteBuffer writeBuffer = ByteBuffer.allocate(1024);

        private final ExecutorService executorService = Executors.newCachedThreadPool();

        Handler(SocketChannel socketChannel, Selector selector) throws IOException {
            this.socketChannel = socketChannel;
            socketChannel.configureBlocking(false);
            selectionKey = socketChannel.register(selector, SelectionKey.OP_READ, this);
            selector.wakeup();
        }

        private synchronized void read() throws IOException {
            while (readBuffer.remaining() <= 0 || socketChannel.read(readBuffer) > 0) {
                if (readBuffer.remaining() <= 0) {
                    ByteBuffer newBuffer = ByteBuffer.allocate(readBuffer.capacity() + readBuffer.capacity() / 2);
                    newBuffer.put(newBuffer);
                    readBuffer = newBuffer;
                    readBuffer.flip();
                }
            }
            state = PROCESS;
            executorService.execute(new Processor());
        }

        private synchronized void processAndHandOff() {
            byte[] b = new byte[readBuffer.arrayOffset()];
            readBuffer.flip();
            readBuffer.get(b);
            System.out.println("输入" + new String(b, 0, b.length));
            System.out.println("操作!");
            byte[] bytes = ("HTTP/1.1 200 OK\r\n Server: Apache-Coyote/1.1\r\n Content-Length:512\r\n\r\n" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).getBytes();
            writeBuffer.put(bytes);
            writeBuffer.flip();
            state = WRITE;
            selectionKey.interestOps(SelectionKey.OP_WRITE);
            selector.wakeup();
        }

        private void write() throws IOException {
            socketChannel.write(writeBuffer);
            SelectionKey selectionKey = socketChannel.keyFor(selector);
            selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
            socketChannel.close();
          /*  socketChannel.close();
            selectionKey.cancel();*/
        }

        class Processor implements Runnable {

            @Override
            public void run() {
                processAndHandOff();
            }
        }

        @Override
        public void run() {
            try {
                if (state == READ) {
                    read();
                } else if (state == WRITE) {
                    write();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
