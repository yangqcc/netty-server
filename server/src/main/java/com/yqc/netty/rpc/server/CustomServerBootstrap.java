package com.yqc.netty.rpc.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-11-25
 * @modified By yangqc
 */
public class CustomServerBootstrap {

    public static void main(String[] args) {
        CustomServerBootstrap.startServer0("localhost", 8088);
    }

    private static void startServer0(String hostName, int port) {
        try {
            io.netty.bootstrap.ServerBootstrap bootstrap = new io.netty.bootstrap.ServerBootstrap();
            NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
            bootstrap
                    .group(eventLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                            pipeline.addLast(new HelloServerHandler());
                        }
                    });
            bootstrap.bind(hostName, port).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
