package com.yqc.concurrent.future;

import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-12-18
 * @modified By yangqc
 */
public class NettyFutureDemo {

    public static void main(String[] args) {
        long currentTime = System.currentTimeMillis();
        EventLoopGroup eventExecutors = new DefaultEventLoop();
        Future<Integer> future = eventExecutors.submit(() -> {
            System.out.println("执行耗时操作!");
            timeConsumingOperation();
            return 100;
        });
        future.addListener(future1 -> System.out.println("计算结果:" + future1.get()));
        System.out.println("主线程运算耗时:" + (System.currentTimeMillis() - currentTime) + " ms");
        //不让守护线程退出
        //  new CountDownLatch(1).await();
    }

    private static void timeConsumingOperation() {
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
