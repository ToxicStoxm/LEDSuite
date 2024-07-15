package com.toxicstoxm.lccp.communication.network;

import com.toxicstoxm.lccp.Constants;
import com.toxicstoxm.lccp.LCCP;
import com.toxicstoxm.lccp.event_handling.EventHandler;
import com.toxicstoxm.lccp.event_handling.Events;
import com.toxicstoxm.lccp.event_handling.listener.EventListener;
import com.toxicstoxm.lccp.task_scheduler.LCCPRunnable;
import com.toxicstoxm.lccp.yaml_factory.YAMLMessage;
import com.toxicstoxm.lccp.yaml_factory.YAMLSerializer;
import com.toxicstoxm.lccp.yaml_factory.wrappers.message_wrappers.ServerError;
import com.toxicstoxm.lccp.yaml_factory.wrappers.message_wrappers.StatusUpdate;
import com.toxicstoxm.lccp.task_scheduler.LCCPProcessor;
import com.toxicstoxm.lccp.task_scheduler.LCCPTask;
import com.toxicstoxm.lccp.time.TimeManager;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;

import java.io.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Utility class for server communication.
 * <p>
 * This class provides methods for interacting with a server, including sending and receiving messages and handling potential errors.
 * @implNote {@code YAML} is used as the primary message format since it is easy to parse and scale.
 * @since 1.0.0
 */
public class Networking {

    private static final TreeMap<Long, LCCPRunnable> networkQueue = new TreeMap<>();
    private static final HashMap<UUID, Communication.NetworkHandler.ReplyListener> replyListeners = new HashMap<>();

    /**
     * Includes functions used to validate {@code IPv4s} and {@code Ports} using format checks and pinging.
     * @since 1.0.0
     */
    public static class Validation {
        private static final int timeout = 3;

        /**
         * Tries to validate the provided IP address using the ping command.
         * @param ip the IP address to validate
         * @return {@code true} If ping received a response before timeout ({@value timeout}s)
         * @implNote The ping command is used for validation because it uses a real ping utility. This method aims to
         * avoid false positives that might occur when using echo port 7 like {@link java.net.InetAddress#isReachable(int)}, which can misinterpret
         * reset (RST) responses from routers/firewalls as successful connections. Such misinterpretations can lead to
         * incorrect validation results.
         * @since 1.0.0
         */
        private static boolean ping(String ip) {
            LCCP.logger.verbose("Received ping request for '" + ip + "'" + " timeout: '" + 3 + "'");
            try {
                // formatting ping command with specified timeout and IPv4 / host name
                LCCP.logger.verbose("Formatting ping command...");
                List<String> command = new ArrayList<>();
                command.add("ping");
                command.add("-W" + timeout);
                command.add("-c1");
                command.add(ip);
                LCCP.logger.verbose("Formatting complete! Command: " + command);

                // creating a new process with the specified arguments above using process builder
                LCCP.logger.verbose("Creating new process...");
                ProcessBuilder processBuilder = new ProcessBuilder(command);

                // starting the process
                Process process = processBuilder.start();
                LCCP.logger.verbose("Created and started new process!");
                LCCP.logger.verbose("Command output: ");
                LCCP.logger.verbose("------------------- PING -------------------");

                // reading console feedback using a buffered reader
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                // storing single feedback lines in a String list, and printing it to the console for debug
                String line;
                List<String> output = new ArrayList<>();
                while ((line = reader.readLine()) != null) {
                    LCCP.logger.verbose(line);
                    output.add(line);
                }
                LCCP.logger.verbose("--------------------------------------------");

                // waiting for the ping process to complete or time out
                process.waitFor();

                // iterating through the command output and checking if it contains a specific String that indicates that the ping was successful
                // if the specific string ('64 bytes' in this case) is found the function returns true
                for (String s : output) {
                    if (s.toLowerCase().contains("64 bytes")) {
                        LCCP.logger.verbose("Ping was successful!");
                        LCCP.logger.verbose("Command complete.");
                        return true;
                    }
                }

            } catch (IOException | InterruptedException e) {
                // if an exception is thrown due to an UnknownHostException / InterruptedException or any kind of IoException,
                // it returns false
                LCCP.logger.verbose("Ping failed!");
                LCCP.logger.verbose("Command complete.");
                return false;
            }
            // if the string isn't found and no exception is thrown the ping command executed but timed out so the function also returns false
            LCCP.logger.verbose("Ping failed!");
            LCCP.logger.verbose("Command complete.");
            return false;
        }

        /**
         * Validates the provided host address using {@link Validation#isValidIP(String)} and {@link Validation#ping(String)}, and tries to get a
         * corresponding hostname to it.
         * @param ip The host address to validate and optionally get the IPv4 from
         * @param ipify Set to {@code true} if you want the function to return the IPv4 instead of the hostname
         * @return The hostname of the provided host address or the IPv4 address, if {@code ipify} is set to true
         * @throws UnknownHostException if the host address is invalid or not reachable
         * @since 1.0.0
         */
        public static String getValidIP(String ip, boolean ipify) throws UnknownHostException {
            LCCP.logger.verbose("Fulfilling ping request for: '" + ip + "'");
            String ipv4;
            try {
                // creating new InetAddress to hold the IPv4 / host name
                InetAddress host;
                // check if the specified string is a valid IPv4 address (matches format)
                if (isValidIP(ip)) {
                    // try to ping ip with custom ping function
                    // if the ping times out or fails throw new UnknownHostException
                    // this is done so the error can be handled differently for different use cases
                    if (!ping(ip)) throw new UnknownHostException("Connection timed out!");

                    // if the ping is successful the IPv4 is pares by InetAddress
                    host = InetAddress.ofLiteral(ip);
                } else {
                    // tries to get IPv4 from a host name using InetAddress integrated getByName() function
                    host = InetAddress.getByName(ip);
                }

                // gets the IPv4 address from the InetAddress object
                ipv4 = host.getHostAddress();
            } catch (UnknownHostException e) {
                // if any exception occur the program will display some standard messages in the console
                LCCP.logger.verbose("Ping failed!");
                LCCP.logger.verbose(e.getMessage());
                LCCP.logger.warn("Invalid host name or IPv4: '" + ip + "'");
                // the exception is thrown again to enable for custom error handling later
                throw e;
            }
            // ping results are displayed in the console
            LCCP.logger.verbose("Ping success!");
            LCCP.logger.verbose("Host name: '" + ip + "'");
            LCCP.logger.verbose("Detected IPv4: '" + ipv4 + "'");
            // return the ip or the host name based on 'ipify' param
            return ipify ? ipv4 : ip;
        }

