package com.yqc.nio.socket.chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * 发送方
 *
 * @author yangqc
 */
public class Send implements Runnable {

    DatagramSocket ds;
    DatagramPacket dp = null;

    public Send(DatagramSocket ds) {
        this.ds = ds;
    }

    @Override
    public void run() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(System.in));
            String str;
            while ((str = br.readLine()) != null) {
                byte[] buf = str.getBytes();
                dp = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(), 9999);
                ds.send(dp);
                if ("over".equalsIgnoreCase(str)) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
