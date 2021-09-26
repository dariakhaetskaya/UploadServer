package ru.nsu.fit.networks.lab2.smartsocket;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static ru.nsu.fit.networks.lab2.util.Protocol.INT_SIZE_BYTES;
import static ru.nsu.fit.networks.lab2.util.Protocol.LONG_SIZE_BYTES;

public class MyInputStream extends FilterInputStream {
    public MyInputStream(InputStream in) {
        super(in);
    }

//    public int readInt() throws IOException {
//        byte[] buffer = new byte[INT_SIZE_BYTES];
//        int readBytes = read(buffer, 0, INT_SIZE_BYTES);
//        System.out.println("got" + Arrays.toString(buffer));
//        System.out.println("read " + readBytes + " byte(s). Int = " + ByteBuffer.wrap(buffer).getInt());
//        if (readBytes != INT_SIZE_BYTES){
//            throw new IOException("failed to read int from socket");
//        }
//        return ByteBuffer.wrap(buffer).getInt();
//    }

    public int readInt() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    public final void readFully(byte b[], int off, int len) throws IOException {
        if (len < 0)
            throw new IndexOutOfBoundsException();
        int n = 0;
        while (n < len) {
            int count = in.read(b, off + n, len - n);
            if (count < 0)
                throw new EOFException();
            n += count;
        }
    }

//    public final long readLong() throws IOException {
//        byte readBuffer[] = new byte[8];
//
//        readFully(readBuffer, 0, 8);
//        return (((long)readBuffer[0] << 56) +
//                ((long)(readBuffer[1] & 255) << 48) +
//                ((long)(readBuffer[2] & 255) << 40) +
//                ((long)(readBuffer[3] & 255) << 32) +
//                ((long)(readBuffer[4] & 255) << 24) +
//                ((readBuffer[5] & 255) << 16) +
//                ((readBuffer[6] & 255) <<  8) +
//                ((readBuffer[7] & 255) <<  0));
//    }

    public long readLong() throws IOException {
        byte[] buffer = new byte[LONG_SIZE_BYTES];
        readFully(buffer, 0, LONG_SIZE_BYTES);
//        if (readBytes != LONG_SIZE_BYTES){
//            throw new IOException("failed to read long from socket");
//        }
        return ByteBuffer.wrap(buffer).getLong();
    }

    public String readUTF(int length) throws IOException {
        byte[] buffer = new byte[length];
        readFully(buffer, 0, length);
        System.out.println("readUTF::" + new String(buffer, StandardCharsets.UTF_8));
//        if (readBytes != length){
//            throw new IOException("failed to read UTF string from socket");
//        }
        return new String(buffer, StandardCharsets.UTF_8);
    }

}
