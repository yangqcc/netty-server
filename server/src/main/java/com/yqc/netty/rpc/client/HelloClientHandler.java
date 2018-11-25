package com.yqc.netty.rpc.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.Callable;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-11-25
 * @modified By yangqc
 */
public class HelloClientHandler extends ChannelInboundHandlerAdapter implements Callable {

    private ChannelHandlerContext channelHandlerContext;

    private String result;

    private String para;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channelHandlerContext = ctx;
    }

    @Override
    public synchronized void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        result = msg.toString();
        notify();
    }

    @Override
    public synchronized Object call() throws Exception {
        channelHandlerContext.writeAndFlush(para);
        wait();
        return result;
    }

    void setPara(String para) {
        this.para = para;
    }
}
