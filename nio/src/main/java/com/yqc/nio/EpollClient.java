package com.yqc.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;

/**
 * <p>title:</p>
 * <p>description: 客户端实现阻塞式读写，实现了同步模型</p>
 *
 * @author yangqc
 * @date Created in 2018-10-21
 * @modified By yangqc
 */
public class EpollClient {

  public static void main(String[] args) {
    try {
      SocketChannel socketChannel = SocketChannel.open();
      socketChannel.connect(new InetSocketAddress("127.0.0.1", 8000));
      ByteBuffer writeBuffer = ByteBuffer.allocate(32);
      ByteBuffer readBuffer = ByteBuffer.allocate(32);
      byte[] buf = new byte[32];
      Random r = new Random();
      int d;
      d = r.nextInt(1000);
      System.out.println(d);
      writeBuffer.put(String.valueOf(d).getBytes());
      writeBuffer.flip();
      socketChannel.write(writeBuffer);

      socketChannel.read(readBuffer);
      readBuffer.flip();
      System.out.println(new String(readBuffer.array()));
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      writeBuffer.clear();
      d = r.nextInt(10);
      System.out.println(d);
      writeBuffer.put(String.valueOf(d).getBytes());
      writeBuffer.flip();
      socketChannel.write(writeBuffer);

      readBuffer.clear();
      socketChannel.read(readBuffer);
      readBuffer.flip();
      readBuffer.get(buf, 0, readBuffer.remaining());
      System.out.println(new String(buf));
      socketChannel.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
