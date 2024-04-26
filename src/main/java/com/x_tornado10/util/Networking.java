package com.x_tornado10.util;

import com.x_tornado10.Main;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class Networking {
    // validate an IP4 address
    public static boolean isValidIP(final String ip) {
        String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

        return ip.matches(PATTERN);
    }
    // validate Port number
    public static boolean isValidPORT(final String port) {
        String PATTERN = "^([1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$";

        return port.matches(PATTERN);
    }

    public static class FileSender {
        // send file to server using sockets
        public static boolean sendFileToServer(String serverIP4, int serverPort, String fileToSendPath) {

            File fileToSend = new File(fileToSendPath);

            Main.logger.debug("Received request to send '" + fileToSend.getAbsolutePath() +"' to " + serverIP4 + ":" + serverPort + "!");
            Main.logger.debug("Inspecting file...");
            Main.logger.debug("File name: " + fileToSend.getName());
            Main.logger.debug("File size: " + ((fileToSend.length() / 1024) / 1024) + "MB");
            Main.logger.debug("File type: " + fileToSend.getName().split("\\.")[1].toUpperCase());
            Main.logger.info("Sending File: '" + fileToSend.getAbsolutePath() + "' to " + serverIP4 + ":" + serverPort);


            try {
                Main.logger.debug("Opening new socket: " + serverIP4 + ":" + serverPort);
                // opening new socket
                Socket socket = new Socket(serverIP4, serverPort);
                Main.logger.debug("Successfully established connection!");

                Main.logger.debug("Opening output streams...");
                // opening the output streams used to transfer data
                OutputStream outputStream = socket.getOutputStream();
                Main.logger.debug("Successfully opened output streams!");

                // sending file metadata
                Main.logger.debug("Sending file metadata...");
                Main.logger.debug("Sending file name...");
                // sending file name
                Main.logger.fatal(fileToSend.getName());
                outputStream.write(fileToSend.getName().strip().getBytes());
                Main.logger.debug("Sending file size...");
                // sending file size in bytes
                Main.logger.fatal(String.valueOf(fileToSend.length()));
                outputStream.write(String.valueOf(fileToSend.length()).getBytes());
                outputStream.flush();
                Main.logger.debug("Successfully send file metadata!");

                Main.logger.debug("Opening new FileInputStream to read the main file content...");
                // opening file input stream to read main file content
                FileInputStream fileInputStream = new FileInputStream(fileToSend);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                // get socket output stream
                Main.logger.debug("Defining new 8129B buffer...");
                // defining new 8KB buffer
                byte[] buffer = new byte[8192];
                Main.logger.debug("Successfully created new BufferedInputStream for the FileInputStream!");
                int count;

                long fileSize = fileToSend.length();
                long transferredSize = 0;
                long lastTransferredSize = 0;

                double temp = (double) fileSize / (1024 * 1024);
                double mbFileSize = (double) Math.round(temp * 1000) / 1000;
                double mbTransferredSize = (double) transferredSize / (1024 * 1024);

                long delay = 1000;
                long lastDisplay = System.currentTimeMillis() - 1000;

                Main.logger.debug("Sending main file contents...");
                // reading and sending main file content using buffer
                while ((count = bufferedInputStream.read(buffer)) > 0) {
                    if (System.currentTimeMillis() - lastDisplay >= delay) {
                        long bytesTransferredLastSecond = transferredSize - lastTransferredSize;
                        double bytesPerSecond = (double) bytesTransferredLastSecond / ((double) delay / 1000);
                        double temp1 = bytesPerSecond / (1024 * 1024);
                        double mbPerSecond = (double) Math.round(temp1 * 1000) / 1000;
                        lastDisplay = System.currentTimeMillis();
                        double percent = ((double) transferredSize / fileSize) * 100;
                        double estimatedSecondsRemaining = (fileSize - transferredSize) / bytesPerSecond;
                        if (transferredSize == 0) estimatedSecondsRemaining = 0;

                        Date date = new Date((long) (estimatedSecondsRemaining * 1000));

                        // Create a SimpleDateFormat object for formatting
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                        sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Set timezone to UTC to avoid offset

                        // Format the date
                        String formattedTime = sdf.format(date);

                        String[] parts = formattedTime.split(":");

                        // Check each part and remove if it's zero
                        StringBuilder result = new StringBuilder();
                        if (!parts[0].equals("00")) {
                            result.append(parts[0]).append("h ");
                        }
                        if (!parts[1].equals("00")) {
                            result.append(parts[1]).append("m ");
                        }
                        result.append(parts[2]).append("s");

                        Main.logger.debug("Transferring File: " + mbTransferredSize + "MB / " + mbFileSize + "MB -- " + (double) Math.round(percent * 1000) / 1000 + "% -- Speed: " + mbPerSecond + "MB/S -- ETA: " + result.toString().trim());
                        lastTransferredSize = transferredSize;
                    }
                    outputStream.write(buffer, 0, count);
                    transferredSize += buffer.length;
                    double temp1 = (double) transferredSize / (1024 * 1024);
                    mbTransferredSize = (double) Math.round(temp1 * 1000) / 1000;
                }
                outputStream.flush();
                Main.logger.debug("Successfully send file contents!");

                Main.logger.debug("Closing socket and streams...");
                // closing socket and all streams to free up resources
                bufferedInputStream.close();
                outputStream.close();
                socket.close();
                Main.logger.debug("Successfully closed socket and streams!");
                Main.logger.debug("Sending complete!");
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            Main.logger.info("Successfully send file to server!");
            return true;
        }
    }
}
