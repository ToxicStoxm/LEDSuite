package com.toxicstoxm.LEDSuite.communication.network;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.LEDSuite;
import com.toxicstoxm.LEDSuite.event_handling.EventHandler;
import com.toxicstoxm.LEDSuite.event_handling.Events;
import com.toxicstoxm.LEDSuite.event_handling.listener.EventListener;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteProcessor;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteTask;
import com.toxicstoxm.LEDSuite.time.TimeManager;
import com.toxicstoxm.LEDSuite.yaml_factory.YAMLMessage;
import com.toxicstoxm.LEDSuite.yaml_factory.YAMLSerializer;
import com.toxicstoxm.LEDSuite.yaml_factory.wrappers.message_wrappers.ServerError;
import com.toxicstoxm.LEDSuite.yaml_factory.wrappers.message_wrappers.StatusUpdate;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;

import java.io.*;
import java.net.*;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Utility class for server communication.
 * <p>
 * This class provides methods for interacting with a server, including sending and receiving messages and handling potential errors.
 * @implNote {@code YAML} is used as the primary message format since it is straightforward to parse and scale.
 * @since 1.0.0
 */
public class Networking {

    private static final TreeMap<Long, LEDSuiteRunnable> networkQueue = new TreeMap<>();
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
            LEDSuite.logger.verbose("Received ping request for '" + ip + "'" + " timeout: '" + 3 + "'");
            try {
                // formatting ping command with specified timeout and IPv4 / host name
                LEDSuite.logger.verbose("Formatting ping command...");
                List<String> command = new ArrayList<>();
                command.add("ping");
                command.add("-W" + timeout);
                command.add("-c1");
                command.add(ip);
                LEDSuite.logger.verbose("Formatting complete! Command: " + command);

                // creating a new process with the specified arguments above using process builder
                LEDSuite.logger.verbose("Creating new process...");
                ProcessBuilder processBuilder = new ProcessBuilder(command);

                // starting the process
                Process process = processBuilder.start();
                LEDSuite.logger.verbose("Created and started new process!");
                LEDSuite.logger.verbose("Command output: ");
                LEDSuite.logger.verbose("------------------- PING -------------------");

                // reading console feedback using a buffered reader
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                // storing single feedback lines in a String list, and printing it to the console for debug
                String line;
                List<String> output = new ArrayList<>();
                while ((line = reader.readLine()) != null) {
                    LEDSuite.logger.verbose(line);
                    output.add(line);
                }
                LEDSuite.logger.verbose("--------------------------------------------");

                // waiting for the ping process to complete or time out
                process.waitFor();

                // iterating through the command output and checking if it contains a specific String that indicates that the ping was successful
                // if the specific string ('64 bytes' in this case) is found, the function returns true
                for (String s : output) {
                    if (s.toLowerCase().contains("64 bytes")) {
                        LEDSuite.logger.verbose("Ping was successful!");
                        LEDSuite.logger.verbose("Command complete.");
                        return true;
                    }
                }

            } catch (IOException | InterruptedException e) {
                // if an exception is thrown due to an UnknownHostException / InterruptedException or any kind of IoException,
                // it returns false
                LEDSuite.logger.verbose("Ping failed!");
                LEDSuite.logger.verbose("Command complete.");
                return false;
            }
            // if the string isn't found and no exception is thrown the ping command executed but timed out so the function also returns false
            LEDSuite.logger.verbose("Ping failed!");
            LEDSuite.logger.verbose("Command complete.");
            return false;
        }

        public interface ValidIPCallback {
            void onResult(String result);
        }

        public static void getValidIP(String ip, boolean ipify, ValidIPCallback callback) {
            if (callback == null) return;
            new LEDSuiteRunnable() {
                @Override
                public void run() {
                    try {
                        callback.onResult(getValidIP(ip, ipify));
                    } catch (UnknownHostException e) {
                        callback.onResult(null);
                    }
                }
            }.runTaskAsynchronously();
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
            LEDSuite.logger.verbose("Fulfilling ping request for: '" + ip + "'");
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

                    // if the ping is successful, the IPv4 is pares by InetAddress
                    host = InetAddress.ofLiteral(ip);
                } else {
                    // tries to get IPv4 from a host name using InetAddress integrated getByName() function
                    host = InetAddress.getByName(ip);
                }

                // gets the IPv4 address from the InetAddress object
                ipv4 = host.getHostAddress();
            } catch (UnknownHostException e) {
                // if any exception occurs, the program will display some standard messages in the console
                LEDSuite.logger.verbose("Ping failed!");
                LEDSuite.logger.verbose(e.getMessage());
                LEDSuite.logger.warn("Invalid host name or IPv4: '" + ip + "'");
                // the exception is thrown again to enable for custom error handling later
                throw e;
            }
            // ping results are displayed in the console
            LEDSuite.logger.verbose("Ping success!");
            LEDSuite.logger.verbose("Host name: '" + ip + "'");
            LEDSuite.logger.verbose("Detected IPv4: '" + ipv4 + "'");
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
            LEDSuite.logger.verbose("Fulfilling port validation request for: '" + port + "'");
            // validate port format
            boolean result = port.matches(Constants.Patterns.PORT);
            // print result to console
            if (result) {
                LEDSuite.logger.verbose("Port validation successful!");
                LEDSuite.logger.verbose("Port has valid format (Range: 1 - 65535)");
            } else {
                LEDSuite.logger.verbose("Port validation has failed!");
                LEDSuite.logger.verbose("Invalid port format: '" + port + "'");
                LEDSuite.logger.verbose("Port needs to be a numerical value between 1 and 65535!");
            }

            return result;
        }
    }

    /**
     * Includes communication logic used to communicate with a server.
     * <p>
     * Key features:
     * <ul>
     *    <li>Sending queue with simple priority system</l>
     *    <li>Flexible and dynamic listener management</li>
     *    <li>Server error handling</li>
     *    <li>Automatic reconnection handler</li>
     *    <li>Keepalive system</li>
     * </ul>
     * @implNote {@link java.net.Socket} is used to communicate with the server
     * @since 1.0.0
     */
    public static class Communication {
        /**
         * This is a wrapper function for {@link #sendFile(String, int, String, ProgressTracker)}
         * @param fileToSendPath path to a file that should be sent to the server
         * @param progressTracker a progress tracker, used to monitor uploading progress, this could be useful if you want to display a loading bar
         * @since 1.0.0
         */
        public static void sendFileDefaultHost(String fileToSendPath, ProgressTracker progressTracker) {
            sendFile(LEDSuite.server_settings.getIPv4(), LEDSuite.server_settings.getPort(), fileToSendPath, progressTracker);
        }

        /**
         * Sends a file upload request to the server, if successful the specified file loaded into memory and sent to the specified host (server:port) monitored by the specified progress tracker <p>
         * This is a wrapper function for {@link #sendFile(String, int, ProgressTracker, File)}
         * @param serverIP4 the servers IPv4
         * @param serverPort the server port
         * @param fileToSendPath path to a file that should be sent to the server
         * @param progressTracker a progress tracker, used to monitor uploading progress, this could be useful if you want to display a loading bar
         * @since 1.0.0
         */
        public static void sendFile(String serverIP4, int serverPort, String fileToSendPath, ProgressTracker progressTracker) {
            // loading the specified file to memory
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
                            // if the request was successful, send the file to the server using the sendFile() method
                            if (success) {
                                if (!sendFile(serverIP4, serverPort, progressTracker, fileToSend)) {
                                    LEDSuite.logger.error("Failed to send file '" + fileToSendPath + "' to server '" + serverIP4 + ":" + serverPort + "'!");
                                }
                            }
                        }
                );
            } catch (YAMLSerializer.TODOException | ConfigurationException | YAMLSerializer.InvalidReplyTypeException |
                     YAMLSerializer.InvalidPacketTypeException e) {
                LEDSuite.logger.displayError(e);
            }
        }

        // send file to server using sockets

        /**
         *
         * @param serverIPv4 the servers IPv4
         * @param serverPort the server port
         * @param progressTracker a progress tracker, used to monitor uploading progress, this could be useful if you want to display a loading bar
         * @param fileToSend the file to send to the server
         * @return {@code true} if the upload was successful, otherwise {@code false}
         * @implNote the serverIPv4 and port params are only used for logging and aren't used as address, since that is handled by the {@link NetworkHandler}
         * @since 1.0.0
         */
        public static boolean sendFile(String serverIPv4, int serverPort, ProgressTracker progressTracker, File fileToSend) {
            boolean track = progressTracker != null;

            // getting new network event id from networkLogger
            String id = "[" +
                    LEDSuite.networkLogger.getRandomUUID(
                            "[Client]" +
                                    "[Data Output]" +
                                    "[FILE]" +
                                    "[Destination '" + serverIPv4 +"']" +
                                    "[Port '" + serverPort + "']"
                    ) +
                    "] ";

            //printing file metadata to console
            LEDSuite.logger.verbose(id + "-------------------- Network Communication --------------------");
            LEDSuite.logger.verbose(id + "Received request to send '" + fileToSend.getAbsolutePath() + "' to " + serverIPv4 + ":" + serverPort + "!");
            LEDSuite.logger.verbose(id + "Inspecting file...");
            LEDSuite.logger.verbose(id + "File name: " + fileToSend.getName());
            LEDSuite.logger.verbose(id + "File size: " + ((fileToSend.length() / 1024) / 1024) + "MB");
            LEDSuite.logger.verbose(id + "File type: " + fileToSend.getName().split("\\.")[1].toUpperCase());

            LEDSuite.logger.info(id + "Sending File: '" + fileToSend.getAbsolutePath() + "' to " + serverIPv4 + ":" + serverPort);

            try {

                // disabling periodic requests to prevent unwanted data injection
                TimeManager.lock("keepalive");
                TimeManager.lock("status");
                TimeManager.lock("mgr");

                // getting current socket from network handler
                Socket socket = NetworkHandler.getServer();
                LEDSuite.logger.verbose(id + "Successfully established connection!");

                // getting the sockets data streams
                LEDSuite.logger.verbose(id + "Opening output streams...");
                OutputStream out = socket.getOutputStream();
                LEDSuite.logger.verbose(id + "Successfully opened output streams!");

                // creating new file input stream to read the file contents
                LEDSuite.logger.verbose(id + "Opening new FileInputStream to read the main file content...");
                FileInputStream fileInputStream = new FileInputStream(fileToSend);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                LEDSuite.logger.verbose(id + "Successfully created new BufferedInputStream for the FileInputStream!");

                // defining new 8KB buffer to store data while reading / writing
                LEDSuite.logger.verbose(id + "Defining new 8129B buffer...");
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

                // reading / writing file contents using the buffer,
                // the app also calculates transfer speed in MB/S and ETA
                // additionally the app keeps track on how much data was already transferred
                LEDSuite.logger.verbose(id + "Sending main file contents...");
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
                            // displaying a transfer information message in the console containing speed, eta, file size and transferred data
                            LEDSuite.logger.verbose(id + "Transferring File: " + mbTransferredSize + "MB / " + mbFileSize + "MB -- " + (double) Math.round(percent * 1000) / 1000 + "% -- Speed: " + mbPerSecond + "MB/S -- ETA: " + result.toString().trim());
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

                // if the progress tracker object is not null and the upload has finished,
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

                // flushing the output stream to make sure all remaining data is sent to the server to prevent data getting stuck in buffers
                out.flush();
                LEDSuite.logger.verbose(id + "Successfully send file contents!");
                LEDSuite.logger.verbose(id + "Sending complete!");
            } catch (IOException e) {
                if (track) progressTracker.setError(true); // inform the progress tracker of the occurred error
                LEDSuite.logger.error(id + "Error occurred! Transmission terminated!");
                LEDSuite.logger.displayError(e);
                return false;
            } catch (NetworkException e) {
                if (track) progressTracker.setError(true); // inform the progress tracker of the occurred error
                LEDSuite.logger.fatal(id + "Network error: " + e.getMessage());
            } finally {
                // re enabling periodic requests
                TimeManager.release("keepalive");
                TimeManager.release("status");
                TimeManager.release("mgr");
                LEDSuite.logger.verbose(id + "---------------------------------------------------------------");
            }
            LEDSuite.logger.verbose(id + "Successfully send file to server!");
            LEDSuite.logger.verbose(id + "---------------------------------------------------------------");
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
         * Manages the core networking logic, including the listener and sending queue with an appropriate send-handler.
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

            /**
             * Main socket object, that is kept open if possible to prevent unnecessary reconnections.
             * @since 1.0.0
             */
            protected static Socket server = null;
            protected static boolean serverIsRebooting = false;

            /**
             * Checks if the server is open and connected.
             * @return {@code true} If the socket is open and connected to a server
             * @since 1.0.0
             */
            public static boolean isConnected() {
                return server != null && !server.isClosed() && server.isConnected();
            }

            /**
             * The network manager is responsible for sending packets.
             * <p>Main objectives:</p>
             * <ul>
             *     <li>Periodically sends keepalive packets to the server, to keep the connection alive</li>
             *     <li>Periodically sends status requests to the server, to keep status information up to date</li>
             *     <li>Periodically checks the network sending queue for entries, if any are found sends them to the server</li>
             * </ul>
             * @since 1.0.0
             */
            private static LEDSuiteTask mgr = null;
            /**
             * The network listener is responsible for receiving packets.
             * <p>Main objectives:</p>
             * <ul>
             *     <li>Periodically checks for available data from the server. If any is found it tries to assign the received packet to a listener from the network listener collection according to a network id.</li>
             *     <li>If no network listener from the collection match the specific network id. The received data is processed using a standard implementation.</li>
             * </ul>
             * @since 1.0.0
             */
            private static LEDSuiteTask masterListener = null;

            private static NetworkHandle handle = null;

            /**
             * Used to monitor communication state between client and server.
             * @see SuccessCallback#getResult(boolean)
             * @since 1.0.0
             */
            public interface SuccessCallback {
                /**
                 * Called when the communication between server and client finished (a message was transferred to the server or an error occurred).
                 * @param success {@code true} If the message was sent successfully, {@code false} if any errors occurred during transfer or no server is connected
                 * @throws NetworkException if any errors occur, to allow for custom error handling
                 * @since 1.0.0
                 */
                void getResult(boolean success) throws NetworkException;
            }

            /**
             * Used to get the current connected socket object. If the socket is not yet connected, a new connection will be made.
             * @return The current connected socket object.
             * @throws NetworkException if the initialization of a new connection fails
             * @since 1.0.0
             */
            protected static Socket getServer() throws NetworkException {
                // if the server is not connected, initialize a new connection
                if (!isConnected()) {
                    init(success -> {
                        // if initialization fails, throw an exception
                        if (!success) throw new NetworkException("Failed to establish connection to the server!");
                    });
                }
                return server;
            }

            /**
             * Initializes a new connection to the server. <p>
             * Main objectives:
             * <ul>
             *     <li>Establish a new connection to the server</li>
             *     <li>Cancel any running tasks from last connection</li>
             *     <li>Create a new network manager, that is responsible for sending messages</li>
             *     <li>Request the initialization of a new network listener using {@link #initListener()}</li>
             * </ul>
             * @param callback used to communicate a result back to the caller method
             * @throws NetworkException if the attempt fails
             * @since 1.0.0
             */
            public static void init(SuccessCallback callback) throws NetworkException {
                if (serverIsRebooting) {
                    if (callback != null) callback.getResult(false);
                    return;
                }
                serverIsRebooting = true;
                try {
                    // if a socket is not initialized at all, create a new socket
                    // connects it to the new server
                    if (server == null || server.isClosed()) {
                        server = new Socket();
                        server.setReuseAddress(true);
                        server.connect(new InetSocketAddress(LEDSuite.server_settings.getIPv4(), LEDSuite.server_settings.getPort()), 3000);
                    }
                    LEDSuite.logger.verbose("Successfully connected to server!");
                } catch (IOException e) {
                    // if connection fails, inform the caller function using the callback
                    LEDSuite.logger.fatal("Failed to initialize connection to server! Error: " + e.getMessage());
                    LEDSuite.logger.displayError(e);
                    serverIsRebooting = false;
                    if (callback != null) callback.getResult(false);
                    return;
                }

                LEDSuite.logger.verbose("Fulfilling initialization request for Network Handler!");

                if (handle == null) {
                    LEDSuite.logger.verbose("Network Handler: starting network handle...");
                    LEDSuite.eventManager.registerEvents(handle = new NetworkHandle());
                    LEDSuite.logger.verbose("Network Handler: started network handle!");
                }


                // if manager is already running, cancel it
                if (mgr != null) mgr.cancel();
                long keepalive = 500;

                // initializing new time tracker to keep track of keepalive timings
                TimeManager.clearTimeTracker("keepalive");
                TimeManager.clearTimeTracker("mgr");
                TimeManager.initTimeTracker("mgr", 0, System.currentTimeMillis());
                TimeManager.initTimeTracker("keepalive", keepalive);

                // starting a new network manager
                LEDSuite.logger.verbose("Network Handler: starting manager...");
                mgr = new LEDSuiteRunnable() {
                    @Override
                    public void run() {
                        if (!TimeManager.call("mgr")) return;
                            // check if keepalive needs to be sent
                        if (TimeManager.call("keepalive")) {
                            LEDSuite.logger.verbose("Sending keepalive");
                            try {
                                // try sending a keepalive message to server
                                if (!sendKeepalive(
                                        // build new keepalive packet using YAMLSerializer
                                        YAMLMessage.builder()
                                                .setPacketType(YAMLMessage.PACKET_TYPE.request)
                                                .setRequestType(YAMLMessage.REQUEST_TYPE.keepalive)
                                                .build(),
                                        false
                                )
                                ) throw new NetworkException("Sending keepalive failed!");
                            } catch (YAMLSerializer.TODOException | ConfigurationException |
                                     YAMLSerializer.InvalidReplyTypeException |
                                     YAMLSerializer.InvalidPacketTypeException |
                                     NetworkException e) {
                                LEDSuite.logger.verbose(e instanceof NetworkException ? e.getMessage() : "Sending keepalive failed! (Not network related)");
                            }
                        }

                        // check if the status needs to be sent / updated
                        if (TimeManager.call("status")) {
                            // if the main window is open request status from server
                            if (LEDSuite.mainWindow != null) LEDSuite.mainWindow.getStatus(null);
                        }

                        // if the send-queue isn't empty and the server is connected
                        if (!networkQueue.isEmpty() && isConnected()) {
                            Map.Entry<Long, LEDSuiteRunnable> entry = networkQueue.firstEntry();
                            LEDSuite.logger.verbose("Handling request: " + entry.getKey());
                            entry.getValue().runTaskAsynchronously();
                            networkQueue.remove(entry.getKey());
                        }
                    }
                }.runTaskTimerAsynchronously(LEDSuite.argumentsSettings.getNetworkingCommunicationClock(), delay);
                LEDSuite.logger.verbose("Network Handler: started manager!");

                // initialize the listener
                initListener();

                LEDSuite.logger.verbose("Network Handler started!");

                serverIsRebooting = false;

                // informs the function caller of the result
                callback.getResult(true);
            }

            /**
             * Tries to initialize a new network listener.
             * <p>Main objectives:</p>
             * <ul>
             *     <li>Cancel any running tasks from previous connection</li>
             *     <li>Create and initialize a new network listener</li>
             * </ul>
             * @since 1.0.0
             */
            private static void initListener() {
                LEDSuite.logger.verbose("Network Handler: starting master listener...");
                // cancelling any active listeners from previous connection
                if (masterListener != null) masterListener.cancel();
                // creating and initializing new listener
                masterListener = new LEDSuiteRunnable() {
                    @Override
                    public void run() {
                        try {
                            // if no server is connected, do nothing
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
                                        LEDSuite.logger.displayError(e);
                                        LEDSuite.logger.warn(System.currentTimeMillis() + " Received reply with missing or invalid network id! Can't associate it with corresponding listener!");
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
                            LEDSuite.logger.fatal("Network Handler: master listener: Error: " + e.getMessage());
                            LEDSuite.logger.displayError(e);
                        }

                    }
                }.runTaskTimerAsynchronously(0, 1);
                LEDSuite.logger.verbose("Network Handler: started master listener!");
            }

            /**
             * Function that cancels the currently running network manager and listener. It also clears the network sending queue and the listener collection using {@link #clearQueues()}.
             * @since 1.0.0
             */
            public static void cancel() {
                LEDSuite.logger.verbose("Network Handler: Fulfilling cancel request!");
                // cancels the network manager if it is running
                if (mgr != null) mgr.cancel();
                // cancels the network listener if it is running
                if (masterListener != null) masterListener.cancel();
                // clearing network queue and listener collection
                clearQueues();
                // Informing the rest of the application that the current connection was closed
                LEDSuite.eventManager.fireEvent(new Events.Status(StatusUpdate.notConnected()));
            }

            /**
             * Clears the network sending queue and the listener collection.
             * @since 1.0.0
             */
            private static void clearQueues() {
                LEDSuite.logger.verbose("Network Handler: Fulfilling clear queues request!");
                // clears the network queue
                networkQueue.clear();
                Map<UUID, ReplyListener> replyListenerQueue = Collections.synchronizedMap(Networking.replyListeners);
                replyListenerQueue.clear();

            }

            /**
             * Closes the current connection and all handlers. Then initializes a new connection and restarts all handlers.
             * @implNote Uses {@link #cancel()}, {@link #clearQueues()} and {@link #init(SuccessCallback)}
             * @throws NetworkException if {@link #init(SuccessCallback)} fails, to allow for custom error handling
             * @since 1.0.0
             */
            public static void reboot() throws NetworkException {
                if (serverIsRebooting) return;
                LEDSuite.logger.verbose("Network Handler: Fulfilling reboot request!");
                // try to close the current connection
                try {
                    LEDSuite.logger.verbose("Network Handler: Closing socket");
                    server.close();
                } catch (Exception e) {
                    LEDSuite.logger.verbose("Network Handler: Closing failed, overwriting connection");
                }

                // canceling handlers
                LEDSuite.logger.verbose("Network Handler: Stopping mgr and listener tasks");
                cancel();

                // try to initialize a new connection and restart the handler with init(SuccessCallback)
                // if it fails to throw a new NetworkException to allow for custom error handling
                try {
                    LEDSuite.logger.verbose("Network Handler: initializing...");
                    init(success -> {
                        LEDSuite.logger.verbose("Network Handler: success: " + success);
                        if (!success) throw new NetworkException("connection failed");
                    });
                } catch (NetworkException e) {
                    LEDSuite.logger.fatal("Network Handler: reboot failed!");
                    LEDSuite.logger.displayError(e);
                    throw new NetworkException("connection failed!");
                }
            }

            /**
             * Wrapper function for {@link #reboot()}. Used when the user changes the host address or port.
             * @throws NetworkException if the reboot fails
             * @since 1.0.0
             */
            public static void hostChanged() throws NetworkException {
                reboot();
            }

            /**
             * Listener Class. Handles various events like {@link Events.Shutdown}, {@link Events.HostChanged} and {@link Events.SettingsChanged}.
             * @since 1.0.0
             */
            public static class NetworkHandle implements EventListener {

                @EventHandler
                public void onShutdown(Events.Shutdown e) {
                    // close current connection and shutdown handlers
                    LEDSuite.logger.verbose("Network Handler: network handle detected shutdown");
                    LEDSuite.logger.verbose("Network Handler: clearing queues and cancelling main tasks");
                    cancel();
                    try {
                        server.close();
                    } catch (IOException ex) {
                        LEDSuite.logger.verbose("Failed to close socket!");
                    }
                    LEDSuite.logger.verbose("Network Handler: shutdown complete");
                }

                @EventHandler
                public void onHostChanged(Events.HostChanged e) {
                    // call hostChanged and log potential errors
                    LEDSuite.logger.verbose("Network Handler: network handle detected host change");
                    try {
                        hostChanged();
                    } catch (NetworkException ex) {
                        LEDSuite.logger.displayError(ex);
                    }
                }

                @EventHandler
                public void onSettingChanged(Events.SettingChanged e) {
                    // name of the changed setting
                    String key = e.key();
                    // new value for the setting
                    Object value = e.value();

                    // some checks to prevent unnecessary network requests
                    if (value == null || key == null || key.isBlank() || key.isEmpty()) return;
                    LEDSuite.logger.verbose("Network Handler: network handle detected settings change (1)");
                    // send a settings change request to the server and log potential errors
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
                        LEDSuite.logger.error("Failed to send (1) settings change request!");
                    }
                    LEDSuite.logger.verbose("Successfully send (1) settings change request to server!");
                }
                @EventHandler
                public void onSettingsChanged(Events.SettingsChanged e) {
                    // names and new values for changed settings
                    HashMap<String, Object> changedSettings = e.changedSettings();
                    // check to prevent unnecessary network requests
                    if (changedSettings.isEmpty()) return;
                    LEDSuite.logger.verbose("Network Handler: network handle detected settings changes (" + changedSettings.size() +")");
                    // send a settings change request to the server and log potential errors
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
                        LEDSuite.logger.error("Failed to send (" + changedSettings.size() +") settings change request!");
                    }
                    LEDSuite.logger.verbose("Successfully send (" + changedSettings.size() +") settings change request to server!");
                }
            }

            /**
             * Listener object used to implement custom listener behaviour. Wrapper for {@link LEDSuiteProcessor}.
             * @param processor custom listener implementation
             * @since 1.0.0
             */
            private record ReplyListener(LEDSuiteProcessor processor) {

                /**
                 * Processes the specified YAML data with its LEDSuiteProcessor object.
                 * @param yaml input YAML message
                 * @since 1.0.0
                 */
                private void processFor(YAMLMessage yaml) {
                    processor.runTask(yaml);
                    LEDSuite.logger.verbose("Successfully processed received Reply Message with ID[" + yaml.getNetworkID() + "] using predefined LEDSuiteProcessor with ID[" + processor.getTaskId() + "]!");

                }
            }

            /**
             * Add a custom {@link ReplyListener} to the listener collection.
             * @param processor the custom listener implementation, received data is processed with
             * @param networkID id of the data to pass to this listener
             * @since 1.0.0
             */
            public static void listenForReply(LEDSuiteProcessor processor, UUID networkID) {
                Map<UUID, ReplyListener> replyListenerQueue = Collections.synchronizedMap(Networking.replyListeners);
                replyListenerQueue.put(networkID,
                        new ReplyListener(processor)
                );
            }

            /**
             * Add a default listener to the listener collection.
             * @implNote Simply fires a {@link Events.DataIn} if it receives any data, to offload the network listener.
             * @param networkID id of the data to pass to this listener
             * @since 1.0.0
             */
            protected static void listenForReply(UUID networkID) {
                Map<UUID, ReplyListener> replyListenerQueue = Collections.synchronizedMap(Networking.replyListeners);
                replyListenerQueue.put(networkID,
                        new ReplyListener(
                                new LEDSuiteProcessor() { // default listener processor
                                    @Override
                                    public void run(YAMLMessage yaml) {
                                        if (yaml != null) {

                                            // fire a new DataIn event to process the data elsewhere to offload the network listener
                                            LEDSuite.eventManager.fireEvent(new Events.DataIn(yaml));

                                        }
                                    }
                                }
                        )
                );
            }
        }

        /**
         * Receives data from an InputStream and wraps it in a {@link YAMLConfiguration}.
         * @param is the InputStream to receive data from
         * @return {@link YAMLConfiguration} if any data was received and processed without any errors, otherwise {@code null}
         * @since 1.0.0
         */
        public static YAMLConfiguration defaultReceive(InputStream is) {
            YAMLConfiguration yaml;
            try {
                // Wrap the InputStream with a BufferedReader for efficient reading
                char[] buffer = defaultRead(is);

                // Convert the buffer into a ByteArrayInputStream and load it into the YAMLConfiguration
                yaml = new YAMLConfiguration();
                new FileHandler(yaml).load(new ByteArrayInputStream(CharBuffer.wrap(buffer).toString().getBytes()));
            } catch (Exception e) {
                LEDSuite.logger.displayError(e);
                return null;
            }
            return yaml;
        }

        private static char[] defaultRead(InputStream is) throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            // Read the first line to get the total number of bytes expected
            int totalBytes = Integer.parseInt(br.readLine());
            //LEDSuite.logger.debug("Total bytes: " + totalBytes);

            // Prepare a buffer to read the expected number of bytes
            char[] buffer = new char[totalBytes];

            // Read the actual data into the buffer if not canceled
            if (br.read(buffer) < 1) throw new NullPointerException();
            return buffer;
        }

        /**
         * Default handler for a received YAML message. This is used if no custom handler was specified.
         * @param yaml the received YAML message
         * @since 1.0.0
         */
        public static void defaultHandle(YAMLMessage yaml) {
            // YAML message type check
            if (yaml.getPacketType().equals(YAMLMessage.PACKET_TYPE.error)) {
                // converts YAML message to wrapper class ServerError, fires a new error event
                LEDSuite.eventManager.fireEvent(new Events.Error(ServerError.fromYAMLMessage(yaml)));
            } else {
                // fires a general DataIn event to process data further elsewhere
                LEDSuite.eventManager.fireEvent(new Events.DataIn(yaml));
            }
        }

        /**
         * Generally used to check if a requested action was completed successfully or not.
         * @since 1.0.0
         */
        public interface FinishCallback {
            /**
             * Executed when the requested action finishes.
             * @param success if the requested action was successful
             * @since 1.0.0
             */
            void onFinish(boolean success);
        }

        /**
         * Sends a keepalive packet to the server.
         * @param yaml keepalive packet to send
         * @param displayLog if the sending process should be printed to console (disabled by default since it is quite verbose)
         * @return {@code true} if the keepalive packet was successfully sent to the server, otherwise {@code false}
         * @since 1.0.0
         */
        public static boolean sendKeepalive(YAMLConfiguration yaml, boolean displayLog) {
            return sendYAMLMessage(LEDSuite.server_settings.getIPv4(), LEDSuite.server_settings.getPort(), yaml, null, null, displayLog);
        }

        /**
         * Wrapper for {@link #sendYAML(String, int, YAMLConfiguration, FinishCallback)}.
         * <p>Requests to send specified YAML message to default host.</p>
         * @param yaml YAML message to send to the server
         * @since 1.0.0
         * @see #sendYAMLDefaultHost(YAMLConfiguration, FinishCallback)
         * @see #sendYAMLDefaultHost(YAMLConfiguration, FinishCallback, LEDSuiteProcessor)
         * @since 1.0.0
         */
        public static void sendYAMLDefaultHost(YAMLConfiguration yaml) {
            if (!sendYAML(LEDSuite.server_settings.getIPv4(), LEDSuite.server_settings.getPort(), yaml, null)) {
                LEDSuite.logger.error("Failed to send YAML message to server! Callback = false | ReplyHandler = false");
            }
        }

        /**
         * Wrapper for {@link #sendYAML(String, int, YAMLConfiguration, FinishCallback)}.
         * <p>Requests to send specified YAML message to default host, with specified successCallback.</p>
         * @param yaml YAML message to send to the server
         * @param callback completion monitor
         * @since 1.0.0
         * @see #sendYAMLDefaultHost(YAMLConfiguration)
         * @see #sendYAMLDefaultHost(YAMLConfiguration, FinishCallback, LEDSuiteProcessor)
         * @since 1.0.0
         */
        public static void sendYAMLDefaultHost(YAMLConfiguration yaml, FinishCallback callback) {
            if (!sendYAML(LEDSuite.server_settings.getIPv4(), LEDSuite.server_settings.getPort(), yaml, callback)) {
                LEDSuite.logger.error("Failed to send YAML message with callback to server! Callback = true | ReplyHandler = false");
            }
        }

        /**
         * Wrapper for {@link #sendYAML(String, int, YAMLConfiguration, FinishCallback, LEDSuiteProcessor)}.
         * <p>Requests to send specified YAML message to default host, with specified successCallback and reply handler.</p>
         * @param yaml YAML message to send to the server
         * @param callback completion monitor
         * @param replyHandler custom listener processor to process the response data with
         * @since 1.0.0
         * @see #sendYAMLDefaultHost(YAMLConfiguration)
         * @see #sendYAMLDefaultHost(YAMLConfiguration, FinishCallback)
         * @since 1.0.0
         */
        public static void sendYAMLDefaultHost(YAMLConfiguration yaml, FinishCallback callback, LEDSuiteProcessor replyHandler) {
            if (!sendYAML(LEDSuite.server_settings.getIPv4(), LEDSuite.server_settings.getPort(), yaml, callback, replyHandler)) {
                LEDSuite.logger.error("Failed to send YAML message with to server! Callback = true | ReplyHandler = true");
            }
        }

        /**
         * Wrapper for {@link #sendYAML(String, int, YAMLConfiguration, FinishCallback, LEDSuiteProcessor)}.
         * <p>Requests to send specified YAML message to specified host (address:port), with specified successCallback.</p>
         * @param host the host address (just for logging)
         * @param port the host port (just for logging)
         * @param yaml the YAML message to send
         * @param callback completion monitor to report completion state to
         * @return {@code true} If send request was successful, otherwise {@code false}
         * @since 1.0.0
         */
        public static boolean sendYAML(String host, int port, YAMLConfiguration yaml, FinishCallback callback) {
            return sendYAML(host, port, yaml, callback, null);
        }

        /**
         * Wrapper for {@link #sendYAML(String, int, YAMLConfiguration, FinishCallback, LEDSuiteProcessor, boolean)}.
         * <p>Requests to send specified YAML message to specified host (address:port), with specified successCallback and reply handler.</p>
         * @param host the host address (just for logging)
         * @param port the host port (just for logging)
         * @param yaml the YAML message to send
         * @param callback completion monitor to report completion state to
         * @return {@code true} If send request was successful, otherwise {@code false}
         * @since 1.0.0
         */
        public static boolean sendYAML(String host, int port, YAMLConfiguration yaml, FinishCallback callback, LEDSuiteProcessor replyHandler) {
            return sendYAML(host, port, yaml, callback, replyHandler, false);
        }

        /**
         * Creates a network queue entry from specified host address, port, YAML message and replay handler and adds it to the network queue based on priority. It also informs the completion monitor object of any errors or successful completion.
         * @param host the host address (just for logging)
         * @param port the host port (just for logging)
         * @param yaml the YAML message to send
         * @param callback completion monitor to report completion state to
         * @param replyHandler custom listener processor to process the response data with
         * @param priority if this network request should be prioritized
         * @return {@code true} If network queue entry was successfully added to the network queue, otherwise {@code false}
         * @since 1.0.0
         */
        public static boolean sendYAML(String host, int port, YAMLConfiguration yaml, FinishCallback callback, LEDSuiteProcessor replyHandler, boolean priority) {
            // if the socket isn't open and connected, try to reopen / reconnect
            // if that fails, to simply return false completion and cancel an attempt
            if (!NetworkHandler.isConnected()) {
                try {
                    NetworkHandler.reboot();
                    if (callback != null) callback.onFinish(true);
                    LEDSuite.logger.verbose("Successfully reconnected to previous server!");
                } catch (NetworkException e) {
                    LEDSuite.logger.fatal(e.getMessage());
                    LEDSuite.logger.error("Reconnection attempt to previous server failed!");
                    if (callback != null) callback.onFinish(false);
                    return false;
                }
            }
            // if the socket is connected or was successfully reconnected,
            // construct a new network queue entry out of, host, port, YAML, callback and replyHandler objects
            LEDSuite.logger.verbose("Appending send request to the network queue!");
            LEDSuiteRunnable sendRequest = new LEDSuiteRunnable() {
                @Override
                public void run() {
                    LEDSuite.logger.verbose("Sending packet: " + yaml.getProperty(Constants.Network.YAML.PACKET_TYPE));
                    sendYAMLMessage(host, port, yaml, callback, replyHandler);
                }
            };
            // put the network entry into the network queue based on priority
            // if priority is true, subtract 1s from the current time before adding the entry to the queue to give it priority
            long current = System.currentTimeMillis();
            networkQueue.put(priority ? current - 1000 : current, sendRequest);
            return true;
        }

        /**
         * Wrapper for {@link #sendYAMLMessage(String, int, YAMLConfiguration, FinishCallback, LEDSuiteProcessor, boolean)}.
         * @param serverIP4 the host address (just for logging)
         * @param serverPort the host port (just for logging)
         * @param yaml the YAML message to send
         * @param callback completion monitor to report completion state to
         * @param replyHandle custom listener processor to process the response data with
         * @since 1.0.0
         */
        private static void sendYAMLMessage(String serverIP4, int serverPort, YAMLConfiguration yaml, FinishCallback callback, LEDSuiteProcessor replyHandle) {
            if (!sendYAMLMessage(serverIP4, serverPort, yaml, callback, replyHandle, true)) {
                LEDSuite.logger.error("Failed to send YAML message to server!");
            }
        }

        /**
         * Sends YAML packets / messages to the current connected host / server.
         * <p>Main objectives:</p>
         * <ul>
         *     <li>Tries to get network id from the YAMl message object or requests a new one from {@link com.toxicstoxm.LEDSuite.logging.network.NetworkLogger}</li>
         *     <li>Adds the specified custom listener (or the default listener) to the listener collection</li>
         *     <li>Loads data and sends it via socket output stream to the server</li>
         *     <li>If an error occurs, {@link NetworkHandler#reboot()} is executed and a new message request is made before giving up</li>
         *     <li>Send monitor object {@link NetworkHandler.SuccessCallback} is notified of the result</li>
         * </ul>
         * @param serverIP4 the host address (just for logging)
         * @param serverPort the host port (just for logging)
         * @param yaml the YAML message to send
         * @param callback completion monitor to report completion state to
         * @param replyHandle custom listener processor to process the response data with
         * @param displayLog if the sending process should be printed to console
         * @return {@code true} if the YAML message was sent successfully, otherwise {@code false}
         * @since 1.0.0
         */
        private static boolean sendYAMLMessage(String serverIP4, int serverPort, YAMLConfiguration yaml, FinishCallback callback, LEDSuiteProcessor replyHandle, boolean displayLog) {
            boolean callb = callback != null;
            boolean err = false;

            // checking if network event id is given
            boolean noID = false;
            // figuring out network event id, if none is given, create a new one
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
                    // try to get network event id from the YAML file
                    networkID = yaml.getString(Constants.Network.YAML.INTERNAL_NETWORK_ID);
                    // if no id is given trigger creation of a new one
                    if (networkID == null || networkID.isBlank()) noID = true;
                } catch (NoSuchElementException e) {
                    // if the id check fails due to an error trigger creating of a new id
                    noID = true;
                }

                // if no id is given, get a new one from networkLogger
                if (noID) {
                    UUID uuid = LEDSuite.networkLogger.getRandomUUID(description);
                    id = "[" +
                            uuid +
                            "] ";
                    yaml.setProperty(Constants.Network.YAML.INTERNAL_NETWORK_ID, String.valueOf(uuid));
                    // if an id is give, pass it on to the network logger
                } else {
                    id = "[" + networkID + "] ";
                    LEDSuite.networkLogger.addEvent(UUID.fromString(networkID), description);
                }

                // general information messages
                LEDSuite.logger.verbose(id + "-------------------- Network Communication --------------------");
                LEDSuite.logger.verbose(id + "Type: client - data out");
                LEDSuite.logger.verbose(id + "Server: " + serverIP4);
                LEDSuite.logger.verbose(id + "Port: " + serverPort);

                // notify the rest of the application about the sending process
                LEDSuite.eventManager.fireEvent(new Events.DataOut(yaml));
            } else yaml.setProperty(Constants.Network.YAML.INTERNAL_NETWORK_ID, String.valueOf(UUID.randomUUID()));

            try {
                // get current connection and fetch network id from YAML message or request a new one from network logger
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

                // add custom listener to a listener collection
                // if no custom listener is given add the default listener to the listener collection
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
                    LEDSuite.logger.verbose(id + "Successfully established connection!");

                    LEDSuite.logger.verbose(id + "Creating data streams...");
                }
                // get socket output stream
                OutputStream out = socket.getOutputStream();
                // create new YAML message output stream
                ByteArrayOutputStream outputS = new ByteArrayOutputStream();

                // loading the YAML message into a byteArrayOutputStream using fileHandler built-in function
                if (displayLog) LEDSuite.logger.verbose(id + "Loading data to transmit...");
                new FileHandler(yaml).save(outputS);

                // inspecting loaded data, detecting data size and printing it to console
                if (displayLog) LEDSuite.logger.verbose(id + "Inspecting data:");
                byte[] bytes = outputS.toByteArray();
                int byteCount = bytes.length;
                boolean kb = byteCount > 8192;
                if (displayLog) LEDSuite.logger.verbose(id + "Size: " + (kb ? (byteCount / 1024) + "KB" : byteCount + " Bytes"));

                // sending yaml data to server
                if (displayLog) LEDSuite.logger.verbose(id + "Transmitting data...");
                out.write(bytes);
                if (displayLog) LEDSuite.logger.verbose(id + "Successfully transmitted data to server!");

                if (displayLog) {
                    LEDSuite.logger.verbose(id + "---------------------------------------------------------------");
                }

            } catch (IOException | ConfigurationException e) {
                // try restarting network communication and retry sending
                try {
                    NetworkHandler.reboot();
                    sendYAMLMessage(serverIP4, serverPort, yaml, callback, replyHandle);
                    LEDSuite.logger.displayError(e);
                } catch (NetworkException ex) {
                    // if an error occurs, print an error message and give up
                    if (displayLog) {
                        LEDSuite.logger.error(id + "Error occurred! Transmission terminated!");
                        LEDSuite.logger.displayError(e);
                    }
                    err = true;
                }
            } catch (NetworkException e) {
                if (displayLog) LEDSuite.logger.fatal(id + "Network error: " + e.getMessage());
            }
            finally {
                if (displayLog) LEDSuite.logger.verbose(id + "---------------------------------------------------------------");
            }

            // if a completion monitor object is given, inform it about the sending outcome
            err = !err;
            if (callb) callback.onFinish(err);
            return err;

        }
    }

    /**
     * Generally used to indicate that something network related has gone wrong.
     * @since 1.0.0
     */
    public static class NetworkException extends Exception {
        private NetworkException(String message) {
            super(message);
        }
    }

    /**
     * Generally used to indicate that something related with server communication has gone wrong.
     * <p>Specialization of {@link NetworkException}</p>
     * @since 1.0.0
     */
    public static class ServerCommunicationException extends NetworkException {
        public ServerCommunicationException(String message) {
            super(message);
        }
    }
}
