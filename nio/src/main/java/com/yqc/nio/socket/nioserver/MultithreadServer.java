package com.yqc.nio.socket.nioserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p>title:多线程socket</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-11-04
 * @modified By yangqc
 */
public class MultithreadServer {

  private int count = 0;

  private static final ExecutorService executors;

  static {
    //自定义线程池
    ThreadFactory threadFactory = Thread::new;
    executors = new ThreadPoolExecutor(5,
        12,
        1,
        TimeUnit.MINUTES,
        new LinkedBlockingQueue<>(100),
        threadFactory,
        (r, executor) -> System.out.println("抛弃当前任务!"));
  }

  public void serve() throws IOException {
    ServerSocket serverSocket = new ServerSocket(8080);
    while (true) {
      Socket socket = serverSocket.accept();
      executors.submit(new HandleStream(socket));
    }
  }

  public static void main(String[] args) {
    Calendar instance = Calendar.getInstance();
    instance.add(Calendar.DAY_OF_MONTH, 1);
    System.out.println(instance.get(Calendar.DAY_OF_WEEK) - 1);
  }
}

class HandleStream implements Runnable {

  private Socket socket;

  HandleStream(Socket socket) {
    this.socket = socket;
  }

  @Override
  public void run() {
    try {
      InputStream inputStream = socket.getInputStream();
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "GBK"));
      String readLine;
      char[] bt = new char[2048];
      do {
        if ((bufferedReader.read(bt)) != -1) {
        }
      } while (bufferedReader.ready());
      System.out.println(new String(bt));
      socket.shutdownInput();
      System.out.println("读取完毕");
      OutputStream outputStream = socket.getOutputStream();
      outputStream.write("this is multilethread server!".getBytes());
      outputStream.flush();
      socket.close();
    } catch (Exception e) {
      System.out.println("出错了:" + e.getMessage());
    }
  }
}