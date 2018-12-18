package com.yqc.concurrent.future;

import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.Callable;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-12-18
 * @modified By yangqc
 */
public class NettyFutureDemo {

    public static void main(String[] args) throws InterruptedException {
        long currentTime = System.currentTimeMillis();
        EventLoopGroup eventExecutors = new DefaultEventLoop();
        Future<Integer> future = eventExecutors.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                System.out.println("执行耗时操作!");
                timeConsumingOperation();
                return 100;
            }
        });
        future.addListener(new GenericFutureListener<Future<? super Integer>>() {
            @Override
            public void operationComplete(Future<? super Integer> future) throws Exception {
                System.out.println("计算结果:" + future.get());
            }
        });
        System.out.println("主线程运算耗时:" + (System.currentTimeMillis() - currentTime) + " ms");
        //不让守护线程退出
        //  new CountDownLatch(1).await();
    }

    static void timeConsumingOperation() {
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
