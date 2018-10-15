package com.yqc.nio.selector;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channel;
import java.nio.channels.Selector;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-07-10
 * @modified By yangqc
 */
public class SelectorTest {

  public static void main(String[] args) throws IOException {
    Selector selctor = Selector.open();
    RandomAccessFile file = new RandomAccessFile("nio-data.txt", "rw");
    Channel channel = file.getChannel();
  /*  channel.configureBlocking(false);
    SelectionKey key = channel.regi*/
  }
}
