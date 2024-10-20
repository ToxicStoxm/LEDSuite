package com.toxicstoxm.LEDSuite.communication;

import jakarta.websocket.*;

public abstract class WebSocketClientEndpoint {

    public abstract void onOpen(Session session);

    public abstract void onMessage(String message, Session session);

    public abstract void onClose(Session session);

    public abstract void onError(Session session, Throwable throwable);
}