        /**
         * Checks if a given IPv4's format is valid, using {@link Constants.Patterns#IPV4}.
         * @param ip The IPv4 address to check
         * @return {@code true} If the given IPv4 address matches the pattern
         * @since 1.0.0
         */
        public static boolean isValidIP(final String ip) {
            // validate IPv4 format
            return ip.matches(Constants.Patterns.IPV4);
        }

        /**
         * Checks if a given port's format is valid, using {@link Constants.Patterns#PORT}.
         * @param port The port to check
         * @return {@code true} If the given port matches the pattern
         * @since 1.0.0
         */
        public static boolean isValidPORT(final String port) {
            LCCP.logger.verbose("Fulfilling port validation request for: '" + port + "'");
            // validate port format
            boolean result = port.matches(Constants.Patterns.PORT);
            // print result to console
            if (result) {
                LCCP.logger.verbose("Port validation successful!");
                LCCP.logger.verbose("Port has valid format (Range: 1 - 65535)");
            } else {
                LCCP.logger.verbose("Port validation has failed!");
                LCCP.logger.verbose("Invalid port format: '" + port + "'");
                LCCP.logger.verbose("Port needs to be a numerical value between 1 and 65535!");
            }

            return result;
        }
    }

    /**
     * Includes communication logic used to communicate with a server.
     * <p>
     * Key features:
     * <l>
     *    <li>Sending queue with simple priority system</l>
     *    <li>Flexible and dynamic listener management</li>
     *    <li>Server error handling</li>
     *    <li>Automatic reconnection handler</li>
     *    <li>Keepalive system</li>
     * </l>
     * @implNote {@link java.net.Socket} is used to communicate with the server
     * @since 1.0.0
     */
    public static class Communication {
        /**
         * This is a wrapper function for {@link Communication#sendFile(String, int, String, ProgressTracker)}
         * @param fileToSendPath path to a file that should be sent to the server
         * @param progressTracker a progress tracker, used to monitor uploading progress, this could be useful if you want to display a loading bar
         * @since 1.0.0
         */
        public static void sendFileDefaultHost(String fileToSendPath, ProgressTracker progressTracker) {
            sendFile(LCCP.server_settings.getIPv4(), LCCP.server_settings.getPort(), fileToSendPath, progressTracker);
        }

        /**
         * Sends a file upload request to the server, if successful the specified file loaded into memory and sent to the specified host (server:port) monitored by the specified progress tracker <p>
         * This is a wrapper function for {@link Communication#sendFile(String, int, ProgressTracker, File)}
         * @param serverIP4 the servers IPv4
         * @param serverPort the servers port
         * @param fileToSendPath path to a file that should be sent to the server
         * @param progressTracker a progress tracker, used to monitor uploading progress, this could be useful if you want to display a loading bar
         * @since 1.0.0
         */
        public static void sendFile(String serverIP4, int serverPort, String fileToSendPath, ProgressTracker progressTracker) {
            // loading file to memory
            File fileToSend = new File(fileToSendPath);
            try {
                // send a file upload request to the server
                sendYAMLDefaultHost(
                        YAMLMessage.builder()
                                .setPacketType(YAMLMessage.PACKET_TYPE.request)
                                .setRequestType(YAMLMessage.REQUEST_TYPE.file_upload)
                                .setRequestFile(fileToSend.getName())
                                .setObjectNewValue(String.valueOf(fileToSend.length()))
                                .build(),
                        success -> {
                            // if the request was successful send the file to the server using the sendFile() method
                            if (success) {
                                if (!sendFile(serverIP4, serverPort, progressTracker, fileToSend)) {
                                    LCCP.logger.error("Failed to send file '" + fileToSendPath + "' to server '" + serverIP4 + ":" + serverPort + "'!");
                                }
                            }
                        }
                );
            } catch (YAMLSerializer.TODOException | ConfigurationException | YAMLSerializer.InvalidReplyTypeException |
                     YAMLSerializer.InvalidPacketTypeException e) {
                LCCP.logger.error(e);
            }
        }

        // send file to server using sockets

