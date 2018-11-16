package com.yqc.nio.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-11-04
 * @modified By yangqc
 */
public class TcpServer {

  public void server() throws IOException {
    ServerSocket serverSocket = new ServerSocket(10086);
    Socket socket = serverSocket.accept();
    InputStream inputStream = socket.getInputStream();
    InputStreamReader reader = new InputStreamReader(inputStream);
    BufferedReader bufferedReader = new BufferedReader(reader);
    String info;
    while ((info = bufferedReader.readLine()) != null && !"".equals(info)) {
      System.out.println("我是服务器,客户端说:" + info);
    }
    //关闭输入流
    socket.shutdownInput();
    OutputStream os = socket.getOutputStream();
    PrintWriter pw = new PrintWriter(os);
    pw.write("欢迎您!");
    pw.flush();
    //关闭资源
    pw.close();
    os.close();
    bufferedReader.close();
    reader.close();
    inputStream.close();
    socket.close();
    serverSocket.close();
  }

  public static void main(String[] args) throws IOException {
    new TcpServer().server();
  }
}
