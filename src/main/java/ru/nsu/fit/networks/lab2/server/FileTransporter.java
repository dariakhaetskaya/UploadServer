package ru.nsu.fit.networks.lab2.server;

import ru.nsu.fit.networks.lab2.smartstreams.SmartInputStream;
import ru.nsu.fit.networks.lab2.smartstreams.SmartOutputStream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class FileTransporter implements Runnable {
    private final Socket socket;

    public FileTransporter(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        try (SmartOutputStream outputStream = new SmartOutputStream(socket.getOutputStream());
             SmartInputStream inputStream = new SmartInputStream(socket.getInputStream())){
            int fileNameSize = inputStream.readInt();
        } catch (IOException e) {
            System.err.println("Connection error:");
            e.printStackTrace(System.err);
        }
    }
}
