package com.yqc.nio.socket.udp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * udp包收发
 *
 * @author yangqc
 */
public class DatagramChannelTest {

    public void serve() throws IOException {
        //打开channel
        DatagramChannel channel = DatagramChannel.open();
        //绑定地址
        channel.socket().bind(new InetSocketAddress(InetAddress.getLocalHost(), 9999));
        //分配一兆空间
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.clear();
        //通道读入数据到缓存
        channel.read(byteBuffer);

    }
}