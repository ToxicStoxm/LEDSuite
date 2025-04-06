package com.toxicstoxm.LEDSuite.communication.websocket;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.StatusRequestPacket;
import com.toxicstoxm.LEDSuite.formatting.StringFormatter;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.tools.ExceptionTools;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs.MessageData;
import com.toxicstoxm.YAJL.Logger;
import jakarta.websocket.Session;
import lombok.Getter;
import org.glassfish.tyrus.client.ClientManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper class for managing WebSocket communication with a server.
 * <p>
 * This class provides functionality for sending messages in both text and binary formats, with a FIFO (First In, First Out)
 * queue to ensure the correct order of messages. It also manages the connection status, heartbeat mechanism, and the periodic
 * status request for the server.
 * </p>
 *
 * <h3>Usage Example</h3>
 * <pre>
 *     WebSocketClient webSocketClient = new WebSocketClient(new MyClientEndpoint(), URI.create("ws://example.com/socket"));
 *     webSocketClient.enqueueMessage("Hello, Server!");
 *     BinaryPacket packet = new BinaryPacket(ByteBuffer.wrap(new byte[]{1, 2, 3}), false);
 *     webSocketClient.enqueueBinaryMessage(packet);
 * </pre>
 *
 * @since 1.0.0
 */
public class WebSocketClient {

    private static final Logger logger = Logger.autoConfigureLogger();

    private final LinkedBlockingDeque<String> sendQueue = new LinkedBlockingDeque<>();
    private final LinkedBlockingDeque<BinaryPacket> sendQueueBinary = new LinkedBlockingDeque<>();
    private boolean cancelled = false;

    /**
     * Check if this WebSocketClient instance has already been closed using {@link #shutdown()}.
     *
     * @return {@code true} if {@link #shutdown()} was already called for this instance, otherwise {@code false}.
     */
    public boolean isClosed() {
        return cancelled;
    }

    @Getter
    private boolean connected = false;

    /**
     * Creates a new WebSocket client, connecting to the specified server URI.
     * <p>
     * The connection is established asynchronously, and a status request clock task is started if the endpoint supports it.
     * </p>
     *
     * @param clientEndpoint The client endpoint implementation containing lifecycle methods.
     * @param path The URI of the server to connect to.
     */
    public WebSocketClient(WebSocketClientEndpoint clientEndpoint, URI path) {
        run(clientEndpoint, path);
        if (clientEndpoint instanceof WebSocketCommunication) startStatusRequestClockTask();
    }

    private void startStatusRequestClockTask() {
        new LEDSuiteRunnable() {
            @Override
            public void run() {
                if (cancelled) this.cancel();
                enqueueMessage(
                        StatusRequestPacket.builder().build().serialize()
                );
            }
        }.runTaskTimerAsynchronously(1000, 1000);
    }

    /**
     * Connects the client to the specified WebSocket server asynchronously.
     * <p>
     * The connection is managed by the {@code ClientManager} from the Tyrus WebSocket client library. Depending on the mode
     * of the endpoint (binary or text), it will handle the appropriate communication.
     * </p>
     *
     * @param clientEndpoint The client endpoint implementation with lifecycle methods.
     * @param path The URI of the server to connect to.
     */
    private void run(WebSocketClientEndpoint clientEndpoint, URI path) {
        logger.verbose(" > Deploying new websocket client: Endpoint = {} URI = {}", StringFormatter.getClassName(getClass()), path);
        new LEDSuiteRunnable() {
            @Override
            public void run() {
                ClientManager clientManager = ClientManager.createClient();

                try (Session session = clientManager.connectToServer(clientEndpoint, path)) {
                    connected = true;
                    while (!cancelled && session.isOpen()) {
                        if (clientEndpoint.binaryMode()) {
                            binaryEndpointHeartBeat(clientEndpoint, session);
                            continue;
                        }
                        textEndpointHeartBeat(session);
                    }
                } catch (Exception e) {
                    logger.verbose(" > {}", e.getMessage());
                    ExceptionTools.printStackTrace(e, logger::stacktrace);
                } finally {
                    if (!cancelled && !LEDSuiteApplication.isConnecting()) {
                        LEDSuiteApplication.notifyUser(
                                MessageData.builder()
                                        .source("Communication")
                                        .heading("Connection Lost")
                                        .message("The connection to '" + path + "' timed out!")
                                        .build()
                        );
                    }
                    connected = false;
                }
            }
        }.runTaskAsynchronously();
    }