        /**
         *
         * @param serverIPv4 the servers IPv4
         * @param serverPort the servers port
         * @param progressTracker a progress tracker, used to monitor uploading progress, this could be useful if you want to display a loading bar
         * @param fileToSend the file to send to the server
         * @return {@code true} if the upload was successful, otherwise {@code false}
         * @implNote the serverIPv4 and port params are only used for logging and aren't actually used as address, since that is handled by the {@link NetworkHandler}
         * @since 1.0.0
         */
        public static boolean sendFile(String serverIPv4, int serverPort, ProgressTracker progressTracker, File fileToSend) {
            boolean track = progressTracker != null;

            // getting new network event id from networkLogger
            String id = "[" +
                    LCCP.networkLogger.getRandomUUID(
                            "[Client]" +
                                    "[Data Output]" +
                                    "[FILE]" +
                                    "[Destination '" + serverIPv4 +"']" +
                                    "[Port '" + serverPort + "']"
                    ) +
                    "] ";

            //printing file metadata to console
            LCCP.logger.verbose(id + "-------------------- Network Communication --------------------");
            LCCP.logger.verbose(id + "Received request to send '" + fileToSend.getAbsolutePath() + "' to " + serverIPv4 + ":" + serverPort + "!");
            LCCP.logger.verbose(id + "Inspecting file...");
            LCCP.logger.verbose(id + "File name: " + fileToSend.getName());
            LCCP.logger.verbose(id + "File size: " + ((fileToSend.length() / 1024) / 1024) + "MB");
            LCCP.logger.verbose(id + "File type: " + fileToSend.getName().split("\\.")[1].toUpperCase());

            LCCP.logger.info(id + "Sending File: '" + fileToSend.getAbsolutePath() + "' to " + serverIPv4 + ":" + serverPort);

            try {
                // getting current socket from network handler
                Socket socket = NetworkHandler.getServer();
                LCCP.logger.verbose(id + "Successfully established connection!");

                // getting the sockets data streams
                LCCP.logger.verbose(id + "Opening output streams...");
                OutputStream out = socket.getOutputStream();
                LCCP.logger.verbose(id + "Successfully opened output streams!");

                // creating new file input stream to read the file contents
                LCCP.logger.verbose(id + "Opening new FileInputStream to read the main file content...");
                FileInputStream fileInputStream = new FileInputStream(fileToSend);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                LCCP.logger.verbose(id + "Successfully created new BufferedInputStream for the FileInputStream!");

                // defining new 8KB buffer to store data while reading / writing
                LCCP.logger.verbose(id + "Defining new 8129B buffer...");
                byte[] buffer = new byte[8192];

                // read byte count
                int count;

                // getting exact file size
                long fileSize = fileToSend.length();

                // values to monitor upload statistics
                long transferredSize = 0;
                long lastTransferredSize = 0;
                double avgBytesPerSecond = 0;
                double vals = 1;

                // calculating the file size in MB
                double temp = (double) fileSize / (1024 * 1024);
                double mbFileSize = (double) Math.round(temp * 1000) / 1000;
                double mbTransferredSize = (double) transferredSize / (1024 * 1024);

                // monitor updating clock speed
                long delay = 100;
                long lastDisplay = System.currentTimeMillis() - delay;

                // console printing clock speed
                long printDelay = 2000;
                long lastPrint = System.currentTimeMillis() - printDelay;

                // reading / writing file contents using the buffer
                // the app also calculates transfer speed in MB/S and ETA
                // additionally the app keeps track on how much data was already transferred
                LCCP.logger.verbose(id + "Sending main file contents...");
                while ((count = bufferedInputStream.read(buffer)) > 0) {
                    if (System.currentTimeMillis() - lastDisplay >= delay) {
                        // calculating speed, eta and transferred data
                        long bytesTransferredLastSecond = transferredSize - lastTransferredSize;
                        double bytesPerSecond = (double) bytesTransferredLastSecond / ((double) delay / 1000);
                        if (avgBytesPerSecond == 0) avgBytesPerSecond = bytesPerSecond;
                        avgBytesPerSecond = ((avgBytesPerSecond * (vals)) + (bytesPerSecond * (1 / vals))) / vals;
                        vals++;
                        double temp1 = bytesPerSecond / (1024 * 1024);
                        double mbPerSecond = (double) Math.round(temp1 * 1000) / 1000;
                        lastDisplay = System.currentTimeMillis();
                        double percent = ((double) transferredSize / fileSize) * 100;
                        double estimatedSecondsRemaining = (fileSize - transferredSize) / avgBytesPerSecond;
                        if (transferredSize == 0) estimatedSecondsRemaining = 0;

                        Date date = new Date((long) (estimatedSecondsRemaining * 1000));

                        // Create a SimpleDateFormat object for formatting
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                        sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Set timezone to UTC to avoid wrong values

                        // Format the date
                        String formattedTime = sdf.format(date);

                        // Check each part and remove if it's zero to make it look better
                        String[] parts = formattedTime.split(":");
                        StringBuilder result = new StringBuilder();
                        if (!parts[0].equals("00")) {
                            result.append(parts[0]).append("h ");
                        }
                        if (!parts[1].equals("00")) {
                            result.append(parts[1]).append("m ");
                        }
                        result.append(parts[2]).append("s");

                        long current = System.currentTimeMillis();
                        if (current - lastPrint > printDelay) {
                            // displaying transfer information message in the console containing speed, eta, file size and transferred data
                            LCCP.logger.verbose(id + "Transferring File: " + mbTransferredSize + "MB / " + mbFileSize + "MB -- " + (double) Math.round(percent * 1000) / 1000 + "% -- Speed: " + mbPerSecond + "MB/S -- ETA: " + result.toString().trim());
                            lastPrint = current;
                        }
                        // if the progress tracker object is not null, update its monitoring values to the ones calculated above
                        if (track && transferredSize > 0) {
                            progressTracker.setUpdated(true);
                            progressTracker.setTotalSizeInBytes(fileSize);
                            progressTracker.setTotalSizeInMegabytes(mbFileSize);
                            progressTracker.setTransferredSizeInBytes(transferredSize);
                            progressTracker.setTransferredSizeInMegabytes(mbTransferredSize);
                            progressTracker.setSpeedInBytes(avgBytesPerSecond);
                            progressTracker.setSpeedInMegabytes(avgBytesPerSecond / (1024 * 1024));
                            progressTracker.setProgressPercentage((double) Math.round(percent * 100) / 10000);
                            progressTracker.setEta(result.toString());
                        }
                        lastTransferredSize = transferredSize;
                    }

                    // writing buffer to output stream and sending it with socket
                    out.write(buffer, 0, count);
                    // keeping track of transferred size
                    transferredSize += buffer.length;
                    // calculating transferred size in MB
                    double temp1 = (double) transferredSize / (1024 * 1024);
                    mbTransferredSize = (double) Math.round(temp1 * 1000) / 1000;
                }

                // if the progress tracker object is not null and the upload has finished
                // update the progress percentage to 100% manually since the loop above will exit at 99%
                if (track && transferredSize > 0) {
                    progressTracker.setTotalSizeInBytes(fileSize);
                    progressTracker.setTotalSizeInMegabytes(mbFileSize);
                    progressTracker.setTransferredSizeInBytes(transferredSize);
                    progressTracker.setTransferredSizeInMegabytes(mbTransferredSize);
                    progressTracker.setSpeedInBytes(0);
                    progressTracker.setSpeedInMegabytes(0);
                    progressTracker.setProgressPercentage(1.0);
                    progressTracker.setEta("N/A");
                }

                // flushing output stream to make sure all remaining data is sent to the server to prevent data getting stuck in buffers
                out.flush();
                LCCP.logger.verbose(id + "Successfully send file contents!");
                LCCP.logger.verbose(id + "Sending complete!");
            } catch (IOException e) {
                if (track) progressTracker.setError(true); // inform the progress tracker of the occurred error
                LCCP.logger.error(id + "Error occurred! Transmission terminated!");
                LCCP.logger.error(e);
                return false;
            } catch (NetworkException e) {
                if (track) progressTracker.setError(true); // inform the progress tracker of the occurred error
                LCCP.logger.fatal(id + "Network error: " + e.getMessage());
            } finally {
                LCCP.logger.verbose(id + "---------------------------------------------------------------");
            }
            LCCP.logger.verbose(id + "Successfully send file to server!");
            LCCP.logger.verbose(id + "---------------------------------------------------------------");
            return true;
        }

