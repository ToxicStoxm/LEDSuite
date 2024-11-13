package com.toxicstoxm.LEDSuite.communication.websocket;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.StatusRequestPacket;
import com.toxicstoxm.LEDSuite.formatting.StringFormatter;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.tools.ExceptionTools;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
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
 * Wrapper class that manages basic communication with the server. It features a sending queue (First In First Out) to ensure that the messages are sent in the correct order.
 * @since 1.0.0
 */
public class WebSocketClient {

    private final LinkedBlockingDeque<String> sendQueue = new LinkedBlockingDeque<>();

    private final LinkedBlockingDeque<BinaryPacket> sendQueueBinary = new LinkedBlockingDeque<>();

    private boolean cancelled = false;

    @Getter
    private boolean connected = false;

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
     * Creates a new websocket client in an async thread and connects it to the specified server address.
     * @param clientEndpoint The client endpoint implementation with lifecycle methods
     * @param path The server address to connect to
     */
    private void run(WebSocketClientEndpoint clientEndpoint, URI path) {
        LEDSuiteApplication.getLogger().info("Deploying new websocket client: Endpoint = " + StringFormatter.getClassName(getClass()) + " URI = " + path, new LEDSuiteLogAreas.NETWORK());
        new LEDSuiteRunnable() {
            @Override
            public void run() {
                ClientManager clientManager = ClientManager.createClient();

                try (Session session = clientManager.connectToServer(
                        clientEndpoint,
                        path
                )) {
                    connected = true;
                    while (!cancelled && session.isOpen()) {
                        if (clientEndpoint.binaryMode()) {
                            binaryEndpointHeartBeat(clientEndpoint, session);
                            continue;
                        }
                        textEndpointHeartBeat(session);
                    }
                } catch (Exception e) {
                    LEDSuiteApplication.getLogger().warn(e.getMessage(), new LEDSuiteLogAreas.NETWORK());
                    ExceptionTools.printStackTrace(e, message -> LEDSuiteApplication.getLogger().stacktrace(message, new LEDSuiteLogAreas.COMMUNICATION()));

                } finally {
                    connected = false;
                }
            }
        }.runTaskAsynchronously();
    }

    private void textEndpointHeartBeat(@NotNull Session session) throws InterruptedException, IOException {
        String toSend = sendQueue.poll(Long.MAX_VALUE, TimeUnit.DAYS);
        session.getBasicRemote().sendText(
                toSend
        );
    }

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
                LEDSuiteApplication.getLogger().info("Last packet was successfully transferred to the server. Closing session: " + session.getId(), new LEDSuiteLogAreas.NETWORK());
                shutdown();
            }
        }
    }

    /**
     * Stops this websocket client.
     */
    public void shutdown() {
        LEDSuiteApplication.getLogger().info(StringFormatter.getClassName(getClass()) + ": Shutdown triggered!", new LEDSuiteLogAreas.NETWORK());
        cancelled = true;
    }

    /**
     * Adds the specified message to the sending queue.<br>
     * WARNING: This does not guarantee that this message will be sent.
     * @param message the message to send to the server
     * @return {@code true} if the message was successfully added to the sending queue, otherwise {@code false}.
     */
    public boolean enqueueMessage(String message) {
        return sendQueue.offer(message);
    }

    /**
     * Adds the specified {@link BinaryPacket} to the sending queue.<br>
     * WARNING: This does not guarantee that this {@link BinaryPacket} will be sent.
     * @param binaryPacket the {@link BinaryPacket} to should be sent to the server
     * @return {@code true} if the {@link BinaryPacket} was successfully added to the sending queue, otherwise {@code false}.
     */
    public boolean enqueueBinaryMessage(BinaryPacket binaryPacket) {
        return sendQueueBinary.offer(binaryPacket);
    }
}
