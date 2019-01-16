/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.yqc.netty.server.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author yangqc
 */
public class HttpHelloWorldServerHandler extends SimpleChannelInboundHandler<HttpObject> {


    private static final byte[] CONTENT = {'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd'};

    private static final AsciiString CONTENT_TYPE = AsciiString.cached("Content-Type");
    private static final AsciiString CONTENT_LENGTH = AsciiString.cached("Content-Length");
    private static final AsciiString CONNECTION = AsciiString.cached("Connection");
    private static final AsciiString KEEP_ALIVE = AsciiString.cached("keep-alive");


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        try {
            if (msg instanceof HttpRequest) {
                String uri = ((HttpRequest) msg).uri();
                String[] strings = uri.split(":");
                CloseableHttpResponse httpResponse = HttpsUtil.get(uri, null);
                HttpEntity entity = httpResponse.getEntity();
                ByteBuf byteBuf = Unpooled.wrappedBuffer("hello".getBytes());
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HTTP_1_1, OK, byteBuf);
                response.headers().set(CONTENT_TYPE, "text/html");
                response.headers().set(CONTENT_LENGTH,
                        response.content().readableBytes());
                response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                ctx.writeAndFlush(entity);
            } else {
                System.out.println("error!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
