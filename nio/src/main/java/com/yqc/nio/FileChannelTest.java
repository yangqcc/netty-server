package com.yqc.nio;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * <p>title:</p>
 * <p>description:当向buffer写入数据时,buffer会记录写下了多少数据。
 * 一旦要读取数据,需要通过flip()方法将Buffer从写模式切换到读模式。在读模式下，
 * 可以读取之前写入到buffer的所有数据。一旦读完了所有的数据，就需要清空缓存区，让它可以再次
 * 被写入。有两种方式能清空缓存区:调用clear()或者compact()方法,clear()方法会清空整个缓存区，compact()
 * 方法只会清除已经读过的数据任何未读的数据都被移到缓存区的起始处,新写入的数据将被方法哦缓冲区未读数据的后面。
 * </p>
 *
 * @author yangqc
 * @date Created in 2018-07-08
 * @modified By yangqc
 */
public class FileChannelTest {

    public static void main(String[] args) throws IOException {
        RandomAccessFile aFile = new RandomAccessFile("nio-data.txt", "rw");
        FileChannel inChannel = aFile.getChannel();
        ByteBuffer buf = ByteBuffer.allocate(48);

        int bytesRead = inChannel.read(buf);
        while (bytesRead != -1) {
            System.out.println("Read " + bytesRead);
            buf.flip();
            while (buf.hasRemaining()) {
                System.out.println((char) buf.get());
            }
            buf.clear();
            bytesRead = inChannel.read(buf);
        }
        aFile.close();
    }

}
