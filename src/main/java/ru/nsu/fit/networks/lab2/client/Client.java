package ru.nsu.fit.networks.lab2.client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static ru.nsu.fit.networks.lab2.util.Utils.*;

public class Client implements Runnable {
    private final File file;
    private final InetAddress serverIP;
    private final int serverPort;

    public Client(String passedFileName, InetAddress serverIP, int serverPort) throws FileNotFoundException {
        Path fileName = Paths.get(passedFileName);
        if (!Files.exists(fileName)){
            throw new FileNotFoundException("can't find " + fileName);
        }
        file = new File(fileName.toUri());
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    private void UploadFile(FileInputStream fileInputStream, BufferedOutputStream socketOutputStream) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int segmentSize;
        while ((segmentSize = fileInputStream.read(buffer)) != -1){
            socketOutputStream.write(segmentSize);
            socketOutputStream.write(buffer);
        }
        socketOutputStream.flush();
    }

    @Override
    public void run() {
        try(Socket socket = new Socket(serverIP, serverPort);
            BufferedOutputStream socketOutputStream = new BufferedOutputStream(socket.getOutputStream());
            FileInputStream fileInputStream = new FileInputStream(file);
            DataInputStream socketInputStream = new DataInputStream(socket.getInputStream());
            ){
            byte[] fileName = file.getName().getBytes(StandardCharsets.UTF_8);
            socketOutputStream.write(fileName.length);
            socketOutputStream.write(fileName);

            UploadFile(fileInputStream, socketOutputStream);
            fileInputStream.close();
            socket.shutdownOutput();

            int transferStatus = socketInputStream.readInt();
            if (transferStatus == FILE_TRANSFER_FAILURE){
                System.out.println("FILE TRANSFER FAILED");
            } else if (transferStatus == SUCCESSFUL_FILE_TRANSFER) {
                System.out.println("FILE TRANSFERRED SUCCESSFULLY");
            }
            socket.shutdownInput();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
