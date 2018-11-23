package com.yqc.nio.socket.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * UDP客户端
 *
 * @author yangqc
 */
public class UdpClient {

    private DatagramSocket clientSocket;

    public String toUpperRemote(String serverIp, int serverPort, String str) {
        String recvStr = "";
        try {
            clientSocket = new DatagramSocket();
            byte[] sendBuf = str.getBytes();
            InetAddress inetAddress = InetAddress.getByName(serverIp);
            DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length
                    , inetAddress, serverPort);
            clientSocket.send(sendPacket);
            byte[] recvBuf = new byte[UdpServer.MAX_BTES];
            DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
            clientSocket.receive(recvPacket);
            recvStr = new String(recvPacket.getData(), 0, recvPacket.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return recvStr;
    }

    public static void main(String[] args) {
        System.out.println(new UdpClient().toUpperRemote("127.0.0.1", 8888, "abc"));
    }
}
