package com.yqc.nio.socket.blockserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * <p>title:</p>
 * <p>description:socket客户端</p>
 *
 * @author yangqc
 * @date Created in 2018-11-21
 * @modified By yangqc
 */
public class MyClient {

  public static void main(String[] args) throws IOException {
    Socket socket = new Socket("localhost", 8080);
    OutputStream outputStream = socket.getOutputStream();
    String message = "hello,server!";
    outputStream.write(message.getBytes(StandardCharsets.UTF_8));
    //通过下面这句话告诉服务端,当前传输已经完成,而不是调用outputStream.close()方法
    //调用该方法后,不能再写数据,否则会报错,如果需要重新再写数据,那么需要再次新建socket
    socket.shutdownOutput();

    InputStream inputStream = socket.getInputStream();
    byte[] bytes = new byte[1024];
    int len;
    StringBuilder stringBuilder = new StringBuilder();
    while ((len = inputStream.read(bytes)) != -1) {
      stringBuilder.append(new String(bytes, 0, len, StandardCharsets.UTF_8));
    }
    System.out.println("get message from server:" + stringBuilder.toString());
    inputStream.close();
    outputStream.close();
    socket.close();
  }
}
