package com.yqc.nio.socket;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * <p>title:</p>
 * <p>description:未使用netty的网络阻塞编程</p>
 *
 * @author yangqc
 * @date Created in 2018-06-03
 * @modified By yangqc
 */
public class PlainOioServer {

  /**
   * 1.绑定服务器到指定的端口。
   *
   * 2.接受一个连接。
   *
   * 3.创建一个新的线程来处理连接。
   *
   * 4.将消息发送到连接的客户端。
   *
   * 5.一旦消息被写入和刷新时就 关闭连接。
   *
   * 6.启动线程。
   */
  public void serve(int port) throws IOException {
    final ServerSocket socket = new ServerSocket(port);
    try {
      for (; ; ) {
        final Socket clientSocket = socket.accept();
        System.out.println("Accepted connection from" + clientSocket);
        new Thread(() -> {
          OutputStream out;
          try {
            Thread.sleep(2000);
            out = clientSocket.getOutputStream();
            out.write("Hi!\r\n".getBytes(Charset.forName("UTF-8")));
            out.flush();
            clientSocket.close();
          } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            try {
              clientSocket.close();
            } catch (IOException ex) {
              ex.printStackTrace();
            }
          }
        }).start();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws IOException {
    new PlainOioServer().serve(8080);
  }
}
