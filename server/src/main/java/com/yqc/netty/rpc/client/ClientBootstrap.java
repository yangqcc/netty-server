package com.yqc.netty.rpc.client;

import com.yqc.netty.rpc.HelloService;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-11-25
 * @modified By yangqc
 */
public class ClientBootstrap {

    public static final String providerName = "HelloService#hello#";

    public static void main(String[] args) throws InterruptedException {
        // 创建一个代理对象
        RpcConsumer consumer = new RpcConsumer();
        HelloService service = (HelloService) consumer.createProxy(HelloService.class, providerName);
        for (; ; ) {
            Thread.sleep(1000);
            System.out.println(service.hello("are you ok ?"));
        }
    }

}
