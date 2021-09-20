package ru.nsu.fit.networks.lab2.smartstreams;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static ru.nsu.fit.networks.lab2.util.Utils.INT_SIZE_BYTES;

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
}
