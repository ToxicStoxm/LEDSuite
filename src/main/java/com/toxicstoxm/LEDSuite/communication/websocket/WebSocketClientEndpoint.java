package com.toxicstoxm.LEDSuite.communication.websocket;

import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import jakarta.websocket.*;
import org.jetbrains.annotations.NotNull;

/**
 * Default websocket client endpoint implementation.
 * @since 1.0.0
 */
@ClientEndpoint
public abstract class WebSocketClientEndpoint {

    abstract boolean binaryOnly();

    @OnOpen
    public void onOpen(@NotNull Session session) {
        LEDSuiteApplication.getLogger().info("WebSocket connection opened with session ID: " + session.getId(), new LEDSuiteLogAreas.NETWORK());
    }

    @OnMessage
    public void onMessage(String message, @NotNull Session session) {
        LEDSuiteApplication.getLogger().info("Received message from session ID " + session.getId() + ": " + message, new LEDSuiteLogAreas.COMMUNICATION());
    }

    @OnClose
    public void onClose(@NotNull Session session) {
        LEDSuiteApplication.getLogger().info("WebSocket connection closed with session ID: " + session.getId(), new LEDSuiteLogAreas.NETWORK());
    }

    @OnError
    public void onError(@NotNull Session session, @NotNull Throwable throwable) {
        LEDSuiteApplication.getLogger().error("Error in WebSocket session ID " + session.getId() + ": " + throwable.getMessage(), new LEDSuiteLogAreas.NETWORK());
    }
}