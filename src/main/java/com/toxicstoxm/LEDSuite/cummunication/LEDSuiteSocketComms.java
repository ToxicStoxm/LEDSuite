package com.toxicstoxm.LEDSuite.cummunication;

import jakarta.websocket.*;

@ClientEndpoint()
public class LEDSuiteSocketComms {

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connection opened with session ID: " + session.getId());
        // You can send an initial message to the server if needed
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("Message received from server: " + message);
        // Handle the message received from the server
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Error on session " + session.getId() + ": " + throwable.getMessage());
        throwable.printStackTrace();
        // Handle errors, perhaps try to reconnect or alert the user
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("Connection closed. Reason: " + closeReason.getReasonPhrase());
        // Perform cleanup or notify the user of the disconnection
    }

}
