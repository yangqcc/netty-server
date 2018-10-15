package com.yqc.nio.buffer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-07-08
 * @modified By yangqc
 */
public class BufferTest1 {

  public static void main(String[] args) throws IOException {
    RandomAccessFile aFile = new RandomAccessFile("nio-data.txt", "rw");
    FileChannel channel = aFile.getChannel();

    //分配48个字节
    ByteBuffer buf = ByteBuffer.allocate(48);

    int bytesRead = channel.read(buf);

    while (bytesRead != -1) {
      //调用flip()方法,转为读模式,此时position被设置为0,limit被设置为之前position的位置,而capacity一直不变
      buf.flip();
      while (buf.hasRemaining()) {
        System.out.println((char) buf.get());
      }
      //可以将position设置为0,重新读取数据,此时limit任然保持不变
      buf.rewind();
      while (buf.hasRemaining()) {
        System.out.println((char) buf.get());
      }
      //完全清空缓存区
      buf.clear();
      //channel内容读入缓存区
      bytesRead = channel.read(buf);
    }
    aFile.close();
  }
}
