package com.yqc.nio.channel;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import org.junit.jupiter.api.Test;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-11-10
 * @modified By yangqc
 */
public class ChannelTransfer {

  @Test
  public void channelTransferTest() throws IOException {
    RandomAccessFile fromFile = new RandomAccessFile("fromFile.txt", "rw");
    FileChannel fromChannel = fromFile.getChannel();

    RandomAccessFile toFile = new RandomAccessFile("toFile.txt", "rw");
    FileChannel toChannel = toFile.getChannel();

    //目标通道写入位置
    long position = 0;
    //源通道读取数量
    long count = fromChannel.size();
    toChannel.transferFrom(fromChannel, position, count);
  }

  /**
   * 当前通道传输到指定通道
   */
  @Test
  public void transferTo() throws IOException {
    RandomAccessFile fromFile = new RandomAccessFile("fromFile.txt", "rw");
    FileChannel fromChannel = fromFile.getChannel();

    RandomAccessFile toFile = new RandomAccessFile("toFile.txt", "rw");
    FileChannel toChannel = toFile.getChannel();
    long position = 0;
    long count = fromChannel.size();
    fromChannel.transferTo(position, count, toChannel);
  }
}
