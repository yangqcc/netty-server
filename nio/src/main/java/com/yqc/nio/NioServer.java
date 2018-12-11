package com.yqc.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * <p>title:</p>
 * <p>description:自定义实现nio</p>
 *
 * @author yangqc
 * @date Created in 2018-10-16
 * @modified By yangqc
 */
public class NioServer {

    private String ip;

    private int port;

    private int connectionCount = 0;

    public NioServer(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    private Selector selector;

    public void startListen() throws IOException, InterruptedException {
        selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        serverChannel.bind(new InetSocketAddress(ip, port));
        while (true) {
            int result = selector.selectNow();
            if (result == 0) {
                continue;
            }
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                if (key.isAcceptable()) {
                    accept(key);
                } else if (key.isReadable()) {
                    read(key);
                } else if (key.isWritable()) {
                    write(key);
                } else {
                    System.out.println("Unknown  selector type");
                }
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        System.out.println("Receive connection");
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        if (socketChannel != null) {
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
        }
        System.out.println("Connection end");
    }

    private void read(SelectionKey selectionKey) throws IOException {
        System.out.println("Start read");
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(64);
        boolean hasContent = false;
        while (channel.read(byteBuffer) > 0) {
            byteBuffer.flip();
            CharBuffer cb = Charset.forName("UTF-8").decode(byteBuffer);
            System.out.println(cb.toString());
            byteBuffer.clear();
            hasContent = true;
        }

        if (hasContent) {
            selectionKey.interestOps(SelectionKey.OP_WRITE);
        } else {
            channel.close();
        }
        System.out.println("Read end");
    }

    private void write(SelectionKey selectionKey) throws IOException, InterruptedException {
        connectionCount++;
        while (connectionCount == 1) {
            Thread.sleep(1000);
        }
        System.out.println("Start write");
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        String resTxt = getResponseText();
        ByteBuffer buffer = ByteBuffer.wrap(resTxt.getBytes());
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
        channel.close();
        System.out.println("End write");
    }

    private String getResponseText() {
        StringBuffer sb = new StringBuffer();
        sb.append("HTTP/1.1 200 OK\n");
        sb.append("Content-Type: text/html; charset=UTF-8\n");
        sb.append("\n");
        sb.append("<html>");
        sb.append("  <head>");
        sb.append("    <title>");
        sb.append("      NIO Http Server");
        sb.append("    </title>");
        sb.append("  </head>");
        sb.append("  <body>");
        sb.append("    <h1>Hello World!</h1>");
        sb.append("  </body>");
        sb.append("</html>");
        return sb.toString();
    }

    public static void main(String[] args) {
        NioServer server = new NioServer("127.0.0.1", 8080);
        try {
            server.startListen();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
