package ru.nsu.fit.networks.lab2.smartstreams;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static ru.nsu.fit.networks.lab2.util.Utils.INT_SIZE_BYTES;

public class SmartOutputStream extends BufferedOutputStream {

    public SmartOutputStream(OutputStream out) {
        super(out);
    }

    public void writeInt(int input) throws IOException {
        byte[] fileNameSize = ByteBuffer.allocate(INT_SIZE_BYTES).putInt(input).array();
        this.write(fileNameSize);
    }

    public void writeStringUTF8(String input) throws IOException {
        byte[] buffer = input.getBytes(StandardCharsets.UTF_8);
        this.write(buffer);
    }

}