        /**
         * Used to monitor upload statistics. Useful for displaying a progress bar.
         * @since 1.0.0
         */
        @Setter
        @Getter
        public static class ProgressTracker {
            private boolean started = false;
            private boolean error = false;
            private boolean updated = false;
            private double totalSizeInBytes = 0.0;
            private double totalSizeInMegabytes = 0.0;
            private double transferredSizeInBytes = 0.0;
            private double transferredSizeInMegabytes = 0.0;
            private double progressPercentage = 0.0;
            private double speedInBytes = 0.0;
            private double speedInMegabytes = 0.0;
            private String eta = "";
        }

        private static final long delay = 10; // delay in milliseconds between network packets

        /**
         * Manages the core networking logic, including the listener and sending queue with an appropriate send handler.
         * This class maintains an open connection to the server using a keepalive mechanism.
         * <p>
         * Any client or server-side errors are handled appropriately.
         * <p>
         * If the server address changes or the connection is closed, this class attempts to automatically reestablish the connection,
         * using the last known connection as a fallback if possible.
         * If all attempts fail, this class will shut down to conserve system resources and will periodically wake up to attempt reconnection.
         * @since 1.0.0
         */
        public static class NetworkHandler {

            // the main socket object, that is kept open if possible
            /**
             * Main socket object, that is kept open if possible to prevent unnecessary reconnections.
             * @since 1.0.0
             */
            protected static Socket server = null;

            /**
             * Checks if the server is open and connected.
             * @return {@code true} If the socket is open and connected to a server
             * @since 1.0.0
             */
            private static boolean isConnected() {
                return server != null && !server.isClosed() && server.isConnected();
            }

            // network - manager and listener objects
            /**
             * The network manager is responsible for sending packets.
             * <p>Main objectives:</p>
             * <l>
             *     <li>Periodically sends keepalive packets to the server, to keep the connection alive</li>
             *     <li>Periodically sends status requests to the server, to keep status information up to date</li>
             *     <li>Periodically checks the network sending queue for entries, if any are found send them to the server</li>
             * </l>
             */
            private static LCCPTask mgr = null;
            private static LCCPTask masterListener = null;

            /**
             * Used to monitor communication state between client and server.
             * @see SuccessCallback#getResult(boolean)
             * @since 1.0.0
             */
            public interface SuccessCallback {
                /**
                 * Called when the communication between server and client finished (message was transferred to the server or an error occurred).
                 * @param success {@code true} If the message was sent successfully, {@code false} if any errors occurred during transfer or no server is connected
                 * @throws NetworkException if any errors occur, to allow for custom error handling
                 * @since 1.0.0
                 */
                void getResult(boolean success) throws NetworkException;
            }

            /**
             * Used to get the current connected socket object. If the socket is not yet connected a new connection will be made.
             * @return The current connected socket object.
             * @throws NetworkException if the initialization of a new connection fails
             * @since 1.0.0
             */
            protected static Socket getServer() throws NetworkException {
                // if the server is not connected, initialize a new connection
                if (!isConnected()) {
                    init(success -> {
                        // if initialization fails throw an exception
                        if (!success) throw new NetworkException("Failed to establish connection to the server!");
                    });
                }
                return server;
            }

