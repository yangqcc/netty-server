package com.yqc.netty.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import org.junit.jupiter.api.Test;

/**
 * <p>title:测试ByteBuf类</p>
 * <p>description:
 * get()和set()操作,从给定的索引开始,并且保持索引不变 read()和write()操作，从给定的索引开始,并且会根据已访问过的字节数对索引进行调整
 * </p>
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
    ByteBuf byteBuf = Unpooled.buffer(40);
    for (int i = 0; i < 10; i++) {
      byteBuf.writeInt(1);
    }
    System.out.println(byteBuf.getInt(4));
  }

  /**
   * 派生缓冲区，对ByteBuf进行切片，返回一个新的ByteBuf对象,该对象有自己的读索引， 写索引和标记索引 slice避免复制内存的开销
   */
  @Test
  public void test5() {
    Charset utf8 = Charset.forName("UTF-8");
    //创建一个用于保存给定字符串的字节的ByteBuf
    ByteBuf buf = Unpooled.copiedBuffer("Netty in action rocks!", utf8);
    //从索引0到索引15结束的一个新的切片
    ByteBuf sliced = buf.slice(0, 15);
    System.out.println(sliced.toString());
    //更新索引0处的字节
    buf.setByte(0, 'J');
    System.out.println(buf.getByte(0) == sliced.getByte(0));
  }

  /**
   * 创建副本
   */
  @Test
  public void test6() {
    Charset utf8 = Charset.forName("UTF-8");
    //创建一个用于保存给定字符串的字节的ByteBuf
    ByteBuf buf = Unpooled.copiedBuffer("Netty in action rocks!", utf8);
    //从索引0到索引15结束的一个新的切片
    ByteBuf sliced = buf.copy(0, 15);
    System.out.println(sliced.toString());
    //更新索引0处的字节
    buf.setByte(0, 'J');
    System.out.println(buf.getByte(0) == sliced.getByte(0));
  }

  @Test
  public void testByteBufAllocator() {
    ByteBufAllocator allocator = new PooledByteBufAllocator();
    allocator.buffer();

  }

}