    /**
     * Sends text messages to the server.
     * <p>
     * This method continuously checks the text message queue and sends messages asynchronously. It will block if the queue is empty.
     * </p>
     *
     * @param session The WebSocket session used for communication.
     * @throws InterruptedException if the thread is interrupted while waiting for a message.
     * @throws IOException if an error occurs while sending the message.
     */
    private void textEndpointHeartBeat(@NotNull Session session) throws InterruptedException, IOException {
        String toSend = sendQueue.poll(Long.MAX_VALUE, TimeUnit.DAYS);
        session.getBasicRemote().sendText(toSend);
    }

    /**
     * Sends binary data packets to the server.
     * <p>
     * This method checks the binary packet queue and sends packets asynchronously. It handles the packet transfer progress and
     * tracks the time taken for each packet to be sent.
     * </p>
     *
     * @param clientEndpoint The client endpoint managing the connection.
     * @param session The WebSocket session used for communication.
     * @throws InterruptedException if the thread is interrupted while waiting for a message.
     * @throws IOException if an error occurs while sending the message.
     */
    private void binaryEndpointHeartBeat(WebSocketClientEndpoint clientEndpoint, Session session) throws InterruptedException, IOException {
        if (clientEndpoint instanceof WebSocketUpload fileTransfer) {
            if (!fileTransfer.isReady()) {
                Thread.sleep(1);
                return;
            }
            BinaryPacket binaryPacket = sendQueueBinary.poll(Long.MAX_VALUE, TimeUnit.DAYS);
            if (binaryPacket == null)
                throw new IllegalArgumentException("Binary packet can't be null!");

            ByteBuffer data = binaryPacket.data();
            boolean isLast = binaryPacket.isLast();
            long start = System.currentTimeMillis();

            session.getBasicRemote().sendBinary(data, isLast);

            long timeElapsed = System.currentTimeMillis() - start;

            fileTransfer.update(
                    ProgressUpdate.builder()
                            .data(data)
                            .lastPacket(isLast)
                            .timeElapsed(timeElapsed)
                            .build()
            );

            if (isLast) {
                logger.verbose(" > Last packet was successfully transferred to the server. Closing session: {}", session.getId());
                shutdown();
            }
        }
    }

    /**
     * Shuts down the WebSocket client, closing the connection and stopping further communication.
     */
    public void shutdown() {
        logger.verbose(" > {}: Shutdown triggered!", StringFormatter.getClassName(getClass()));
        cancelled = true;
    }

    /**
     * Adds a message to the text message queue for sending to the server.
     * <p>
     * This method returns immediately and does not guarantee the message will be sent right away.
     * </p>
     *
     * @param message the message to send to the server.
     * @return {@code true} if the message was successfully added to the queue, otherwise {@code false}.
     */
    public boolean enqueueMessage(String message) {
        if (message == null) {
            logger.warn("Refused null message!");
        }
        return sendQueue.offer(message);
    }

    /**
     * Adds a binary packet to the binary message queue for sending to the server.
     * <p>
     * This method returns immediately and does not guarantee the packet will be sent right away.
     * </p>
     *
     * @param binaryPacket the binary packet to send to the server.
     * @return {@code true} if the packet was successfully added to the queue, otherwise {@code false}.
     */
    public boolean enqueueBinaryMessage(BinaryPacket binaryPacket) {
        return sendQueueBinary.offer(binaryPacket);
    }
}