            /**
             * Initializes a new connection to the server. <p>
             * Main objectives:
             * <l>
             *     <li>Establish a new connection to the server</li>
             *     <li>Cancel any running tasks from last connection</li>
             *     <li>Create a new network manager, that is responsible for sending messages</li>
             *     <li>Request the initialization of a new network listener using {@link NetworkHandler#initListener()}</li>
             * </l>
             * @param callback used to communicate result back to the caller method
             * @throws NetworkException if the attempt fails
             * @since 1.0.0
             */
            public static void init(SuccessCallback callback) throws NetworkException {
                try {
                    // if socket is not initialized at all, create a new socket
                    // connect it to the new server
                    if (server == null || server.isClosed()) {
                        server = new Socket(LCCP.server_settings.getIPv4(), LCCP.server_settings.getPort());
                    } else {
                        server.connect(new InetSocketAddress(LCCP.server_settings.getIPv4(), LCCP.server_settings.getPort()));
                    }
                    LCCP.logger.verbose("Successfully connected to server!");
                } catch (Exception e) {
                    // if connection fails, inform the caller function using the callback
                    LCCP.logger.fatal("Failed to initialize connection to server! Error: " + e.getMessage());
                    LCCP.logger.error(e);
                    if (callback != null) callback.getResult(false);
                    return;
                }

                LCCP.logger.verbose("Fulfilling initialization request for Network Handler!");

                LCCP.logger.verbose("Network Handler: starting network handle...");
                LCCP.eventManager.registerEvents(new NetworkHandle());
                LCCP.logger.verbose("Network Handler: started network handle!");


                // if manager is already running cancel it
                if (mgr != null) mgr.cancel();
                long keepalive = 500;

                // initializing new time tracker to keep track of keepalive timings
                TimeManager.clearTimeTracker("keepalive");
                TimeManager.initTimeTracker("keepalive", keepalive);

                // starting a new network manager
                LCCP.logger.verbose("Network Handler: starting manager...");
                mgr = new LCCPRunnable() {
                    @Override
                    public void run() {
                        // check if keepalive needs to be sent
                        if (TimeManager.call("keepalive")) {
                            try {
                                // try sending keepalive message to server
                               if (!sendKeepalive(
                                       // build new keepalive packet using YAMLSerializer
                                        YAMLMessage.builder()
                                                .setPacketType(YAMLMessage.PACKET_TYPE.request)
                                                .setRequestType(YAMLMessage.REQUEST_TYPE.keepalive)
                                                .build(),
                                        false
                                )
                               ) throw new NetworkException("Failed to send Keepalive!");
                            } catch (YAMLSerializer.TODOException | ConfigurationException |
                                     YAMLSerializer.InvalidReplyTypeException |
                                     YAMLSerializer.InvalidPacketTypeException |
                                     NetworkException e) {
                                LCCP.logger.fatal("Failed to send keepalive!");
                                LCCP.logger.error(e);
                            }
                        }
                        // check if status needs to be sent / updated
                        if (TimeManager.call("status")) {
                            // if main window is open request status from server
                            if (LCCP.mainWindow != null) LCCP.mainWindow.getStatus(null);
                        }
                        // if the send-queue isn't empty and the server is connected
                        if (!networkQueue.isEmpty() && isConnected()) {
                            Map.Entry<Long, LCCPRunnable> entry = networkQueue.firstEntry();
                            LCCP.logger.verbose("Handling request: " + entry.getKey());
                            entry.getValue().runTaskAsynchronously();
                            networkQueue.remove(entry.getKey());
                        }
                    }
                }.runTaskTimerAsynchronously(LCCP.settings.getNetworkingCommunicationClockSpeed(), delay);
                LCCP.logger.verbose("Network Handler: started manager!");

                // initialize the listener
                initListener();

                LCCP.logger.verbose("Network Handler started!");
                // informs the functions caller of the result
                callback.getResult(true);
            }

            /**
             * Tries to initialize a new network listener.
             * <p>Main objectives:</p>
             * <l>
             *     <li>Cancel any running tasks from previous connection</li>
             *     <li>Create and initialize a new network listener</li>
             * </l>
             * @since 1.0.0
             */
            private static void initListener() {
                LCCP.logger.verbose("Network Handler: starting master listener...");
                // cancelling any active listeners from previous connection
                if (masterListener != null) masterListener.cancel();
                // creating and initializing new listener
                masterListener = new LCCPRunnable() {
                    @Override
                    public void run() {
                        try {
                            // if no server is connected do nothing
                            if (!isConnected()) return;
                            // get the current connected sockets input stream
                            InputStream is = server.getInputStream();
                            // check if any data is available, this function blocks for large amounts of data
                            if (is.available() > 0) {
                                // use the default receive function to receive the data sent by the server
                                YAMLConfiguration yamlCfg = defaultReceive(is);

                                if (yamlCfg != null) {

                                    YAMLMessage yaml;

                                    // try to extract the network id from the received message
                                    // this is used for logging and for assigning the message to the correct listener
                                    try {
                                        UUID networkID0 = UUID.fromString(yamlCfg.getString(Constants.Network.YAML.INTERNAL_NETWORK_ID));
                                        yaml = YAMLSerializer.deserializeYAML(yamlCfg, networkID0);
                                    } catch (IllegalArgumentException e) {
                                        LCCP.logger.error(e);
                                        LCCP.logger.warn(System.currentTimeMillis() + " Received reply with missing or invalid network id! Can't associate it with corresponding listener!");
                                        yaml = YAMLSerializer.deserializeYAML(yamlCfg);

                                    }
                                    UUID networkID = yaml.getNetworkID();

                                    // check if any custom listeners with the specific network id exist
                                    // if a listener is found, it will be executed with the received message
                                    // finally the listener is removed for the reply listeners collection
                                    // if no listener is found for the specific network id, the message is processed using a default handler
                                    Map<UUID, ReplyListener> replyListeners = Collections.synchronizedMap(Networking.replyListeners);
                                    if (replyListeners.containsKey(networkID)) {
                                        ReplyListener listener = replyListeners.remove(networkID);
                                        if (listener != null) {
                                            listener.processFor(yaml);
                                        }
                                        replyListeners.remove(networkID);
                                    } else {
                                        defaultHandle(yaml);
                                    }

                                }
                            }
                        } catch (Exception e) {
                            LCCP.logger.fatal("Network Handler: master listener: Error: " + e.getMessage());
                            LCCP.logger.error(e);
                        }

                    }
                }.runTaskTimerAsynchronously(0, 1);
                LCCP.logger.verbose("Network Handler: started master listener!");
            }

