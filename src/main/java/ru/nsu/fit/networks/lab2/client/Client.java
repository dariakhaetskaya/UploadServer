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
        int segmentSize;
        while ((segmentSize = fileInputStream.read(buffer, 0, BUFFER_SIZE)) != -1){
//            socketOutputStream.writeInt(segmentSize);
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

            String fileName = file.getName();
//            byte[] fileNameSize = ByteBuffer.allocate(INT_SIZE_BYTES).putInt(fileName.length).array();
            outputSteam.sendInt(fileName.getBytes(StandardCharsets.UTF_8).length);
            outputSteam.sendUTF(fileName);
            System.out.println("sending: " + fileName);


            if (inputStream.readInt() != SUCCESSFUL_FILENAME_TRANSFER){
                System.err.println("Error transferring filename");
                fileInputStream.close();
                outputSteam.close();
                socket.shutdownOutput();
                return;
            }

            long fileSize = Files.size(path);
            outputSteam.sendLong(fileSize);

            UploadFile(fileInputStream, outputSteam);

            int transferStatus = inputStream.readInt();
            if (transferStatus == FILE_TRANSFER_FAILURE){
                System.out.println("FILE TRANSFER FAILED");
            } else if (transferStatus == SUCCESSFUL_FILE_TRANSFER) {
                System.out.println("FILE TRANSFERRED SUCCESSFULLY");
            }

//            fileInputStream.close();
//            socketOutputStream.close();
            socket.shutdownOutput();
//            socketInputStream.close();
            socket.shutdownInput();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
