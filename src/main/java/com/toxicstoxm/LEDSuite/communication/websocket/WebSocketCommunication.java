package com.toxicstoxm.LEDSuite.communication.websocket;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.StatusRequestPacket;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog.ServerState;
import jakarta.websocket.*;
import org.jetbrains.annotations.NotNull;

/**
 * WebSocket endpoint for general communication with the server. This class handles the communication channel
 * for exchanging general information, sending status requests, and handling errors that may arise during the WebSocket session.
 * <p>
 * The primary responsibility of this class is to manage the WebSocket connection and process incoming and outgoing messages.
 * This includes:
 * <ul>
 *     <li>Opening and closing WebSocket sessions.</li>
 *     <li>Handling incoming messages by deserializing them into {@link CommunicationPacket} objects.</li>
 *     <li>Sending status requests after the connection with the server is made.</li>
 *     <li>Logging detailed information about WebSocket communication events and errors.</li>
 * </ul>
 * </p>
 *
 * @since 1.0.0
 */
@ClientEndpoint
public class WebSocketCommunication extends WebSocketClientEndpoint {

    /**
     * Specifies that this WebSocket endpoint operates in text mode rather than binary mode.
     * <p>
     * The {@code binaryMode()} method indicates whether this endpoint uses binary communication or text communication.
     * In this implementation, text communication is used.
     * </p>
     *
     * @return {@code false}, indicating the endpoint uses text-based communication.
     */
    @Override
    boolean binaryMode() {
        return false;
    }

    /**
     * Called when the WebSocket connection is successfully opened.
     * <p>
     * This method is invoked when the WebSocket connection is established.
     * Upon opening the connection,
     * a status request is sent to the server, and the UI is updated to reflect that the server is now connected.
     * </p>
     *
     * @param session The WebSocket session representing the connection.
     */
    @OnOpen
    public void onOpen(@NotNull Session session) {
        LEDSuiteApplication.getLogger().info("WebSocket connection opened with session ID: " + session.getId(), new LEDSuiteLogAreas.NETWORK());

        // Send a status request to the server
        LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                StatusRequestPacket.builder().build().serialize()
        );

        // Update the UI to reflect the server is connected
        LEDSuiteApplication.getWindow().setServerConnected(true);
    }

    /**
     * Called when a message is received from the server.
     * <p>
     * This method processes incoming messages from the server by logging them, deserializing them into
     * {@link CommunicationPacket} objects, and passing them to the packet handler for further processing.
     * </p>
     *
     * @param message The message received from the server.
     * @param session The WebSocket session from which the message was received.
     */
    @OnMessage
    public void onMessage(String message, @NotNull Session session) {
        LEDSuiteApplication.getLogger().verbose("----------------------< IN >----------------------" + "\n[Session] " + session.getId(), new LEDSuiteLogAreas.COMMUNICATION());
        LEDSuiteApplication.getLogger().verbose(message, new LEDSuiteLogAreas.COMMUNICATION());
        LEDSuiteApplication.getLogger().verbose("--------------------------------------------------", new LEDSuiteLogAreas.COMMUNICATION());

        // Deserialize the incoming message into a CommunicationPacket
        CommunicationPacket incomingPacket = LEDSuiteApplication.getPacketManager().deserialize(message);

        // Pass the deserialized packet to the packet handler for further processing
        LEDSuiteApplication.getPacketReceivedHandler().handleIncomingPacket(incomingPacket);
    }

    /**
     * Called when the WebSocket connection is closed.
     * <p>
     * This method logs the connection closure event and updates the UI to indicate that the server is disconnected.
     * </p>
     *
     * @param session The WebSocket session representing the connection.
     */
    @OnClose
    public void onClose(@NotNull Session session) {
        LEDSuiteApplication.getLogger().info("WebSocket connection closed with session ID: " + session.getId(), new LEDSuiteLogAreas.NETWORK());

        // Update the UI to reflect the server state
        LEDSuiteApplication.getWindow().setServerState(ServerState.DISCONNECTED);
        LEDSuiteApplication.getWindow().setServerConnected(false);
    }

    /**
     * Called when an error occurs during the WebSocket session.
     * <p>
     * This method logs the error message and the stack trace for debugging purposes when an exception occurs during communication.
     * </p>
     *
     * @param session The WebSocket session where the error occurred.
     * @param throwable The exception or error that occurred during the session.
     */
    @OnError
    public void onError(@NotNull Session session, @NotNull Throwable throwable) {
        LEDSuiteApplication.getLogger().error("Error in WebSocket session ID " + session.getId() + ": " + throwable.getMessage(), new LEDSuiteLogAreas.NETWORK());

        // Log each element of the stack trace for detailed error information
        for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
            LEDSuiteApplication.getLogger().stacktrace(String.valueOf(stackTraceElement), new LEDSuiteLogAreas.NETWORK());
        }
    }
}
