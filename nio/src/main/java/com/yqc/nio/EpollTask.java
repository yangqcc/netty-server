package com.yqc.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * <p>title:</p>
 * <p>description:这就是一个状态机模型的例子。我们的服务端虽然只有一个线程，但是当多个客户端来连接的时候，仍然可以快速响应。</p>
 *
 * @author yangqc
 * @date Created in 2018-10-21
 * @modified By yangqc
 */
public class EpollTask {

    private SocketChannel socketChannel;
    private SelectionKey key;
    private int state;
    private int dividend;
    private int divisor;
    private int result;
    private ByteBuffer writeBuffer;

    public EpollTask(SocketChannel socketChannel, SelectionKey selectionKey) {
        this.socketChannel = socketChannel;
        this.key = selectionKey;
        writeBuffer = ByteBuffer.allocate(64);
    }

    public void onRead(int data) {
        if (state == 0) {
            dividend = data;
            System.out.println(dividend);
            state = 1;
        } else if (state == 2) {
            divisor = data;
            System.out.println(divisor);
            if (divisor == 0) {
                result = Integer.MAX_VALUE;
            } else {
                result = dividend / divisor;
            }
            state = 3;
        } else {
            throw new RuntimeException("wrong state" + state);
        }
    }

    public void onWrite() {
        try {
            if (state == 1) {
                writeBuffer.clear();
                writeBuffer.put("divident".getBytes());
                writeBuffer.flip();
                socketChannel.write(writeBuffer);
                state = 2;
            } else if (state == 3) {
                writeBuffer.clear();
                writeBuffer.put(String.valueOf(result).getBytes());
                writeBuffer.flip();
                socketChannel.write(writeBuffer);

                socketChannel.close();
                key.cancel();
                state = 4;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
