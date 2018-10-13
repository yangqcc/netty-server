package com.yqc.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * <p>title:</p>
 * <p>description:netty实现阻塞io</p>
 *
 * @author yangqc
 * @date Created in 2018-10-13
 * @modified By yangqc
 */
public class NettyOioServer {

  public void server(int port) throws InterruptedException {
    final ByteBuf buf = Unpooled
        .unreleasableBuffer(Unpooled.copiedBuffer("Hi!\r\n", Charset.forName("UTF-8")));
    EventLoopGroup group = new OioEventLoopGroup();
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(group)
          //OioServerSocketChannel允许以阻塞模式
          .channel(OioServerSocketChannel.class)
          .localAddress(new InetSocketAddress(port))
          //对于每个已接收的连接都调用它
          .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
              ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

                @Override
                public void channelActive(ChannelHandlerContext ctx) {
                  ctx.writeAndFlush(buf.duplicate()).addListener(ChannelFutureListener.CLOSE);
                }

              });
            }
          });
      ChannelFuture f = b.bind().sync();
      f.channel().closeFuture().sync();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      group.shutdownGracefully().sync();
    }
  }

  public static void main(String[] args) throws InterruptedException {
    new NettyOioServer().server(8080);
  }
}
