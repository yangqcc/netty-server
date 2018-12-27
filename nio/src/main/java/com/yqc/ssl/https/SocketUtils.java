package com.yqc.ssl.https;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-12-24
 * @modified By yangqc
 */
public class SocketUtils {

    public static void close(Socket s) {
        try {
            s.shutdownInput();
            s.shutdownOutput();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static byte[] readBytes(DataInputStream in, int length) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int l = 0;
        while ((l = in.read(data)) != -1) {
            output.write(data, 0, l);
        }
        return output.toByteArray();
    }

    public static void writeBytes(DataOutputStream out, byte[] bytes, int length) throws IOException {
        //out.writeInt(length);
        out.write(bytes);
        out.flush();
    }
}
