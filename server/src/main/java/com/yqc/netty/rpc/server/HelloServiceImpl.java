package com.yqc.netty.rpc.server;

import com.yqc.netty.rpc.HelloService;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-11-25
 * @modified By yangqc
 */
public class HelloServiceImpl implements HelloService {


    @Override
    public String hello(String msg) {
        return msg != null ? msg.toUpperCase() : "---->I am fine.";
    }
}
