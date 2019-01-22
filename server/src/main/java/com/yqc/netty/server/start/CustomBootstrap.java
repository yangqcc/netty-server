package com.yqc.netty.server.start;

import com.yqc.netty.server.handler.CustomServer;


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
