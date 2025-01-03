package com.toxicstoxm.LEDSuite.communication.websocket;

import com.toxicstoxm.LEDSuite.gettext.Translations;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.time.Action;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.dialogs.UpdateCallback;
import com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs.ErrorData;
import jakarta.websocket.*;
import lombok.Builder;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;

/**
 * WebSocket endpoint for uploading binary data to the server. This class manages the file upload process by sending
 * data packets to the server and tracking progress during the transfer.
 * <p>
 * The primary responsibilities of this class include:
 * <ul>
 *     <li>Establishing a WebSocket connection with the server.</li>
 *     <li>Sending the session ID to the server upon connection to identify the file upload session.</li>
 *     <li>Handling progress updates during the upload process.</li>
 *     <li>Handling connection events like opening, closing, and errors.</li>
 * </ul>
 * </p>
 *
 * @since 1.0.0
 */
@ClientEndpoint
@Builder
public class WebSocketUpload extends WebSocketClientEndpoint {

    // Indicates whether the upload is ready to start
    @Setter
    @Builder.Default
    private boolean ready = false;

    // Callback for updating progress during the upload
    private UpdateCallback<ProgressUpdate> progressUpdateUpdateCallback;

    // Callback for notifying when the connection is established
    private Action connectionNotifyCallback;

    // Unique session ID for this upload, generated on object creation
    @Builder.Default
    private String sessionID = String.valueOf(UUID.randomUUID());

    /**
     * Indicates if this WebSocket endpoint operates in binary mode.
     * <p>
     * This method returns {@code true}, indicating that this endpoint handles binary data.
     * </p>
     *
     * @return {@code true}, meaning the endpoint communicates using binary data.
     */
    @Override
    boolean binaryMode() {
        return true;
    }

    /**
     * Called when the WebSocket connection is successfully opened.
     * <p>
     * Upon opening the connection, this method sends the {@code sessionID} to the server, signaling the start of
     * the upload session. After that, it calls the {@link #connect()} method to notify that the connection is established.
     * </p>
     *
     * @param session The WebSocket session representing the connection.
     */
    @OnOpen
    public void onOpen(@NotNull Session session) {
        try {
            // Send the session ID to the server to initiate the upload session
            session.getBasicRemote().sendText(sessionID);
        } catch (IOException e) {
            LEDSuiteApplication.handleError(
                    ErrorData.builder()
                            .message(Translations.getText("Failed to send sessionID to server, terminating file upload!"))
                            .logArea(new LEDSuiteLogAreas.NETWORK())
                            .build()
            );
            throw new RuntimeException(e);
        }

        // Notify that the connection is established
        connect();

        // Call the superclass onOpen method
        super.onOpen(session);
    }

    /**
     * Called when a message is received from the server.
     * <p>
     * This method handles the incoming message from the server. In this implementation, it simply delegates the message
     * handling to the superclass method for logging and further processing.
     * </p>
     *
     * @param message The message received from the server.
     * @param session The WebSocket session from which the message was received.
     */
    @OnMessage
    public void onMessage(String message, @NotNull Session session) {
        super.onMessage(message, session);
    }

    /**
     * Called when the WebSocket connection is closed.
     * <p>
     * This method logs the connection closure event and delegates the call to the superclass's {@code onClose} method.
     * </p>
     *
     * @param session The WebSocket session representing the connection.
     */
    @OnClose
    public void onClose(@NotNull Session session) {
        super.onClose(session);
    }

    /**
     * Called when an error occurs during the WebSocket session.
     * <p>
     * This method logs the error and delegates the error handling to the superclass method.
     * </p>
     *
     * @param session The WebSocket session where the error occurred.
     * @param throwable The exception or error that occurred during the session.
     */
    @OnError
    public void onError(@NotNull Session session, @NotNull Throwable throwable) {
        super.onError(session, throwable);
    }

    /**
     * Updates the upload progress.
     * <p>
     * This method is called to provide progress updates during the file upload process. If a progress update callback is set,
     * it invokes the callback with the current progress.
     * </p>
     *
     * @param progressUpdate The current progress update information.
     */
    protected void update(ProgressUpdate progressUpdate) {
        if (progressUpdateUpdateCallback != null) {
            progressUpdateUpdateCallback.update(progressUpdate);
        }
    }

    /**
     * Notifies that the connection is established and ready for the upload.
     * <p>
     * This method is called once the WebSocket connection is established and the session ID is sent to the server. If
     * a connection notification callback is set, it will be executed.
     * </p>
     */
    protected void connect() {
        if (connectionNotifyCallback != null) {
            connectionNotifyCallback.run();
        }
    }
}
