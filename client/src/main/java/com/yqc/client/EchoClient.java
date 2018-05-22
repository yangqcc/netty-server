package com.yqc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * 客户端:
 * <p>
 * 创建一个 Bootstrap 来初始化客户端
 * 一个 NioEventLoopGroup 实例被分配给处理该事件的处理，这包括创建新的连接和处理入站和出站数据
 * 创建一个 InetSocketAddress 以连接到服务器
 * 连接好服务器之时，将安装一个 EchoClientHandler 在 pipeline
 * 之后 Bootstrap.connect（）被调用连接到远程的 - 本例就是 echo(回声)服务器。
 *
 * @author yangqc
 */
public class EchoClient {
    private final String host;
    private final int port;

    public EchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();                //1.创建 Bootstrap
            b.group(group)                                //2.指定 EventLoopGroup 来处理客户端事件。由于我们使用 NIO 传输，所以用到了 NioEventLoopGroup 的实现
                    .channel(NioSocketChannel.class)      //3.使用的 channel 类型是一个用于 NIO 传输
                    .remoteAddress(new InetSocketAddress(host, port))    //4.设置服务器的 InetSocketAddress
                    .handler(new ChannelInitializer<SocketChannel>() {   //5.当建立一个连接和一个新的通道时，创建添加到 EchoClientHandler 实例 到 channel pipeline
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new EchoClientHandler());
                        }
                    });

            ChannelFuture f = b.connect().sync();        //6.连接到远程;等待连接完成
            f.channel().closeFuture().sync();            //7.阻塞直到 Channel 关闭
        } finally {
            group.shutdownGracefully().sync();           //8.调用 shutdownGracefully() 来关闭线程池和释放所有资源
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: " + EchoClient.class.getSimpleName() + " <host> <port>");
            return;
        }
        final String host = args[0];
        final int port = Integer.parseInt(args[1]);
        new EchoClient(host, port).start();
    }

}
