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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.AsciiString;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
                HttpRequest req = (HttpRequest) msg;
                String uri = req.uri();
                URL url = new URL(uri);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                int code = connection.getResponseCode();
                if (code == 404) {
                    throw new Exception("认证无效，找不到此次认证的会话信息！");
                }
                if (code == 500) {
                    throw new Exception("认证服务器发生内部错误！");
                }
                if (code != 200) {
                    throw new Exception("发生其它错误，认证服务器返回 " + code);
                }
                InputStream is = connection.getInputStream();
                byte[] response = new byte[is.available()];
                is.read(response);
                is.close();
                if (response.length == 0) {
                    throw new Exception("认证无效，找不到此次认证的会话信息！");
                }
                String content = new String(response, "UTF-8");
                ctx.write(content);
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
