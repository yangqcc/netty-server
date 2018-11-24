package com.yqc.nio.socket.serverandclient;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-11-21
 * @modified By yangqc
 */
public class MultithreadingServer {

  public static void main(String[] args) throws IOException {
    ServerSocket serverSocket = new ServerSocket(8080);
    while (true) {
      Socket socket = serverSocket.accept();
      new Thread(()->{
        try {
          InputStream inputStream = socket.getInputStream();
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
    }
  }
}
