package com.toxicstoxm.LEDSuite.communication.websocket;

import com.toxicstoxm.LEDSuite.time.Action;
import com.toxicstoxm.LEDSuite.ui.dialogs.UpdateCallback;
import jakarta.websocket.*;
import lombok.Builder;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@ClientEndpoint
@Builder
public class WebSocketUpload extends WebSocketClientEndpoint {

    @Setter
    @Builder.Default
    private boolean ready = false;

    @Override
    public boolean isReady() {
        return ready;
    }

    private UpdateCallback<ProgressUpdate> progressUpdateUpdateCallback;
    private Action connectionNotifyCallback;
    @Builder.Default
    private String sessionID = String.valueOf(UUID.randomUUID());

    @Override
    boolean binaryMode() {
        return true;
    }

    @OnOpen
    public void onOpen(@NotNull Session session) {

        session.getAsyncRemote().sendText(sessionID);
        connect();

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

    protected void update(ProgressUpdate progressUpdate) {
        if (progressUpdateUpdateCallback != null) progressUpdateUpdateCallback.update(progressUpdate);
    }

    protected void connect() {
        if (connectionNotifyCallback != null) connectionNotifyCallback.run();
    }

}
