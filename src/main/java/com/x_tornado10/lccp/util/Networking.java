package com.x_tornado10.lccp.util;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.util.logging.Logger;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class Networking {

    // try to check a provided IPv4 for connectivity using the ping command in the linux terminal
    // this is kind of a workaround since the default Java function normally used for this (InetAddress.isReachable()) requires root to work properly
    // also doing it this way is way more robust since it uses a real ping utility instead of echo port 7 like InetAddress.isReachable() function does
    // also, using echo port 7 can lead to false positives since most routers / firewalls are configured to respond to closed / non-existing addresses with a reset (RST) response,
    // this will lead to InetAddress.isReachable() returning true because it interprets any kind of response as reachable even though
    // the InetAddress.getByName() works just fine for host names though
    private static boolean ping(String ip, int timeout) {
        LCCP.logger.debug("Received ping request for '" + ip + "'" + " timeout: '" + timeout + "'");
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
        return ip.matches(Paths.Patterns.IPV4);
    }

    // validate Port number format
    public static boolean isValidPORT(final String port) {
        LCCP.logger.debug("Fulfilling port validation request for: '" + port + "'");
        // valid port format
        boolean result = port.matches(Paths.Patterns.PORT);
        // print result to console
        if (result) {
            LCCP.logger.debug("Port validation successful!");
            LCCP.logger.debug("Port has valid format (Range: 1 - 65535)");
        } else {
            LCCP.logger.debug("Port validation has failed!");
            LCCP.logger.debug("Invalid port format: '" + port + "'");
            LCCP.logger.debug("Port needs to be a numerical value between 1 and 65535!");
        }

        return result;
    }

    // custom file sender that sends a file to a server using java sockets
    public static class FileSender {
        // send file to server using sockets
        public static boolean sendFile(String serverIP4, int serverPort, String fileToSendPath) {

            // loading file to memory
            File fileToSend = new File(fileToSendPath);

            // getting new network event id from networkLogger
            String id = "[" +
                    LCCP.networkLogger.getRandomUUID(
                            "[Client]" +
                                    "[Data Output]" +
                                    "[FILE]" +
                                    "[Destination '" + serverIP4 +"']" +
                                    "[Port '" + serverPort + "']"
                    ) +
                    "] ";

            //displaying file metadata to console
            LCCP.logger.debug(id + "-------------------- Network Communication --------------------");
            LCCP.logger.debug(id + "Received request to send '" + fileToSend.getAbsolutePath() + "' to " + serverIP4 + ":" + serverPort + "!");
            LCCP.logger.debug(id + "Inspecting file...");
            LCCP.logger.debug(id + "File name: " + fileToSend.getName());
            LCCP.logger.debug(id + "File size: " + ((fileToSend.length() / 1024) / 1024) + "MB");
            LCCP.logger.debug(id + "File type: " + fileToSend.getName().split("\\.")[1].toUpperCase());


            LCCP.logger.info(id + "Sending File: '" + fileToSend.getAbsolutePath() + "' to " + serverIP4 + ":" + serverPort);


            try {
                // open a new client socket for server:port
                LCCP.logger.debug(id + "Opening new socket: " + serverIP4 + ":" + serverPort);
                Socket socket = new Socket(serverIP4, serverPort);
                LCCP.logger.debug(id + "Successfully established connection!");

                // creating data streams
                LCCP.logger.debug(id + "Opening output streams...");
                DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
                LCCP.logger.debug(id + "Successfully opened output streams!");

                // sending file metadata
                LCCP.logger.debug(id + "Sending file metadata...");

                // sending file name
                LCCP.logger.debug(id + "Sending file name...");
                dout.write(fileToSend.getName().strip().getBytes());

                // sending file size
                LCCP.logger.debug(id + "Sending file size...");
                dout.writeLong(fileToSend.length());

                // flushing steam to make sure the server received all the metadata before the file contents are sent in the next step
                dout.flush();

                LCCP.logger.debug(id + "Successfully send file metadata!");

                // creating new file input stream to read the file contents
                LCCP.logger.debug(id + "Opening new FileInputStream to read the main file content...");
                FileInputStream fileInputStream = new FileInputStream(fileToSend);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                LCCP.logger.debug(id + "Successfully created new BufferedInputStream for the FileInputStream!");

                // defining new 8KB buffer to store data while reading / writing
                LCCP.logger.debug(id + "Defining new 8129B buffer...");
                byte[] buffer = new byte[8192];

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

                LCCP.logger.debug(id + "Sending main file contents...");
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
                        LCCP.logger.debug(id + "Transferring File: " + mbTransferredSize + "MB / " + mbFileSize + "MB -- " + (double) Math.round(percent * 1000) / 1000 + "% -- Speed: " + mbPerSecond + "MB/S -- ETA: " + result.toString().trim());
                        lastTransferredSize = transferredSize;
                    }
                    // writing buffer to output stream and sending it with socket
                    dout.write(buffer, 0, count);
                    // keeping track of transferred size
                    transferredSize += buffer.length;
                    // calculating transferred size in MB
                    double temp1 = (double) transferredSize / (1024 * 1024);
                    mbTransferredSize = (double) Math.round(temp1 * 1000) / 1000;
                }
                // flushing output stream to make sure all remaining data is sent to the server to prevent data getting stuck in buffers
                dout.flush();
                LCCP.logger.debug(id + "Successfully send file contents!");

                LCCP.logger.debug(id + "Closing socket and streams...");
                // closing socket and all streams to free up system resources
                bufferedInputStream.close();
                socket.close();
                LCCP.logger.debug(id + "Successfully closed socket and streams!");
                LCCP.logger.debug(id + "Sending complete!");

            } catch (IOException e) {
                LCCP.logger.error(id + "Error occurred! Transmission terminated!");
                //LCCP.networkLogger.addPacketToQueue(uuid, Logger.log_level.ERROR);
                return false;
            }
            LCCP.logger.debug(id + "Successfully send file to server!");
            LCCP.logger.debug(id + "---------------------------------------------------------------");
            //LCCP.networkLogger.addPacketToQueue(uuid, Logger.log_level.DEBUG);
            return true;
        }

        // function to send YAML packets to the server
        public static boolean sendYAML(String serverIP4, int serverPort, YAMLConfiguration yaml) {
            // checking if network event id is given
            boolean noID = false;
            // figuring out network event id, if none is given create a new one
            String networkID = "";
            String id;
            String description =
                    "[Client]" +
                            "[Data Output]" +
                            "[YAML]" +
                            "[Destination '" + serverIP4 +"']" +
                            "[Port '" + serverPort + "']";
            try {
                // try to get network event id from the yaml file
                networkID = yaml.getString(Paths.NETWORK.YAML.INTERNAL_NETWORK_EVENT_ID);
                // if no id is given trigger creation of a new one
                if (networkID == null || networkID.isBlank()) noID = true;
            } catch (NoSuchElementException e) {
                // if the id check fails due to an error trigger creating of a new id
                noID = true;
            }

            // if no id is given get a new one from networkLogger
            if (noID) {
                id = "[" +
                        LCCP.networkLogger.getRandomUUID(description) +
                        "] ";
            // if an id is give, pass it on to the network logger
            } else {
                id = "[" + networkID + "]";
                LCCP.networkLogger.addEvent(UUID.fromString(networkID), description);
            }

            // general information messages
            LCCP.logger.debug(id + "-------------------- Network Communication --------------------");
            LCCP.logger.debug(id + "Type: client - data out");
            LCCP.logger.debug(id + "Server: " + serverIP4);
            LCCP.logger.debug(id + "Port: " + serverPort);


            try {
                // open a new client socket for server:port
                Socket socket = new Socket(serverIP4, serverPort);
                LCCP.logger.debug(id + "Successfully established connection!");

                // opening data streams
                LCCP.logger.debug(id + "Creating data streams...");
                DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
                ByteArrayOutputStream outputS = new ByteArrayOutputStream();

                // loading the yaml message into a byteArrayOutputStream using fileHandler built-in function
                LCCP.logger.debug(id + "Loading data to transmit...");
                new FileHandler(yaml).save(outputS);

                // inspecting loaded data, detecting data size and printing it to console
                LCCP.logger.debug(id + "Inspecting data:");
                byte[] bytes = outputS.toByteArray();
                int byteCount = bytes.length;
                boolean kb = byteCount > 8192;
                LCCP.logger.debug(id + "Size: " + (kb ? (byteCount / 1024) + "KB" : byteCount + " Bytes"));

                // sending data size to server
                LCCP.logger.debug(id + "Transmitting size...");
                dout.writeInt(byteCount);
                LCCP.logger.debug(id + "Successfully transmitted size to server!");

                // sending yaml data to server
                LCCP.logger.debug(id + "Transmitting data...");
                dout.write(bytes);
                LCCP.logger.debug(id + "Successfully transmitted data to server!");

                // closing data streams and socket to free up system resources
                LCCP.logger.debug(id + "Closing socket and data streams...");
                dout.close();
                socket.close();

                LCCP.logger.debug(id + "---------------------------------------------------------------");

            } catch (IOException | ConfigurationException e) {
                // if an error occurs print an error message
                LCCP.logger.error(id + "Error occurred! Transmission terminated!");
                LCCP.logger.error(e);
                return false;
            }

            return true;

        }
    }
}
