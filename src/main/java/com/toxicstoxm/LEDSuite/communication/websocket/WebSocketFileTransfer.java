package com.toxicstoxm.LEDSuite.communication.websocket;

import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.dialogs.UpdateCallback;
import jakarta.websocket.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * File transfer / upload endpoint. Used for uploading files to the server.
 * @since 1.0.0
 */
@ClientEndpoint
public class WebSocketFileTransfer extends WebSocketClientEndpoint implements ProgressUpdater {

    private final UpdateCallback<String> onConnectCb;
    private final UpdateCallback<ProgressUpdate> onProgress;
    private final IsReadyCallback readyCallback;
    private final String uploadID;

    public WebSocketFileTransfer(UpdateCallback<String> onConnect, UpdateCallback<ProgressUpdate> onProgress, IsReadyCallback isReadyCallback, String uploadID) {
        this.onConnectCb = onConnect;
        this.onProgress = onProgress;
        this.readyCallback = isReadyCallback;
        this.uploadID = uploadID;
    }

    boolean ready() {return readyCallback.isReady();}

    @Override
    boolean binaryOnly() {
        return true;
    }

    @OnOpen
    public void onOpen(@NotNull Session session) {
        super.onOpen(session);

        try {
            session.getBasicRemote().sendText(uploadID);
            onConnect(uploadID);
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
        LEDSuiteApplication.getLogger().info("WebSocket connection closed with session ID: " + session.getId(), new LEDSuiteLogAreas.NETWORK());
    }

    @OnError
    public void onError(@NotNull Session session, @NotNull Throwable throwable) {
        super.onError(session, throwable);
    }

    @Override
    public void onConnect(String sessionID) {
        onConnectCb.update(sessionID);
    }

    @Override
    public void update(ProgressUpdate newValues) {
        onProgress.update(newValues);
    }
}
