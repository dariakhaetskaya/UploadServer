package ru.nsu.fit.networks.lab2.server;

import ru.nsu.fit.networks.lab2.smartsocket.MyInputStream;
import ru.nsu.fit.networks.lab2.smartsocket.MyOutputStream;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import static ru.nsu.fit.networks.lab2.util.Protocol.*;
import static ru.nsu.fit.networks.lab2.util.Utils.*;

public class FileTransporter implements Runnable {
    private final Socket socket;

    public FileTransporter(Socket socket){
        this.socket = socket;
    }

    private Path createFile(String filename) throws IOException {
        Path filenamePath = Paths.get(filename);
        filename = filenamePath.getFileName().toString();
        Path uploadDir = Paths.get("uploads");
        if (!Files.exists(uploadDir)){
            Files.createDirectory(uploadDir);
        }
        Path path = Paths.get(uploadDir + System.getProperty("file.separator") + filename);
        if (Files.exists(path)){
            Random randomGenerator = new Random();
            path = Paths.get(uploadDir + System.getProperty("file.separator") + filename + "_" +
                    randomGenerator.nextInt());
        }
        System.out.println("creating file: " + path);
        Files.createFile(path);
        return path;
    }

    @Override
    public void run() {
        try (MyOutputStream outputStream = new MyOutputStream(socket.getOutputStream());
             MyInputStream inputStream = new MyInputStream(socket.getInputStream())){

            int fileNameSize = inputStream.readInt();
            System.out.println(ANSI_RED + "got filename size = " + fileNameSize);
            String filename = inputStream.readUTF(fileNameSize);
            System.out.println(ANSI_GREEN + "got filename = " + filename + ANSI_RESET);

            if (filename.length() != fileNameSize){
                outputStream.sendInt(FILENAME_SIZE_NOT_MATCH);
                outputStream.flush();
            }
            byte[] buff = ByteBuffer.allocate(INT_SIZE_BYTES).putInt(SUCCESSFUL_FILENAME_TRANSFER).array();
//            outputStream.sendInt(SUCCESSFUL_FILENAME_TRANSFER);
            outputStream.write(buff, 0, INT_SIZE_BYTES);
            outputStream.flush();

            long fileSize = inputStream.readLong();
            System.out.println("File size = " + fileSize);
            Path newFile = createFile(filename);

            try (OutputStream fileOutputStream = Files.newOutputStream(newFile)){
                long totalBytesRead = 0;
                long uploadStartedTime = System.currentTimeMillis();
                long lastSpeedMeasureTime = uploadStartedTime;
                long currentTime = uploadStartedTime;
                long readSinceLastTimeMeasure = 0;
                byte[] buffer = new byte[BUFFER_SIZE];
//                ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                int i = 0;
                while (totalBytesRead < fileSize){

                    int segmentSize = inputStream.readInt();
                    readSinceLastTimeMeasure += segmentSize;
//                    System.out.println("got" + segmentSize + "bytes");
                    inputStream.readFully(buffer,0, segmentSize);

                    totalBytesRead += segmentSize;
//                    System.out.println("SEGMENT #" + i++ + new String(buffer, StandardCharsets.UTF_8));
                    if (segmentSize > 0){
                        fileOutputStream.write(buffer, 0, segmentSize);
                        fileOutputStream.flush();
                    }
//                    buffer = buffer.clear();
                    currentTime = System.currentTimeMillis();
                    if (currentTime - lastSpeedMeasureTime > TIME_MEASURE_INTERVAL){

                        System.out.println("Uploading " + newFile.getFileName() + ":\n"
                                + ANSI_GREEN + "Instant speed: " + readSinceLastTimeMeasure * 1000 / (currentTime - lastSpeedMeasureTime) + " Byte/sec\n" + ANSI_RESET
                                + ANSI_CYAN + "Average speed: " + totalBytesRead * 1000 / (currentTime - uploadStartedTime) + " Byte/sec" + ANSI_RESET);
                        readSinceLastTimeMeasure = 0;
                        lastSpeedMeasureTime = currentTime;
                    }
                }
                try {
                    fileOutputStream.close();
                } catch (IOException e){
                    outputStream.sendInt(FILE_TRANSFER_FAILURE);
                    e.printStackTrace(System.err);
                }
                if (totalBytesRead == fileSize){
                    outputStream.sendInt(SUCCESSFUL_FILE_TRANSFER);
                    System.out.println("Finished uploading " + newFile.getFileName());
                } else {
                    outputStream.sendInt(FILE_TRANSFER_FAILURE);
                }
            } catch (Exception e){
                e.printStackTrace();
            }

        } catch (IOException e) {
            System.err.println("Connection error:");
            e.printStackTrace(System.err);
        }
    }
}

