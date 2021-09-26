package ru.nsu.fit.networks.lab2.client;

import ru.nsu.fit.networks.lab2.smartstreams.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static ru.nsu.fit.networks.lab2.util.Protocol.*;

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

    private void UploadFile(FileInputStream fileInputStream, SmartOutputStream socketOutputStream) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int segmentSize;
        while ((segmentSize = fileInputStream.read(buffer)) != -1){
            socketOutputStream.writeInt(segmentSize);
            socketOutputStream.write(buffer);
        }
        socketOutputStream.flush();
    }

    @Override
    public void run() {
        try(Socket socket = new Socket(serverIP, serverPort);
//            BufferedOutputStream socketOutputStream = new BufferedOutputStream(socket.getOutputStream());
            SmartOutputStream socketOutputStream = new SmartOutputStream(socket.getOutputStream());
            FileInputStream fileInputStream = new FileInputStream(file);
            SmartInputStream socketInputStream = new SmartInputStream(socket.getInputStream());){

            byte[] fileName = file.getName().getBytes(StandardCharsets.UTF_8);
//            byte[] fileNameSize = ByteBuffer.allocate(INT_SIZE_BYTES).putInt(fileName.length).array();
//            socketOutputStream.write(fileNameSize);
            socketOutputStream.write(fileName);
            socketOutputStream.writeInt(fileName.length);

            UploadFile(fileInputStream, socketOutputStream);
            fileInputStream.close();
            socketOutputStream.close();
            socket.shutdownOutput();

            int transferStatus = socketInputStream.readInt();
            if (transferStatus == FILE_TRANSFER_FAILURE){
                System.out.println("FILE TRANSFER FAILED");
            } else if (transferStatus == SUCCESSFUL_FILE_TRANSFER) {
                System.out.println("FILE TRANSFERRED SUCCESSFULLY");
            }

            socketInputStream.close();
            socket.shutdownInput();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