            public static void cancel() {
                LCCP.logger.verbose("Network Handler: Fulfilling cancel request!");
                if (mgr != null) mgr.cancel();
                if (masterListener != null) masterListener.cancel();
                clearQueues();
                LCCP.eventManager.fireEvent(new Events.Status(StatusUpdate.notConnected()));
            }

            private static void clearQueues() {
                LCCP.logger.verbose("Network Handler: Fulfilling clear queues request!");
                networkQueue.clear();
                Map<UUID, ReplyListener> replyListenerQueue = Collections.synchronizedMap(Networking.replyListeners);
                replyListenerQueue.clear();

            }

            public static void reboot() throws NetworkException {
                LCCP.logger.verbose("Network Handler: Fulfilling reboot request!");
                try {
                    LCCP.logger.verbose("Network Handler: Closing socket");
                    server.close();
                } catch (Exception e) {
                    LCCP.logger.verbose("Network Handler: Closing failed, overwriting connection");
                }

                LCCP.logger.verbose("Network Handler: Stopping mgr and listener tasks");
                cancel();
                LCCP.logger.verbose("Network Handler: Clearing queues");
                clearQueues();

                try {
                    LCCP.logger.verbose("Network Handler: initializing...");
                    init(success -> {
                        LCCP.logger.verbose("Network Handler: success: " + success);
                        if (!success) throw new NetworkException("connection failed");
                    });
                } catch (NetworkException e) {
                    LCCP.logger.fatal("Network Handler: reboot failed!");
                    LCCP.logger.error(e);
                    throw new NetworkException("connection failed!");
                }
            }

            public static void hostChanged() throws NetworkException {
                reboot();
            }

            public static boolean connectedAndRunning() {
                return server != null && server.isConnected() && !server.isClosed();
            }

            public static class NetworkHandle implements EventListener {

                @EventHandler
                public void onShutdown(Events.Shutdown e) {
                    LCCP.logger.verbose("Network Handler: network handle detected shutdown");
                    LCCP.logger.verbose("Network Handler: clearing queues and cancelling main tasks");
                    cancel();
                    try {
                        server.close();
                    } catch (IOException ex) {
                        LCCP.logger.verbose("Failed to close socket!");
                    }
                    LCCP.logger.verbose("Network Handler: shutdown complete");
                }

                @EventHandler
                public void onHostChanged(Events.HostChanged e) {
                    LCCP.logger.verbose("Network Handler: network handle detected host change");
                    try {
                        hostChanged();
                    } catch (NetworkException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                @EventHandler
                public void onSettingChanged(Events.SettingChanged e) {
                    String key = e.key();
                    Object value = e.value();
                    if (value == null || key == null || key.isBlank() || key.isEmpty()) return;
                    LCCP.logger.verbose("Network Handler: network handle detected settings change (1)");
                    try {
                        sendYAMLDefaultHost(
                               YAMLMessage.builder()
                                        .setPacketType(YAMLMessage.PACKET_TYPE.request)
                                        .setRequestType(YAMLMessage.REQUEST_TYPE.settings_change)
                                        .addAdditionalEntry(key, value)
                                       .build()
                        );
                    } catch (YAMLSerializer.TODOException | ConfigurationException |
                             YAMLSerializer.InvalidReplyTypeException | YAMLSerializer.InvalidPacketTypeException ex) {
                        LCCP.logger.error("Failed to send (1) settings change request!");
                    }
                    LCCP.logger.verbose("Successfully send (1) settings change request to server!");
                }
                @EventHandler
                public void onSettingsChanged(Events.SettingsChanged e) {
                    HashMap<String, Object> changedSettings = e.changedSettings();
                    if (changedSettings.isEmpty()) return;
                    LCCP.logger.verbose("Network Handler: network handle detected settings changes (" + changedSettings.size() +")");
                    try {
                        sendYAMLDefaultHost(
                                YAMLMessage.builder()
                                        .setPacketType(YAMLMessage.PACKET_TYPE.request)
                                        .setRequestType(YAMLMessage.REQUEST_TYPE.settings_change)
                                        .setAdditionalEntries(changedSettings)
                                        .build()
                        );
                    } catch (YAMLSerializer.TODOException | ConfigurationException |
                             YAMLSerializer.InvalidReplyTypeException | YAMLSerializer.InvalidPacketTypeException ex) {
                        LCCP.logger.error("Failed to send (" + changedSettings.size() +") settings change request!");
                    }
                    LCCP.logger.verbose("Successfully send (" + changedSettings.size() +") settings change request to server!");
                }
            }

            private record ReplyListener(LCCPProcessor processor) {

                private void processFor(YAMLMessage yaml) {
                    processor.runTask(yaml);
                    LCCP.logger.verbose("Successfully processed received Reply Message with ID[" + yaml.getNetworkID() + "] using predefined LCCPProcessor with ID[" + processor.getTaskId() + "]!");

                }
            }

            public static void listenForReply(LCCPProcessor processor, UUID networkID) {
                Map<UUID, ReplyListener> replyListenerQueue = Collections.synchronizedMap(Networking.replyListeners);
                replyListenerQueue.put(networkID,
                        new ReplyListener(processor)
                );
            }

            protected static void listenForReply(UUID networkID) {
                Map<UUID, ReplyListener> replyListenerQueue = Collections.synchronizedMap(Networking.replyListeners);
                replyListenerQueue.put(networkID,
                        new ReplyListener(
                                new LCCPProcessor() {
                                    @Override
                                    public void run(YAMLMessage yaml) {
                                        if (yaml != null) {

                                            LCCP.eventManager.fireEvent(new Events.DataIn(yaml));

                                        }
                                    }
                                }
                        )
                );
            }
        }

