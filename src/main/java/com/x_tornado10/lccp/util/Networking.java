package com.x_tornado10.lccp.util;

import com.x_tornado10.lccp.LCCP;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Networking {
    // validate an IP4 address format
    public static boolean isValidIP(final String ip) {
        String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
        return ip.matches(PATTERN);
    }
    // extension of isValidIP with option to return IPv4 or host name
    // exception is thrown to enable custom error handling
    public static String getValidIP(String ip, boolean ipify) throws UnknownHostException {
        LCCP.logger.debug("Fulfilling ping request for: '" + ip + "'");
        // check if IPv4 is a valid format
        if (isValidIP(ip)) {
            LCCP.logger.debug("IPv4 is valid: '" + ip + "'");
            return ip;
        }
        String ipv4;
        try {
            // try to get IPv4 from a host name with ping
            ipv4 = InetAddress.getByName(ip).getHostAddress();
        } catch (UnknownHostException e) {
            // some standard error handling
            LCCP.logger.debug("Ping failed!");
            LCCP.logger.debug(e.getMessage());
            LCCP.logger.warn("Invalid host name or IPv4: '" + ip + "'");
            throw e;
        }
        // console output of the result
        LCCP.logger.debug("Ping success!");
        LCCP.logger.debug("Host name: '" + ip + "'");
        LCCP.logger.debug("Detected IPv4: '" + ipv4 + "'");
        // return the ip or the host name based on the param 'ipify'
        return ipify ? ipv4 : ip;
    }
    // validate Port number format
    public static boolean isValidPORT(final String port) {
        String PATTERN = "^([1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$";

        return port.matches(PATTERN);
    }

    public static class FileSender {
        // send file to server using sockets
        public static boolean sendFileToServer(String serverIP4, int serverPort, String fileToSendPath) {

            File fileToSend = new File(fileToSendPath);

            LCCP.logger.debug("Received request to send '" + fileToSend.getAbsolutePath() +"' to " + serverIP4 + ":" + serverPort + "!");
            LCCP.logger.debug("Inspecting file...");
            LCCP.logger.debug("File name: " + fileToSend.getName());
            LCCP.logger.debug("File size: " + ((fileToSend.length() / 1024) / 1024) + "MB");
            LCCP.logger.debug("File type: " + fileToSend.getName().split("\\.")[1].toUpperCase());
            LCCP.logger.info("Sending File: '" + fileToSend.getAbsolutePath() + "' to " + serverIP4 + ":" + serverPort);


            try {
                LCCP.logger.debug("Opening new socket: " + serverIP4 + ":" + serverPort);
                // opening new socket
                Socket socket = new Socket(serverIP4, serverPort);
                LCCP.logger.debug("Successfully established connection!");

                LCCP.logger.debug("Opening output streams...");
                // opening the output streams used to transfer data
                OutputStream outputStream = socket.getOutputStream();
                LCCP.logger.debug("Successfully opened output streams!");

                // sending file metadata
                LCCP.logger.debug("Sending file metadata...");
                LCCP.logger.debug("Sending file name...");
                // sending file name
                LCCP.logger.fatal(fileToSend.getName());
                outputStream.write(fileToSend.getName().strip().getBytes());
                LCCP.logger.debug("Sending file size...");
                // sending file size in bytes
                LCCP.logger.fatal(String.valueOf(fileToSend.length()));
                outputStream.write(String.valueOf(fileToSend.length()).getBytes());
                outputStream.flush();
                LCCP.logger.debug("Successfully send file metadata!");

                LCCP.logger.debug("Opening new FileInputStream to read the main file content...");
                // opening file input stream to read main file content
                FileInputStream fileInputStream = new FileInputStream(fileToSend);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                // get socket output stream
                LCCP.logger.debug("Defining new 8129B buffer...");
                // defining new 8KB buffer
                byte[] buffer = new byte[8192];
                LCCP.logger.debug("Successfully created new BufferedInputStream for the FileInputStream!");
                int count;

                long fileSize = fileToSend.length();
                long transferredSize = 0;
                long lastTransferredSize = 0;

                double temp = (double) fileSize / (1024 * 1024);
                double mbFileSize = (double) Math.round(temp * 1000) / 1000;
                double mbTransferredSize = (double) transferredSize / (1024 * 1024);

                long delay = 1000;
                long lastDisplay = System.currentTimeMillis() - 1000;

                LCCP.logger.debug("Sending main file contents...");
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

                        LCCP.logger.debug("Transferring File: " + mbTransferredSize + "MB / " + mbFileSize + "MB -- " + (double) Math.round(percent * 1000) / 1000 + "% -- Speed: " + mbPerSecond + "MB/S -- ETA: " + result.toString().trim());
                        lastTransferredSize = transferredSize;
                    }
                    outputStream.write(buffer, 0, count);
                    transferredSize += buffer.length;
                    double temp1 = (double) transferredSize / (1024 * 1024);
                    mbTransferredSize = (double) Math.round(temp1 * 1000) / 1000;
                }
                outputStream.flush();
                LCCP.logger.debug("Successfully send file contents!");

                LCCP.logger.debug("Closing socket and streams...");
                // closing socket and all streams to free up resources
                bufferedInputStream.close();
                outputStream.close();
                socket.close();
                LCCP.logger.debug("Successfully closed socket and streams!");
                LCCP.logger.debug("Sending complete!");
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            LCCP.logger.info("Successfully send file to server!");
            return true;
        }
    }
}
