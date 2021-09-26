package ru.nsu.fit.networks.lab2.smartsocket;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import static ru.nsu.fit.networks.lab2.util.Protocol.INT_SIZE_BYTES;
import static ru.nsu.fit.networks.lab2.util.Protocol.LONG_SIZE_BYTES;

public class MyOutputStream extends FilterOutputStream {

    public MyOutputStream(OutputStream out) {
        super(out);
    }

    public void send(byte[] input) throws IOException {
        this.write(input);
        flush();
    }

    public void send(byte[] input, int byteCount) throws IOException {
        if (input.length == byteCount){
            this.write(input, 0, byteCount);
            flush();
            return;
        }
        byte[] buffer = new byte[byteCount];;
        if (input.length < byteCount){
            System.arraycopy(input, 0, buffer, 0, input.length);
        } else {
            buffer = Arrays.copyOf(input, byteCount);
        }
        this.write(buffer);
        flush();
    }

    public void sendUTF(String string) throws IOException {
        byte[] buffer = string.getBytes(StandardCharsets.UTF_8);
        send(buffer, buffer.length);
        flush();
    }

    public void sendInt(int num) throws IOException {
        byte[] buffer = ByteBuffer.allocate(INT_SIZE_BYTES).putInt(num).array();
        send(buffer, INT_SIZE_BYTES);
//        System.out.println("sending" + Arrays.toString(buffer));
        flush();
    }

    public void sendLong(long  num) throws IOException {
        byte[] buffer = ByteBuffer.allocate(LONG_SIZE_BYTES).putLong(num).array();
//        System.out.println("sending" + Arrays.toString(buffer));
        send(buffer, LONG_SIZE_BYTES);
//        flush();
    }
}
