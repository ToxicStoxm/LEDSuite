package com.toxicstoxm.LEDSuite.communication.websocket;

import jakarta.websocket.*;

@ClientEndpoint
public class WebSocketFileTransfer extends WebSocketClientEndpoint {

    @OnOpen
    public void onOpen(Session session) {
        super.onOpen(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        super.onMessage(message, session);
    }

    @OnClose
    public void onClose(Session session) {
        super.onClose(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        super.onError(session, throwable);
    }
}
