package com.toxicstoxm.LEDSuite.communication.websocket;

import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import jakarta.websocket.*;
import org.jetbrains.annotations.NotNull;

/**
 * File transfer / upload endpoint. Used for uploading files to the server.
 * @since 1.0.0
 */
@ClientEndpoint
public class WebSocketFileTransfer extends WebSocketClientEndpoint {

    @OnOpen
    public void onOpen(@NotNull Session session) {

        // TODO send over file transfer endpoint, not communication endpoint
        LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                session.getId()
        );

        super.onOpen(session);
    }

    @OnMessage
    public void onMessage(String message, @NotNull Session session) {
        super.onMessage(message, session);
    }

    @OnClose
    public void onClose(@NotNull Session session) {
        super.onClose(session);
    }

    @OnError
    public void onError(@NotNull Session session, @NotNull Throwable throwable) {
        super.onError(session, throwable);
    }
}
