package com.x_tornado10.lccp.util;

import com.x_tornado10.lccp.LCCP;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class Networking {

    // try to check a provided IPv4 for connectivity using the ping command in the linux terminal
    // this is kind of a workaround since the default Java function normally used for this (InetAddress.isReachable()) requires root to work properly
    // also doing it this way is way more robust since it uses a real ping utility instead of echo port 7 like InetAddress.isReachable() function does
    // also, using echo port 7 can lead to false positives since most routers / firewalls are configured to respond to closed / non-existing addresses with a reset (RST) response,
    // this will lead to InetAddress.isReachable() returning true because it interprets any kind of response as reachable even though
    // the InetAddress.getByName() works just fine for host names though
    private static boolean ping(String ip, int timeout) {
        LCCP.logger.debug("Received ping request for '" + ip + "'" + " timeout: '"+ timeout + "'");
        try {
            // formatting ping command with specified timeout and IPv4 / host name
            LCCP.logger.debug("Formatting ping command...");
            List<String> command = new ArrayList<>();
            command.add("ping");
            command.add("-W" + timeout);
            command.add("-c1");
            command.add(ip);
            LCCP.logger.debug("Formatting complete! Command: " + command);

            // creating a new process with the specified arguments above using process builder
            LCCP.logger.debug("Creating new process...");
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            // starting the process
            Process process = processBuilder.start();
            LCCP.logger.debug("Created and started new process!");

            LCCP.logger.debug("Command output: ");
            LCCP.logger.debug("------------------- PING -------------------");
            // reading console feedback using a buffered reader
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            List<String> output = new ArrayList<>();
            // storing single feedback lines in a String list
            while ((line = reader.readLine()) != null) {
                // displaying the command output in the console for debugging purposes
                LCCP.logger.debug(line);
                output.add(line);
            }
            LCCP.logger.debug("--------------------------------------------");
            // waiting for the ping process to complete or time out
            process.waitFor();
            // iterating through the command output and checking if it contains a specific String that indicates that the ping was successful
            for (String s : output) {
                if (s.toLowerCase().contains("64 bytes")) {
                    // if the specific string ('64 bytes' in this case) is found the function returns true
                    LCCP.logger.debug("Ping was successful!");
                    LCCP.logger.debug("Command complete.");
                    return true;
                }
            }

        } catch (IOException | InterruptedException e) {
            // if an exception is thrown due to an UnknownHostException / InterruptedException or any kind of IoException,
            // it returns false
            LCCP.logger.debug("Ping failed!");
            LCCP.logger.debug("Command complete.");
            return false;
        }
        // if the string isn't found and no exception is thrown the ping command executed but timed out so the function also returns false
        LCCP.logger.debug("Ping failed!");
        LCCP.logger.debug("Command complete.");
        return false;
    }



    // extension of isValidIP with option to return IPv4 or host name
    // exception is thrown to enable custom error handling
    public static String getValidIP(String ip, boolean ipify) throws IOException {
        LCCP.logger.debug("Fulfilling ping request for: '" + ip + "'");
        String ipv4;
        try {
            // creating new InetAddress to hold the IPv4 / host name
            InetAddress host;
            // check if the specified string is a valid IPv4 address (matches format)
            if (isValidIP(ip)) {
                // try to ping ip with custom ping function
                // if the ping times out or fails throw new UnknownHostException
                // this is done so the error can be handled differently for different use cases
                if (!ping(ip, 3)) throw new UnknownHostException("Connection timed out!");

                // if the ping is successful the IPv4 is pares by InetAddress
                host = InetAddress.ofLiteral(ip);
            } else {
                // tries to get IPv4 from a host name using InetAddress integrated getByName() function
                host = InetAddress.getByName(ip);
            }

            // gets the IPv4 address from the InetAddress object
            ipv4 = host.getHostAddress();
        } catch (IOException e) {
            // if any exception occur the program will display some standard messages in the console
            LCCP.logger.debug("Ping failed!");
            LCCP.logger.debug(e.getMessage());
            LCCP.logger.warn("Invalid host name or IPv4: '" + ip + "'");
            // the exception is thrown again to enable for custom error handling later
            throw e;
        }
        // ping results are displayed in the console
        LCCP.logger.debug("Ping success!");
        LCCP.logger.debug("Host name: '" + ip + "'");
        LCCP.logger.debug("Detected IPv4: '" + ipv4 + "'");
        // return the ip or the host name based on 'ipify' param
        return ipify ? ipv4 : ip;
    }
    // validate an IP4 address format
    public static boolean isValidIP(final String ip) {
        String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
        return ip.matches(PATTERN);
    }
    // validate Port number format
    public static boolean isValidPORT(final String port) {
        String PATTERN = "^([1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$";

        return port.matches(PATTERN);
    }

    // custom file sender that sends a file to a server using java sockets
    public static class FileSender {
        // send file to server using sockets
        public static boolean sendFileToServer(String serverIP4, int serverPort, String fileToSendPath) {

            // loading file to memory
            File fileToSend = new File(fileToSendPath);

            //displaying file metadata to console
            LCCP.logger.debug("Received request to send '" + fileToSend.getAbsolutePath() +"' to " + serverIP4 + ":" + serverPort + "!");
            LCCP.logger.debug("Inspecting file...");
            LCCP.logger.debug("File name: " + fileToSend.getName());
            LCCP.logger.debug("File size: " + ((fileToSend.length() / 1024) / 1024) + "MB");
            LCCP.logger.debug("File type: " + fileToSend.getName().split("\\.")[1].toUpperCase());
            LCCP.logger.info("Sending File: '" + fileToSend.getAbsolutePath() + "' to " + serverIP4 + ":" + serverPort);


            try {
                LCCP.logger.debug("Opening new socket: " + serverIP4 + ":" + serverPort);
                // creating new socket
                Socket socket = new Socket(serverIP4, serverPort);
                LCCP.logger.debug("Successfully established connection!");

                LCCP.logger.debug("Opening output streams...");
                // getting the sockets output stream to transfer data
                OutputStream outputStream = socket.getOutputStream();
                LCCP.logger.debug("Successfully opened output streams!");

                // sending file metadata
                LCCP.logger.debug("Sending file metadata...");
                LCCP.logger.debug("Sending file name...");
                // sending file name
                outputStream.write(fileToSend.getName().strip().getBytes());
                LCCP.logger.debug("Sending file size...");
                // sending file size in bytes
                outputStream.write(String.valueOf(fileToSend.length()).getBytes());
                // flushing steam to make sure the server received all the metadata before the file contents are sent in the next step
                outputStream.flush();
                LCCP.logger.debug("Successfully send file metadata!");

                LCCP.logger.debug("Opening new FileInputStream to read the main file content...");
                // creating new file input stream to read the file contents
                FileInputStream fileInputStream = new FileInputStream(fileToSend);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                LCCP.logger.debug("Defining new 8129B buffer...");
                // defining new 8KB buffer to store data while reading / writing
                byte[] buffer = new byte[8192];
                LCCP.logger.debug("Successfully created new BufferedInputStream for the FileInputStream!");
                int count;

                // getting exact file size
                long fileSize = fileToSend.length();
                long transferredSize = 0;
                long lastTransferredSize = 0;

                // calculating the file size in MB
                double temp = (double) fileSize / (1024 * 1024);
                double mbFileSize = (double) Math.round(temp * 1000) / 1000;
                double mbTransferredSize = (double) transferredSize / (1024 * 1024);

                long delay = 1000;
                long lastDisplay = System.currentTimeMillis() - 1000;

                LCCP.logger.debug("Sending main file contents...");
                // reading / writing file contents using the buffer
                // the app also calculates transfer speed in MB/S and ETA
                // additionally the app keeps track on how much data was already transferred
                while ((count = bufferedInputStream.read(buffer)) > 0) {
                    if (System.currentTimeMillis() - lastDisplay >= delay) {
                        // calculating speed, eta and transferred data
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

                        // displaying transfer information message in the console containing speed, eta, file size and transferred data
                        LCCP.logger.debug("Transferring File: " + mbTransferredSize + "MB / " + mbFileSize + "MB -- " + (double) Math.round(percent * 1000) / 1000 + "% -- Speed: " + mbPerSecond + "MB/S -- ETA: " + result.toString().trim());
                        lastTransferredSize = transferredSize;
                    }
                    // writing buffer to output stream and sending it with socket
                    outputStream.write(buffer, 0, count);
                    // keeping track of transferred size
                    transferredSize += buffer.length;
                    // calculating transferred size in MB
                    double temp1 = (double) transferredSize / (1024 * 1024);
                    mbTransferredSize = (double) Math.round(temp1 * 1000) / 1000;
                }
                // flushing output stream to make sure all remaining data is sent to the server to prevent data getting stuck in buffers
                outputStream.flush();
                LCCP.logger.debug("Successfully send file contents!");

                LCCP.logger.debug("Closing socket and streams...");
                // closing socket and all streams to free up system resources
                bufferedInputStream.close();
                outputStream.close();
                socket.close();
                LCCP.logger.debug("Successfully closed socket and streams!");
                LCCP.logger.debug("Sending complete!");
            } catch (IOException e) {
                return false;
            }
            LCCP.logger.info("Successfully send file to server!");
            return true;
        }
    }
}
