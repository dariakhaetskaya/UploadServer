package ru.nsu.fit.networks.lab2.client;

import java.io.IOException;
import java.net.InetAddress;

public class ClientMain {
    public static void main(String[] args) throws IOException{
        if (args.length != 3){
            throw new IllegalArgumentException("Expected 3 arguments, got " + args.length
            + "\n" + "Arguments: [filename] [hostname] [port]");
        }

        String fileName = args[0];
        String hostName = args[1];
        int port = Integer.parseInt(args[2]);

        Client client = new Client(fileName, InetAddress.getByName(hostName), port);
        client.run();
    }
}
