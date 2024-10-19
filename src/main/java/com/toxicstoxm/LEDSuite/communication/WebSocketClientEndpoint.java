package com.toxicstoxm.LEDSuite.communication;

import jakarta.websocket.*;

@ClientEndpoint
public class WebSocketClientEndpoint {

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("New connection established, session id: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("Received message: " + message + " from session: " + session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("Connection closed, session id: " + session.getId());
    }
}