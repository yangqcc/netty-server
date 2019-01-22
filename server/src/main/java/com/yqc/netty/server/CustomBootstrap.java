package com.yqc.netty.server;

import com.yqc.netty.server.netty.handler.CustomServer;


/**
 * 服务启动
 *
 * @author yangqc
 */
public class CustomBootstrap {

    private final int port;

    public CustomBootstrap(int port) {
        this.port = port;
    }


    public static void main(String[] args) {
        new CustomServer(8080, false).start();
    }
}
