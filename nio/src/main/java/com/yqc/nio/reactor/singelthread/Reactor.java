package com.yqc.nio.reactor.singelthread;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

/**
 * 单线程reactor服务端模型
 *
 * @author yangqc
 */
public class Reactor implements Runnable {

    public static void main(String[] args) throws IOException {
        new Reactor(8080).run();
    }

    private final Selector selector;

    private final ServerSocketChannel serverSocketChannel;

    public Reactor(int port) throws IOException {
        SelectorProvider selectorProvider = SelectorProvider.provider();
        selector = selectorProvider.openSelector();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        serverSocketChannel.socket();
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
                    dispatch(iterator.next());
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void dispatch(SelectionKey selectionKey) {
        Runnable runnable = (Runnable) selectionKey.attachment();
        if (runnable != null) {
            runnable.run();
        }
    }

    /**
     * acceptor
     */
    class Acceptor implements Runnable {

        @Override
        public void run() {
            try {
                SocketChannel socketChannel = serverSocketChannel.accept();
                if (socketChannel != null) {
                    new Handler(socketChannel, selector);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class Handler implements Runnable {

        private final SocketChannel socketChannel;

        private SelectionKey selectionKey;

        ByteBuffer input = ByteBuffer.allocate(1024);

        ByteBuffer output = ByteBuffer.allocate(1024);

        private static final int READING = 0, SENDING = 1;

        int state = READING;

        boolean inputIsComplete() {
            return true;
        }

        boolean outputIsComplete() {
            return true;
        }

        void process() {
            byte[] bytes1 = new byte[input.arrayOffset()];
            input.flip();
            input.get(bytes1);
            System.out.println("输入:" + new String(bytes1, 0, input.arrayOffset()));
            byte[] bytes = "hello,client!".getBytes();
            output.put(bytes);
            output.flip();
        }

        Handler(SocketChannel socketChannel, Selector selector) throws IOException {
            this.socketChannel = socketChannel;
            socketChannel.configureBlocking(false);
            selectionKey = socketChannel.register(selector, 0);
            selectionKey.attach(this);
            selectionKey.interestOps(SelectionKey.OP_READ);
            selector.wakeup();
        }

        private void read() throws IOException {
            while (input.remaining() > 0 && socketChannel.read(input) != -1 && socketChannel.read(input) != 0) {
                if (input.remaining() <= 0) {
                    ByteBuffer newBuffer = ByteBuffer.allocate(input.capacity() + input.capacity() / 2);
                    input.flip();
                    newBuffer.put(input);
                    input = newBuffer;
                    input.flip();
                }
            }
            if (inputIsComplete()) {
                state = SENDING;
                //执行业务逻辑
                process();
                // Normally also do first write now
                selectionKey.interestOps(SelectionKey.OP_WRITE);
            }
        }

        private void write() throws IOException {
            socketChannel.write(output);
            if (outputIsComplete()) {
                socketChannel.close();
                //这里要cancel,不然一直有读事件
                selectionKey.cancel();
            }
        }

        @Override
        public void run() {
            try {
                if (state == READING) {
                    read();
                } else if (state == SENDING) {
                    write();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
