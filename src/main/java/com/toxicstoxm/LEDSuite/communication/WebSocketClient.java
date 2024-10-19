package com.toxicstoxm.LEDSuite.communication;

import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Session;
import org.glassfish.tyrus.client.ClientManager;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;


public class WebSocketClient {

    private final LinkedBlockingDeque<String> sendQueue = new LinkedBlockingDeque<>();

    private boolean cancelled = false;

    public WebSocketClient(Class<?> clientEndpoint, URI path) {
        run(clientEndpoint, path);
    }

    private void run(Class<?> clientEndpoint, URI path) {
        new LEDSuiteRunnable() {
            @Override
            public void run() {
                ClientManager clientManager = ClientManager.createClient();

                try (Session session = clientManager.connectToServer(
                        clientEndpoint,
                        path
                )) {
                    while (!cancelled) {
                        String toSend = sendQueue.poll(Long.MAX_VALUE, TimeUnit.DAYS);
                        LEDSuiteApplication.getLogger().verbose("Sending: " + toSend);
                        session.getAsyncRemote().sendText(
                                toSend
                        );
                    }
                } catch (DeploymentException | IOException | InterruptedException e) {
                     LEDSuiteApplication.getLogger().warn(e.getMessage());
                }
            }
        }.runTaskAsynchronously();
    }

    public void shutdown() {
        cancelled = true;
    }

    public boolean send(String message) {
        return sendQueue.offer(message);
    }

}
