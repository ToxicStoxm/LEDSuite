package com.toxicstoxm.LEDSuite.communication.websocket;

import jakarta.websocket.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * File transfer / upload endpoint. Used for uploading files to the server.
 * @since 1.0.0
 */
@ClientEndpoint
public abstract class WebSocketFileTransfer extends WebSocketClientEndpoint implements ProgressUpdater {

    @Override
    boolean binaryOnly() {
        return true;
    }

    @OnOpen
    public void onOpen(@NotNull Session session) {
        super.onOpen(session);
        try {
            String sessionID = session.getId();
            session.getBasicRemote().sendText(sessionID);
            onConnect(sessionID);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
