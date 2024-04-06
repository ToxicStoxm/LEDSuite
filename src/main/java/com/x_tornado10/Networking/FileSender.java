package com.x_tornado10.Networking;

import java.io.*;
import java.net.Socket;

public class FileSender {
    public static void main() {
        String serverAddress = "127.0.0.1"; // Replace with the server IP address
        int serverPort = 12345; // Replace with the server port

        try {
            // Establish a connection to the server
            Socket socket = new Socket(serverAddress, serverPort);

            // Open input and output streams
            OutputStream outputStream = socket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            // Example file to send (replace with your file)
            File fileToSend = new File("example.mp4");

            // Send file metadata
            objectOutputStream.writeObject(fileToSend.getName());
            objectOutputStream.writeLong(fileToSend.length());

            // Send file content
            FileInputStream fileInputStream = new FileInputStream(fileToSend);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            OutputStream fileOutputStream = socket.getOutputStream();
            byte[] buffer = new byte[8192];
            int count;
            while ((count = bufferedInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, count);
            }

            // Close streams and socket
            bufferedInputStream.close();
            fileOutputStream.close();
            objectOutputStream.close();
            outputStream.close();
            socket.close();

            System.out.println("File sent successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

