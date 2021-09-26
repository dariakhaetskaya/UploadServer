package ru.nsu.fit.networks.lab2.smartsocket;

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

    public int readInt() throws IOException {
        byte[] buffer = new byte[INT_SIZE_BYTES];
        int readBytes = read(buffer, 0, INT_SIZE_BYTES);
        System.out.println("got" + Arrays.toString(buffer));
        System.out.println("read " + readBytes + " byte(s). Int = " + ByteBuffer.wrap(buffer).getInt());
        if (readBytes != INT_SIZE_BYTES){
            throw new IOException("failed to read int from socket");
        }
        return ByteBuffer.wrap(buffer).getInt();
    }

    public long readLong() throws IOException {
        byte[] buffer = new byte[LONG_SIZE_BYTES];
        int readBytes = read(buffer, 0, LONG_SIZE_BYTES);
        if (readBytes != LONG_SIZE_BYTES){
            throw new IOException("failed to read long from socket");
        }
        return ByteBuffer.wrap(buffer).getLong();
    }

    public String readUTF(int length) throws IOException {
        byte[] buffer = new byte[length];
        int readBytes = read(buffer, 0, length);
        if (readBytes != length){
            throw new IOException("failed to read UTF string from socket");
        }
        return new String(buffer, StandardCharsets.UTF_8);
    }

}
