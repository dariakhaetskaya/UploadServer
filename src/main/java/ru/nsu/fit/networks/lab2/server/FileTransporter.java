package ru.nsu.fit.networks.lab2.server;

import ru.nsu.fit.networks.lab2.mystreams.MyInputStream;
import ru.nsu.fit.networks.lab2.mystreams.MyOutputStream;

import java.io.*;
import java.net.Socket;
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
                    Math.abs(randomGenerator.nextInt()));
        }
        System.out.println("creating file: " + path);
        Files.createFile(path);
        return path;
    }

    @Override
    public void run() {
        try (MyOutputStream outputStream = new MyOutputStream(socket.getOutputStream());
             MyInputStream inputStream = new MyInputStream(socket.getInputStream())){

            // read filename length and filename
            int fileNameSize = inputStream.readInt();
            String filename = inputStream.readUTF(fileNameSize);
            System.out.println(ANSI_YELLOW + "Requested to upload new file: " + filename + ANSI_RESET);

            // if length that we send differs from the one we've got, it's an error
            if (filename.length() != fileNameSize){
                outputStream.sendInt(FILENAME_SIZE_NOT_MATCH);
                outputStream.flush();
            }

            // if everything OK send success flag
            outputStream.sendInt(SUCCESSFUL_FILENAME_TRANSFER);

            // get file size
            long fileSize = inputStream.readLong();

            //create file in Upload directory
            Path newFile = createFile(filename);

            // read data from the socket and write to the file
            try (OutputStream fileOutputStream = Files.newOutputStream(newFile)){
                long totalBytesRead = 0;
                long uploadStartedTime = System.currentTimeMillis();
                long lastSpeedMeasureTime = uploadStartedTime;
                long currentTime = uploadStartedTime;
                long readSinceLastTimeMeasure = 0;
                byte[] buffer = new byte[BUFFER_SIZE];

                while (totalBytesRead < fileSize){

                    int segmentSize = inputStream.readInt();
                    inputStream.readFully(buffer,0, segmentSize);

                    readSinceLastTimeMeasure += segmentSize;
                    totalBytesRead += segmentSize;

                    if (segmentSize > 0){
                        fileOutputStream.write(buffer, 0, segmentSize);
                        fileOutputStream.flush();
                    }

                    currentTime = System.currentTimeMillis();
                    if (currentTime - lastSpeedMeasureTime > TIME_MEASURE_INTERVAL){

                        System.out.println("Uploading " + newFile.getFileName() + ":\n"
                                + ANSI_GREEN + "Instant speed: "
                                + readSinceLastTimeMeasure * 1000 / (currentTime - lastSpeedMeasureTime) + " Byte/sec\n"
                                + ANSI_CYAN + "Average speed: " +
                                totalBytesRead * 1000 / (currentTime - uploadStartedTime) + " Byte/sec" + ANSI_RESET);

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
                outputStream.close();
                inputStream.close();
                socket.close();
            } catch (Exception e){
                e.printStackTrace();
            }

        } catch (IOException e) {
            System.err.println("Connection error:");
            e.printStackTrace(System.err);
        }
    }
}

