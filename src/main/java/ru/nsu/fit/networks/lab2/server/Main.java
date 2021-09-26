package ru.nsu.fit.networks.lab2.server;

import java.io.IOException;

public class Main {
    public static void main(String[] args){
        if (args.length != 1){
            throw new IllegalArgumentException("Expected 1 argument : port. Got " + args.length);
        }
        int port = Integer.parseInt(args[0]);
        try {
            Server server = new Server(port);
            server.run();
        } catch (IOException e){
            System.out.println("Invalid Port");
        }
    }
}
