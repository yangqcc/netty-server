package com.yqc.nio.socket.chat;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * @author yangqc
 */
public class Receive implements Runnable {

    DatagramSocket ds;
    DatagramPacket dp = null;

    public Receive(DatagramSocket ds) {
        this.ds = ds;
    }

    @Override
    public void run() {
        try {
            while (true) {
                byte[] buf = new byte[1024];
                dp = new DatagramPacket(buf, buf.length);
                ds.receive(dp);
                String id = dp.getAddress().getHostAddress();
                int port = dp.getPort();
                String str = new String(dp.getData(), 0, dp.getLength());
                if ("over".equalsIgnoreCase(str)) {
                    System.out.println("对方离开聊天室!");
                    break;
                }
                System.out.println("ip:--" + id + ",端口");
            }
        } catch (Exception e) {

        }

    }
}
