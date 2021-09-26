package ru.nsu.fit.networks.lab2.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
    private final int port;

    Server(int port) throws IOException {
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true){
                Socket clientSocket = serverSocket.accept();
                FileTransporter fileTransporter = new FileTransporter(clientSocket);
                Thread fileTransportation = new Thread(fileTransporter);
                fileTransportation.start();
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize server on port " + port);
        }
    }
}
