package com.yqc.netty.rpc.server;

import com.yqc.netty.rpc.client.ClientBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-11-25
 * @modified By yangqc
 */
public class HelloServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg.toString().startsWith(ClientBootstrap.providerName)) {
            String result = new HelloServiceImpl().hello(msg.toString());
            ctx.writeAndFlush(result);
        }
    }
}
