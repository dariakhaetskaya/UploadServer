package ru.nsu.fit.networks.lab2.smartstreams;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static ru.nsu.fit.networks.lab2.util.Protocol.INT_SIZE_BYTES;

public class SmartInputStream extends BufferedInputStream {

    public SmartInputStream(InputStream in) {
        super(in);
    }

    public int readInt() throws IOException {
        byte[] buffer = new byte[INT_SIZE_BYTES];
        if (this.read(buffer) != INT_SIZE_BYTES){
            throw new IOException("failed to read INT");
        }
        return ByteBuffer.wrap(buffer).getInt();
    }

    public String readStringUTF8(int length) throws IOException {
        byte[] buffer = new byte[length];
        if (this.read(buffer, 0, length) != length){
            throw new IOException("Failed to read client's file name");
        }
        return new String(buffer, StandardCharsets.UTF_8);
    }
}
