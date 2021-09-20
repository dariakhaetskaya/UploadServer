package ru.nsu.fit.networks.lab2.client;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException{
        if (args.length != 3){
            throw new IllegalArgumentException("Expected 3 arguments, got " + args.length);
        }

        String fileName = args[0];
        String hostName = args[1];
        int port = Integer.parseInt(args[2]);
    }
}
