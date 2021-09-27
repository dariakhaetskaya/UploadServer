package ru.nsu.fit.networks.lab2.client;

import ru.nsu.fit.networks.lab2.smartsocket.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static ru.nsu.fit.networks.lab2.util.Protocol.*;

public class Client implements Runnable {
    private final Path path;
    private final File file;
    private final InetAddress serverIP;
    private final int serverPort;

    public Client(String passedFileName, InetAddress serverIP, int serverPort) throws FileNotFoundException {
        this.path = Paths.get(passedFileName);
        if (!Files.exists(path)){
            throw new FileNotFoundException("can't find " + path);
        }
        file = new File(path.toUri());
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    private void UploadFile(FileInputStream fileInputStream, MyOutputStream socketOutputStream) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int segmentSize; // size of an individual piece of data that can be less than size of the buffer
        while ((segmentSize = fileInputStream.read(buffer, 0, BUFFER_SIZE)) != -1){
            socketOutputStream.sendInt(segmentSize);
            socketOutputStream.send(buffer, segmentSize);
            socketOutputStream.flush();
        }
    }

    @Override
    public void run() {
        try(Socket socket = new Socket(serverIP, serverPort);
            MyOutputStream outputSteam = new MyOutputStream(socket.getOutputStream());
            MyInputStream inputStream = new MyInputStream(socket.getInputStream());
            FileInputStream fileInputStream = new FileInputStream(file)){

            // get the filename, send its length and then the name itself
            String fileName = file.getName();
            outputSteam.sendInt(fileName.getBytes(StandardCharsets.UTF_8).length);
            outputSteam.sendUTF(fileName);
            System.out.println("Uploading: " + fileName);

            // if length that we send differs from the one we've got, it's an error
            if (inputStream.readInt() != SUCCESSFUL_FILENAME_TRANSFER){
                System.err.println("Error transferring filename");
                fileInputStream.close();
                outputSteam.close();
                socket.close();
                return;
            }

            // send file size to the server
            long fileSize = Files.size(path);
            outputSteam.sendLong(fileSize);

            UploadFile(fileInputStream, outputSteam);
            fileInputStream.close();

            // get server's response weather the transfer was successful or not
            int transferStatus = inputStream.readInt();
            if (transferStatus == FILE_TRANSFER_FAILURE){
                System.out.println("FILE TRANSFER FAILED");
            } else if (transferStatus == SUCCESSFUL_FILE_TRANSFER) {
                System.out.println("FILE TRANSFERRED SUCCESSFULLY");
            }

            socket.shutdownOutput();
            socket.shutdownInput();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

