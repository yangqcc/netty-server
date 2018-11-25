package com.yqc.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.net.InetSocketAddress;

/**
 * <p>title:</p>
 * <p>description:netty非阻塞io</p>
 *
 * @author yangqc
 * @date Created in 2018-10-13
 * @modified By yangqc
 */
public class EchoServer {

  public static void main(String[] args) throws InterruptedException {
    new EchoServer(8080).serve();
  }

  private final int port;

  public EchoServer(int port) {
    this.port = port;
  }

  public void serve() throws InterruptedException {
    //设置serverHandler
    EchoServerHandler serverHandler = new EchoServerHandler();
    //创建EventLoopGroup进行事件的处理,如新的连接或者读/写数据处理
    EventLoopGroup group = new NioEventLoopGroup();
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(group)
          .channel(NioServerSocketChannel.class)
          //使用指定的端口设置套接字地址
          .localAddress(new InetSocketAddress(port))
          //添加一个Handler到子Channel的ChannelPipeline
          //当一个新链接被接受时,一个新的子Channel会被创建
          //ChannelInitializer会将EchoServerHandler添加到该Channel的ChannelPipeline
          .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
              ch.pipeline().addLast(serverHandler);
            }
          });
      //异步绑定服务器,调用sync方法直到绑定完成
      ChannelFuture f = b.bind().sync();
      Channel channel = f.channel();
      channel.writeAndFlush("this is server!");
      //获取Channel的CloseFuture,并且阻塞当前线程直到完成
      f.channel().closeFuture().sync();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      group.shutdownGracefully().sync();
    }
  }

}
