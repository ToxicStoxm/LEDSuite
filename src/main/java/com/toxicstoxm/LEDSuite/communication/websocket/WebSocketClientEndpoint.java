package com.toxicstoxm.LEDSuite.communication.websocket;

import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import jakarta.websocket.*;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract WebSocket client endpoint implementation that provides lifecycle event handling.
 * <p>
 * This class serves as the base class for implementing WebSocket client endpoints, providing hooks for connection events,
 * message handling, error handling, and connection closure. It allows customization of the communication mode (binary or text)
 * through the {@link #binaryMode()} method, which determines the communication format for the client.
 * </p>
 *
 * <h3>Lifecycle Events:</h3>
 * The following methods are triggered during the WebSocket session lifecycle:
 * <ul>
 *     <li>{@link #onOpen(Session)}: Called when the WebSocket connection is opened.</li>
 *     <li>{@link #onMessage(String, Session)}: Called when a message is received from the server.</li>
 *     <li>{@link #onClose(Session)}: Called when the WebSocket connection is closed.</li>
 *     <li>{@link #onError(Session, Throwable)}: Called when an error occurs during communication.</li>
 * </ul>
 *
 * @since 1.0.0
 */
@ClientEndpoint
public abstract class WebSocketClientEndpoint {

    /**
     * Returns {@code true} if this WebSocket endpoint communicates using binary data only.
     * <p>
     * If the endpoint communicates using text data only, this method should return {@code false}.
     * </p>
     *
     * @return {@code true} for binary communication, {@code false} for text communication.
     */
    abstract boolean binaryMode();

    /**
     * Returns {@code true} if the WebSocket endpoint is ready to send or receive data.
     * <p>
     * This method is used to check the readiness of the endpoint before initiating any communication. The default
     * implementation returns {@code true}, but this can be overridden for custom behavior.
     * </p>
     *
     * @return {@code true} if the endpoint is ready, otherwise {@code false}.
     */
    public boolean isReady() {
        return true;
    }

    /**
     * Called when the WebSocket connection is opened.
     * <p>
     * This method logs the session ID when the connection is successfully established.
     * </p>
     *
     * @param session The WebSocket session representing the connection.
     */
    @OnOpen
    public void onOpen(@NotNull Session session) {
        LEDSuiteApplication.getLogger().info("WebSocket connection opened with session ID: " + session.getId(), new LEDSuiteLogAreas.NETWORK());
    }

    /**
     * Called when a text message is received from the server.
     * <p>
     * This method logs the message and the session ID associated with the communication.
     * </p>
     *
     * @param message The message received from the server.
     * @param session The WebSocket session from which the message was received.
     */
    @OnMessage
    public void onMessage(String message, @NotNull Session session) {
        LEDSuiteApplication.getLogger().info("Received message from session ID " + session.getId() + ": " + message, new LEDSuiteLogAreas.COMMUNICATION());
    }

    /**
     * Called when the WebSocket connection is closed.
     * <p>
     * This method logs the session ID when the connection is closed.
     * </p>
     *
     * @param session The WebSocket session representing the connection.
     */
    @OnClose
    public void onClose(@NotNull Session session) {
        LEDSuiteApplication.getLogger().info("WebSocket connection closed with session ID: " + session.getId(), new LEDSuiteLogAreas.NETWORK());
    }

    /**
     * Called when an error occurs in the WebSocket session.
     * <p>
     * This method logs the session ID and the error message when an exception is encountered during communication.
     * </p>
     *
     * @param session The WebSocket session where the error occurred.
     * @param throwable The exception or error that occurred during the session.
     */
    @OnError
    public void onError(@NotNull Session session, @NotNull Throwable throwable) {
        LEDSuiteApplication.getLogger().error("Error in WebSocket session ID " + session.getId() + ": " + throwable.getMessage(), new LEDSuiteLogAreas.NETWORK());
    }
}
