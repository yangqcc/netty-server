package com.yqc.nio.scatter;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-07-08
 * @modified By yangqc
 */
public class ScatterTest1 {

  public static void main(String[] args) throws IOException {
    RandomAccessFile file = new RandomAccessFile("nio-data.txt", "rw");
    Channel channel = file.getChannel();

    ByteBuffer header = ByteBuffer.allocate(128);
    ByteBuffer body = ByteBuffer.allocate(1024);

    ByteBuffer[] bufferArray = {header, body};
    ((FileChannel) channel).read(bufferArray);
    for (ByteBuffer byteBuffer : bufferArray) {
      byteBuffer.flip();
      System.out.println(byteBuffer);
      while (byteBuffer.hasRemaining()) {
        System.out.print((char) (byteBuffer.get()));
      }
      byteBuffer.clear();
    }

    //多个buffer的内容写入到一个文件中去
    RandomAccessFile file2 = new RandomAccessFile("my-data.txt", "rw");
    (file2.getChannel()).write(bufferArray);
    for (ByteBuffer byteBuffer : bufferArray) {
      System.out.println(byteBuffer);
    }
  }
}
