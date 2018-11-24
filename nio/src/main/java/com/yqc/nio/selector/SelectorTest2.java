package com.yqc.nio.selector;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-11-10
 * @modified By yangqc
 */
public class SelectorTest2 {

  @Test
  public void testSelector() throws IOException {
    Selector selector = Selector.open();
    // 打开一个SocketChannel
    SocketChannel socketChannel = SocketChannel.open();
    // 和Selector注册到Selector,Channel必须处于非阻塞模式下,
    // 这意味着FileChannel不能与Selector一起使用,因为FileChannel不能切换到非阻塞模式
    // 但是套接字通道可以
    socketChannel.configureBlocking(false);
    // 将通道注册到selector上
    // 注意register()方法的第二个参数。这是一个“interest集合”，意思是在通过Selector监听Channel时对什么事件感兴趣。可以监听四种不同类型的事件：
    //
    //    Connect
    //    Accept
    //    Read
    //    Write
    // 通道触发了一个事件意思是该事件已经就绪。所以，某个channel成功连接到另一个服务器称为“连接就绪”。
    // 一个server socket channel准备好接收新进入的连接称为“接收就绪”。一个有数据可读的通道可以说是“读就绪”。
    // 等待写数据的通道可以说是“写就绪”。
    //
    // 这四种事件用SelectionKey的四个常量来表示：
    //
    //    SelectionKey.OP_CONNECT
    //    SelectionKey.OP_ACCEPT
    //    SelectionKey.OP_READ
    //    SelectionKey.OP_WRITE
    //
    // 如果你对不止一种事件感兴趣，那么可以用“位或”操作符将常量连接起来，如下：
    //	int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
    SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
    // 一旦调用了select()方法，并且返回值表明有一个或更多个通道就绪了，
    // 然后可以通过调用selector的selectedKeys()方法，访问“已选择键集（selected key set）”中的就绪通道。如下所示：
    Set selectedKeys = selector.selectedKeys();
    Iterator keyIterator = selectedKeys.iterator();
    while (keyIterator.hasNext()) {
      SelectionKey key = (SelectionKey) keyIterator.next();
      if (key.isAcceptable()) {
        // a connection was accepted by a ServerSocketChannel.
      } else if (key.isConnectable()) {
        // a connection was established with a remote server.
      } else if (key.isReadable()) {
        // a channel is ready for reading
      } else if (key.isWritable()) {
        // a channel is ready for writing
      }
      // 注意每次迭代末尾的keyIterator.remove()调用。
      // Selector不会自己从已选择键集中移除SelectionKey实例。必须在处理完通道时自己移除。
      // 下次该通道变成就绪时，Selector会再次将其放入已选择键集中。
      keyIterator.remove();
    }
  }

}
