package com.toxicstoxm.LEDSuite.communication.websocket;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.StatusRequestPacket;
import com.toxicstoxm.LEDSuite.formatting.StringFormatter;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import jakarta.websocket.Session;
import lombok.Getter;
import org.glassfish.tyrus.client.ClientManager;

import java.net.URI;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper class that manages basic communication with the server. It features a sending queue (First In First Out) to ensure that the messages are sent in the correct order.
 * @since 1.0.0
 */
public class WebSocketClient {

    private final LinkedBlockingDeque<String> sendQueue = new LinkedBlockingDeque<>();

    private boolean cancelled = false;

    public WebSocketClient(Class<?> clientEndpoint, URI path) {
        run(clientEndpoint, path);
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

    @Getter
    private boolean connected = false;

    /**
     * Creates a new websocket client in an async thread and connects it to the specified server address.
     * @param clientEndpoint The client endpoint implementation with lifecycle methods
     * @param path The server address to connect to
     */
    private void run(Class<?> clientEndpoint, URI path) {
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
                    //session.setMaxIdleTimeout(Long.MAX_VALUE);
                    while (!cancelled) {
                        String toSend = sendQueue.poll(Long.MAX_VALUE, TimeUnit.DAYS);
                        // LEDSuiteApplication.getLogger().verbose("----------------------< OUT >----------------------", new LEDSuiteLogAreas.COMMUNICATION());
                        // LEDSuiteApplication.getLogger().verbose(toSend, new LEDSuiteLogAreas.COMMUNICATION());
                        // LEDSuiteApplication.getLogger().verbose("---------------------------------------------------", new LEDSuiteLogAreas.COMMUNICATION());
                        session.getAsyncRemote().sendText(
                                toSend
                        );
                    }
                } catch (Exception e) {
                     LEDSuiteApplication.getLogger().warn(e.getMessage(), new LEDSuiteLogAreas.NETWORK());
                } finally {
                    connected = false;
                }
            }
        }.runTaskAsynchronously();
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
}
