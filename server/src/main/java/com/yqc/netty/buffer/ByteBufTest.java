package com.yqc.netty.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import org.junit.jupiter.api.Test;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-10-13
 * @modified By yangqc
 */
public class ByteBufTest {

  @Test
  public void test1() {
    ByteBuf heapBuf = Unpooled
        .unreleasableBuffer(Unpooled.copiedBuffer("Hi!\r\n", Charset.forName("UTF-8")));
    //检查ByteBuf是否有支撑数组，如果有，获取对该数组的引用
    if (heapBuf.hasArray()) {
      byte[] array = heapBuf.array();
      //计算第一个字节的偏移量
      int offset = heapBuf.arrayOffset() + heapBuf.readerIndex();
      //获取可读字节数
      int length = heapBuf.readableBytes();
    }
  }

  /**
   * jdk api 创建复合缓冲区
   */
  @Test
  public void test2() {
    ByteBuffer header = ByteBuffer.allocate(1024);
    ByteBuffer body = ByteBuffer.allocate(10 * 1024);
    ByteBuffer[] message = new ByteBuffer[]{header, body};
    ByteBuffer message2 = ByteBuffer.allocate(header.remaining() + body.remaining());
    message2.put(header);
    message2.put(body);
    message2.flip();
  }

  /**
   * netty类实现符合缓存区模式
   */
  @Test
  public void test3() {
    CompositeByteBuf messageBuf = Unpooled.compositeBuffer();
    ByteBuf header = Unpooled
        .unreleasableBuffer(Unpooled.copiedBuffer("Hi!\r\n", Charset.forName("UTF-8")));
    ByteBuf body = Unpooled
        .unreleasableBuffer(Unpooled.copiedBuffer("Hi!\r\n", Charset.forName("UTF-8")));
    messageBuf.addComponents(header, body);
    //删除索引位置为0的组件
    messageBuf.removeComponent(0);
    for (ByteBuf buf : messageBuf) {
      System.out.println(buf.toString());
    }
  }

  /**
   * Bytebuf获取数据
   */
  @Test
  public void test4() {
    ByteBuf byteBuf = Unpooled.buffer();
    for (int i = 0; i < 10; i++) {
      byteBuf.writeInt(1);
    }
    System.out.println(byteBuf.getInt(2));
  }

}
