package ru.nsu.fit.networks.lab2.mystreams;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static ru.nsu.fit.networks.lab2.util.Protocol.LONG_SIZE_BYTES;

public class MyInputStream extends FilterInputStream {
    public MyInputStream(InputStream in) {
        super(in);
    }

    public int readInt() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4));
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

    public long readLong() throws IOException {
        byte[] buffer = new byte[LONG_SIZE_BYTES];
        readFully(buffer, 0, LONG_SIZE_BYTES);
        return ByteBuffer.wrap(buffer).getLong();
    }

    public String readUTF(int length) throws IOException {
        byte[] buffer = new byte[length];
        readFully(buffer, 0, length);
        return new String(buffer, StandardCharsets.UTF_8);
    }

}
