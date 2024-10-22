package com.toxicstoxm.LEDSuite.communication.websocket;

import com.toxicstoxm.LEDSuite.communication.packet_management.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.status.StatusRequestPacket;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import jakarta.websocket.*;

@ClientEndpoint
public class WebSocketCommunication extends WebSocketClientEndpoint {

    @OnOpen
    public void onOpen(Session session) {
        LEDSuiteApplication.getLogger().info("WebSocket connection opened with session ID: " + session.getId(), new LEDSuiteLogAreas.NETWORK());

        LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                StatusRequestPacket.builder().build().serialize()
        );

    }

    @OnMessage
    public void onMessage(String message, Session session) {
        LEDSuiteApplication.getLogger().info("Received message from session ID " + session.getId(), new LEDSuiteLogAreas.COMMUNICATION());

        // deserialize the incoming packet
        CommunicationPacket incomingPacket = LEDSuiteApplication.getPacketManager().deserialize(CommunicationPacket.class, message);

        // handle the incoming packet using the packet received handler
        LEDSuiteApplication.getPacketReceivedHandler().handleIncomingPacket(incomingPacket);
    }

    @OnClose
    public void onClose(Session session) {
        LEDSuiteApplication.getLogger().info("WebSocket connection closed with session ID: " + session.getId(), new LEDSuiteLogAreas.NETWORK());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LEDSuiteApplication.getLogger().error("Error in WebSocket session ID " + session.getId() + ": " + throwable.getMessage(), new LEDSuiteLogAreas.NETWORK());
    }
}