        public static YAMLConfiguration defaultReceive(InputStream is) {
            YAMLConfiguration yaml;
            try {
                // Wrap the InputStream with a BufferedReader for efficient reading
                BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

                // Read the first line to get the total number of bytes expected
                int totalBytes = Integer.parseInt(br.readLine());
                //LCCP.logger.debug("Total bytes: " + totalBytes);

                // Prepare a buffer to read the expected number of bytes
                char[] buffer = new char[totalBytes];

                // Read the actual data into the buffer if not cancelled
                if (br.read(buffer) < 1) throw new NullPointerException();

                // Convert the buffer into a ByteArrayInputStream and load it into the YAMLConfiguration
                yaml = new YAMLConfiguration();
                new FileHandler(yaml).load(new ByteArrayInputStream(CharBuffer.wrap(buffer).toString().getBytes()));
            } catch (Exception e) {
                LCCP.logger.error(e);
                return null;
            }
            return yaml;
        }

        public static void defaultHandle(YAMLMessage yaml) {
            if (yaml.getPacketType().equals(YAMLMessage.PACKET_TYPE.error)) {
                LCCP.eventManager.fireEvent(new Events.Error(ServerError.fromYAMLMessage(yaml)));
            } else {
                LCCP.eventManager.fireEvent(new Events.DataIn(yaml));
            }
        }

        public interface FinishCallback {
            void onFinish(boolean success);
        }
        public static boolean sendKeepalive(YAMLConfiguration yaml, boolean displayLog) {
            return sendYAMLMessage(LCCP.server_settings.getIPv4(), LCCP.server_settings.getPort(), yaml, null, null, displayLog);
        }

        public static void sendYAMLDefaultHost(YAMLConfiguration yaml) {
            if (!sendYAML(LCCP.server_settings.getIPv4(), LCCP.server_settings.getPort(), yaml, null)) {
                LCCP.logger.error("Failed to send YAML message to server! Callback = false | ReplyHandler = false");
            }
        }
        public static void sendYAMLDefaultHost(YAMLConfiguration yaml, FinishCallback callback) {
            if (!sendYAML(LCCP.server_settings.getIPv4(), LCCP.server_settings.getPort(), yaml, callback)) {
                LCCP.logger.error("Failed to send YAML message with callback to server! Callback = true | ReplyHandler = false");
            }
        }

        public static void sendYAMLDefaultHost(YAMLConfiguration yaml, FinishCallback callback, LCCPProcessor replyHandler) {
            if (!sendYAML(LCCP.server_settings.getIPv4(), LCCP.server_settings.getPort(), yaml, callback, replyHandler)) {
                LCCP.logger.error("Failed to send YAML message with to server! Callback = true | ReplyHandler = true");
            }
        }

        public static boolean sendYAML(String host, int port, YAMLConfiguration yaml, FinishCallback callback) {
            return sendYAML(host, port, yaml, callback, null);
        }

        public static boolean sendYAML(String host, int port, YAMLConfiguration yaml, FinishCallback callback, LCCPProcessor replyHandler) {
            return sendYAML(host, port, yaml, callback, replyHandler, false);
        }

        public static boolean sendYAML(String host, int port, YAMLConfiguration yaml, FinishCallback callback, LCCPProcessor replyHandler, boolean priority) {
            if (!NetworkHandler.isConnected()) {
                try {
                    NetworkHandler.init(success -> {
                        if (!success) {
                            if (callback != null) callback.onFinish(false);
                            throw new NetworkException("Reconnection attempt to previous server failed!");
                        } else LCCP.logger.verbose("Successfully reconnected to previous server!");
                    });
                } catch (NetworkException e) {
                    LCCP.logger.fatal(e.getMessage());
                    if (callback != null) callback.onFinish(false);
                    return false;
                }
            }
            LCCP.logger.verbose("Appending send request to the network queue!");
            LCCPRunnable sendRequest = new LCCPRunnable() {
                @Override
                public void run() {
                    LCCP.logger.verbose("Sending packet: " + yaml.getProperty(Constants.Network.YAML.PACKET_TYPE));
                    sendYAMLMessage(host, port, yaml, callback, replyHandler);
                }
            };
            long current = System.currentTimeMillis();
            networkQueue.put(priority ? current - 1000 : current, sendRequest);
            return true;
        }

