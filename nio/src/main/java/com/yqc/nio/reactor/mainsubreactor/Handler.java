package com.yqc.nio.reactor.mainsubreactor;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    private ByteBuffer readBuffer = ByteBuffer.allocate(5);

    private ByteBuffer writeBuffer = ByteBuffer.allocate(1024);

    public Handler(Selector selector, SocketChannel socketChannel) {
        try {
            this.selector = selector;
            this.socketChannel = socketChannel;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void read() {
        try {
            while (true) {
                if (socketChannel.read(readBuffer) != -1 && socketChannel.read(readBuffer) == 0 && !readBuffer.hasRemaining()) {
                    ByteBuffer newBuffer = ByteBuffer.allocate(readBuffer.capacity() * 2);
                    readBuffer.flip();
                    newBuffer.put(readBuffer);
                    readBuffer = newBuffer;
                    continue;
                }
                if (socketChannel.read(readBuffer) == -1 || readBuffer.hasRemaining()) {
                    break;
                }
            }
            System.out.println("当前线程是:" + Thread.currentThread().getName());
            System.out.println(new String(readBuffer.array(), 0, readBuffer.limit(), StandardCharsets.UTF_8));
            state = PROCESS;
            process();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void write() {
        try {
            socketChannel.write(writeBuffer);
            SelectionKey selectionKey = socketChannel.keyFor(selector);
            selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
            selectionKey.cancel();
            socketChannel.close();
            readBuffer.clear();
            writeBuffer.flip();
            state = READ;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void process() {
        writeBuffer.clear();
        byte[] bytes = ("HTTP/1.1 200 OK \r\n Transfer-Encoding: chunked\r\n\r\n " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).getBytes();
        writeBuffer.put(bytes);
        writeBuffer.flip();
        SelectionKey selectionKey = socketChannel.keyFor(selector);
        selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
        state = WRITE;
    }

    @Override
    public void run() {
        if (state == READ) {
            read();
        } else if (state == PROCESS) {
            process();
        } else if (state == WRITE) {
            write();
        }
    }
}
