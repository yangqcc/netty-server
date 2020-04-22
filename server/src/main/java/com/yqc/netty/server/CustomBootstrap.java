package com.yqc.netty.server;

import com.yqc.netty.server.netty.handler.CustomServer;


/**
 * 服务启动
 *
 * @author yangqc
 */
public class CustomBootstrap {

    public CustomBootstrap(int port) {
    }


    public static void main(String[] args) {
        new CustomServer(8080, false).start();
    }
}
