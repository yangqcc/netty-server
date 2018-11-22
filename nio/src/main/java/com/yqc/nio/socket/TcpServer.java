package com.yqc.nio.socket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * <p>title:</p>
 * <p>description:最普通的阻塞服务器</p>
 *
 * @author yangqc
 * @date Created in 2018-11-04
 * @modified By yangqc
 */
public class TcpServer {

    public void server() throws IOException {
        ServerSocket serverSocket = new ServerSocket(10086);
        Socket socket = serverSocket.accept();
        //so表示Socket Option
        //socket关闭后,底层socket 10后再关闭,为了将数据发送出去
        socket.setSoLinger(true, 10);
        /**
         *  在默认情况下，客户端向服务器发送数据时，会根据数据包的大小决定是否立即发送。当数据包中的数据很少时，
         *  如只有1个字节，而数据包的头却有几十个字节（IP头+TCP头）时，系统会在发送之前先将较小的包合并到软大的包后，
         *  一起将数据发送出去。在发送下一个数据包时，系统会等待服务器对前一个数据包的响应，
         *  当收到服务器的响应后，再发送下一个数据包，这就是所谓的Nagle算法；在默认情况下，Nagle算法是开启的。
         *
         *  这种算法虽然可以有效地改善网络传输的效率，但对于网络速度比较慢，而且对实现性的要求比较高的情况下（如游戏、Telnet等），
         *  使用这种方式传输数据会使得客户端有明显的停顿现象。因此，最好的解决方案就是需要Nagle算法时就使用它，不需要时就关闭它。
         *  而使用setTcpToDelay正好可以满足这个需求。当使用setTcpNoDelay（true）将Nagle算法关闭后，客户端每发送一次数据，
         *  无论数据包的大小都会将这些数据发送出去。
         */
        socket.setTcpNoDelay(false);
        /**
         *  在默认情况下，输出流的发送缓冲区是8096个字节（8K）。
         *  这个值是Java所建议的输出缓冲区的大小。如果这个默认值不能满足要求，可以用setSendBufferSize方法来重新设置缓冲区的大小。
         *  但最好不要将输出缓冲区设得太小，否则会导致传输数据过于频繁，从而降低网络传输的效率。
         *
         *  如果底层的Socket实现不支持SO_SENDBUF选项，这两个方法将会抛出SocketException例外。
         *  必须将size设为正整数，否则setSendBufferedSize方法将抛出IllegalArgumentException例外。
         */
        socket.setSendBufferSize(1024);
        /**
         *  在默认情况下，输入流的接收缓冲区是8096个字节（8K）。这个值是Java所建议的输入缓冲区的大小。
         *  如果这个默认值不能满足要求，可以用setReceiveBufferSize方法来重新设置缓冲区的大小。
         *  但最好不要将输入缓冲区设得太小，否则会导致传输数据过于频繁，从而降低网络传输的效率。
         *
         *  如果底层的Socket实现不支持SO_RCVBUF选项，这两个方法将会抛出SocketException例外。
         *  必须将size设为正整数，否则setReceiveBufferSize方法将抛出IllegalArgumentException例外。
         */
        socket.setReceiveBufferSize(1024);
        /**
         * 如果将这个Socket选项打开，客户端Socket每隔段的时间（大约两个小时）就会利用空闲的连接向服务器发送一个数据包。
         * 这个数据包并没有其它的作用，只是为了检测一下服务器是否仍处于活动状态。如果服务器未响应这个数据包，
         * 在大约11分钟后，客户端Socket再发送一个数据包，如果在12分钟内，服务器还没响应，那么客户端Socket将关闭。
         * 如果将Socket选项关闭，客户端Socket在服务器无效的情况下可能会长时间不会关闭。
         * SO_KEEPALIVE选项在默认情况下是关闭的，可以使用如下的语句将这个SO_KEEPALIVE选项打开：
         */
        socket.setKeepAlive(false);
        /**
         *  如果这个Socket选项打开，可以通过Socket类的sendUrgentData方法向服务器发送一个单字节的数据。
         *  这个单字节数据并不经过输出缓冲区，而是立即发出。虽然在客户端并不是使用OutputStream向服务器发送数据，但在服务端程序中这个单字节的数据是和其它的普通数据混在一起的。
         *  因此，在服务端程序中并不知道由客户端发过来的数据是由OutputStream还是由sendUrgentData发过来的。下面是sendUrgentData方法的声明：
         */
        socket.setOOBInline(true);
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
