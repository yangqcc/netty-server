package com.yqc.concurrent.future;

import com.google.common.util.concurrent.*;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-12-18
 * @modified By yangqc
 */
public class GuavaFutureDemo {

    public static void main(String[] args) throws InterruptedException {
        long l = System.currentTimeMillis();
        ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
        ListenableFuture<Integer> future = service.submit(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                System.out.println("执行耗时操作...");
                timeConsumingOperation();
                return 100;
            }
        });//<1>
        Futures.addCallback(future, new FutureCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                System.out.println("计算结果:" + result);
            }

            @Override
            public void onFailure(Throwable throwable) {
                System.out.println("异步处理失败,e=" + throwable);
            }
        });//<2>
        System.out.println("主线程运算耗时:" + (System.currentTimeMillis() - l) + " ms");
        //不让守护线程退出
        new CountDownLatch(1).await();
    }

    private static void timeConsumingOperation() {
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