        private static void sendYAMLMessage(String serverIP4, int serverPort, YAMLConfiguration yaml, FinishCallback callback, LCCPProcessor replyHandle) {
            if (!sendYAMLMessage(serverIP4, serverPort, yaml, callback, replyHandle, true)) {
                LCCP.logger.error("Failed to send YAML message to server!");
            }
        }

        // function to send YAML packets to the server
        private static boolean sendYAMLMessage(String serverIP4, int serverPort, YAMLConfiguration yaml, FinishCallback callback, LCCPProcessor replyHandle, boolean displayLog) {
            boolean callb = callback != null;
            boolean err = false;

            // checking if network event id is given
            boolean noID = false;
            // figuring out network event id, if none is given create a new one
            String networkID = "";
            String id = "";
            if (displayLog) {
                String description =
                        "[Client]" +
                                "[Data Output]" +
                                "[YAML]" +
                                "[Destination '" + serverIP4 + "']" +
                                "[Port '" + serverPort + "']";
                try {
                    // try to get network event id from the yaml file
                    networkID = yaml.getString(Constants.Network.YAML.INTERNAL_NETWORK_ID);
                    // if no id is given trigger creation of a new one
                    if (networkID == null || networkID.isBlank()) noID = true;
                } catch (NoSuchElementException e) {
                    // if the id check fails due to an error trigger creating of a new id
                    noID = true;
                }

                // if no id is given get a new one from networkLogger
                if (noID) {
                    UUID uuid = LCCP.networkLogger.getRandomUUID(description);
                    id = "[" +
                            uuid +
                            "] ";
                    yaml.setProperty(Constants.Network.YAML.INTERNAL_NETWORK_ID, String.valueOf(uuid));
                    // if an id is give, pass it on to the network logger
                } else {
                    id = "[" + networkID + "] ";
                    LCCP.networkLogger.addEvent(UUID.fromString(networkID), description);
                }

                // general information messages
                LCCP.logger.verbose(id + "-------------------- Network Communication --------------------");
                LCCP.logger.verbose(id + "Type: client - data out");
                LCCP.logger.verbose(id + "Server: " + serverIP4);
                LCCP.logger.verbose(id + "Port: " + serverPort);


                LCCP.eventManager.fireEvent(new Events.DataOut(yaml));
            } else yaml.setProperty(Constants.Network.YAML.INTERNAL_NETWORK_ID, String.valueOf(UUID.randomUUID()));

            try {
                Socket socket = NetworkHandler.getServer();
                UUID networkID0 = null;
                if (displayLog) {
                    networkID0 = UUID.fromString(
                            id
                                    .replaceAll("\\[", "")
                                    .replaceAll("]", "")
                                    .strip()

                    );
                }

                if (replyHandle != null) {
                    NetworkHandler.listenForReply(
                            replyHandle,
                            networkID0
                    );
                } else {
                    NetworkHandler.listenForReply(
                            networkID0
                    );
                }

                if (displayLog) {
                    LCCP.logger.verbose(id + "Successfully established connection!");

                    LCCP.logger.verbose(id + "Creating data streams...");
                }
                // opening data streams
                OutputStream out = socket.getOutputStream();
                ByteArrayOutputStream outputS = new ByteArrayOutputStream();

                // loading the yaml message into a byteArrayOutputStream using fileHandler built-in function
                if (displayLog) LCCP.logger.verbose(id + "Loading data to transmit...");
                new FileHandler(yaml).save(outputS);

                // inspecting loaded data, detecting data size and printing it to console
                if (displayLog) LCCP.logger.verbose(id + "Inspecting data:");
                byte[] bytes = outputS.toByteArray();
                int byteCount = bytes.length;
                boolean kb = byteCount > 8192;
                if (displayLog) LCCP.logger.verbose(id + "Size: " + (kb ? (byteCount / 1024) + "KB" : byteCount + " Bytes"));

                // sending data size to server
                //LCCP.logger.debug(id + "Transmitting size...");
                //out.write((byteCount + "\n").getBytes());
                //out.flush();
                //LCCP.logger.debug(id + "Successfully transmitted size to server!");

                // sending yaml data to server
                if (displayLog) LCCP.logger.verbose(id + "Transmitting data...");
                out.write(bytes);
                if (displayLog) LCCP.logger.verbose(id + "Successfully transmitted data to server!");

                if (displayLog) {
                    //LCCP.logger.debug(id + "Closing socket and data streams...");
                    LCCP.logger.verbose(id + "---------------------------------------------------------------");
                }

            } catch (IOException | ConfigurationException e) {
                // try restarting network communication and retry sending
                try {
                    NetworkHandler.reboot();
                    sendYAMLMessage(serverIP4, serverPort, yaml, callback, replyHandle);
                    LCCP.logger.error(e);
                } catch (NetworkException ex) {
                    // if an error occurs print an error message
                    if (displayLog) {
                        LCCP.logger.error(id + "Error occurred! Transmission terminated!");
                        LCCP.logger.error(e);
                    }
                    err = true;
                }
            } catch (NetworkException e) {
                if (displayLog) LCCP.logger.fatal(id + "Network error: " + e.getMessage());
            }
            finally {
                if (displayLog) LCCP.logger.verbose(id + "---------------------------------------------------------------");
            }

            err = !err;
            if (callb) callback.onFinish(err);
            return err;

        }
    }

    public static class NetworkException extends Exception {
        private NetworkException(String message) {
            super(message);
        }
    }
    public static class ServerCommunicationException extends NetworkException {
        public ServerCommunicationException(String message) {
            super(message);
        }
    }
}
