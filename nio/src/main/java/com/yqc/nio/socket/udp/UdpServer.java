package com.yqc.nio.socket.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * udp服务器 UDP和HTTP不一样,http是数据流读取数据,UDP是数据包读取数据
 *
 * @author yangqc
 */
public class UdpServer {

    /**
     * 服务器地址
     */
    public static final String SERVER_IP = "127.0.0.1";

    /**
     * 端口号
     */
    public static final int SERVER_PORT = 8888;

    /**
     * 最多处理1024个字符
     */
    public static final int MAX_BTES = 1024;

    /**
     * UDP使用DatagramSocket发送数据包
     */
    private DatagramSocket serverSocket;

    /**
     * 启用服务器
     *
     * @param serverIp
     * @param serverPort
     */
    public void startServer(String serverIp, int serverPort) {
        try {
            InetAddress inetAddress = InetAddress.getByName(serverIp);
            serverSocket = new DatagramSocket(serverPort, inetAddress);
            byte[] recvBuf = new byte[MAX_BTES];
            DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
            while (true) {
                try {
                    serverSocket.receive(recvPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String recvStr = new String(recvPacket.getData(), 0, recvPacket.getLength());
                InetAddress clientAddr = recvPacket.getAddress();
                int clientPort = recvPacket.getPort();

                String upperStr = recvStr.toUpperCase();
                byte[] sendBuf = upperStr.getBytes();
                DatagramPacket sendPackage = new DatagramPacket(sendBuf, sendBuf.length, clientAddr, clientPort);
                serverSocket.send(sendPackage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
    }

    public static void main(String[] args) {
        new UdpServer().startServer(SERVER_IP, SERVER_PORT);
    }
}
