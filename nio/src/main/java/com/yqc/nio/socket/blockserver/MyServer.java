package com.yqc.nio.socket.blockserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * <p>title:socket基本编程</p>
 * <p>description:socket编程,获取信息,并且向客户端发送信息 </p>
 *
 * @author yangqc
 * @date Created in 2018-11-21
 * @modified By yangqc
 */
public class MyServer {

  public static void main(String[] args) throws IOException {
    //监听端口
    int port = 8080;
    ServerSocket serverSocket = new ServerSocket();
    serverSocket.bind(new InetSocketAddress("localhost", port));
    Socket socket = serverSocket.accept();
    InputStream inputStream = socket.getInputStream();
    byte[] bytes = new byte[1024];
    int len;
    StringBuilder sb = new StringBuilder();
    while ((len = inputStream.read(bytes)) != -1) {
      sb.append(new String(bytes, 0, len, StandardCharsets.UTF_8));
    }
    System.out.println("get message from client:" + sb);

    OutputStream outputStream = socket.getOutputStream();
    outputStream.write("Hello client,I get message.".getBytes(StandardCharsets.UTF_8));
    inputStream.close();
    outputStream.close();
    socket.close();
    serverSocket.close();
  }
}
