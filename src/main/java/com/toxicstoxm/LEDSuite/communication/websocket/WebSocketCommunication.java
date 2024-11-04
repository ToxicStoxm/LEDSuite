package com.toxicstoxm.LEDSuite.communication.websocket;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.StatusRequestPacket;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import jakarta.websocket.*;
import org.jetbrains.annotations.NotNull;

/**
 * Communication endpoint used for exchanging general information with the server and receiving potential errors.
 * @since 1.0.0
 */
@ClientEndpoint
public class WebSocketCommunication extends WebSocketClientEndpoint {

    @Override
    boolean binaryOnly() {
        return false;
    }

    @OnOpen
    public void onOpen(@NotNull Session session) {
        LEDSuiteApplication.getLogger().info("WebSocket connection opened with session ID: " + session.getId(), new LEDSuiteLogAreas.NETWORK());

        LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                StatusRequestPacket.builder().build().serialize()
        );

        LEDSuiteApplication.getWindow().setServerConnected(true);
    }

    @OnMessage
    public void onMessage(String message, @NotNull Session session) {
        LEDSuiteApplication.getLogger().verbose("----------------------< IN >----------------------" + "\n[Session] " + session.getId(), new LEDSuiteLogAreas.COMMUNICATION());
        LEDSuiteApplication.getLogger().verbose(message, new LEDSuiteLogAreas.COMMUNICATION());
        LEDSuiteApplication.getLogger().verbose("--------------------------------------------------", new LEDSuiteLogAreas.COMMUNICATION());

        // deserialize the incoming packet
        CommunicationPacket incomingPacket = LEDSuiteApplication.getPacketManager().deserialize(message);

        // handle the incoming packet using the packet received handler
        LEDSuiteApplication.getPacketReceivedHandler().handleIncomingPacket(incomingPacket);
    }

    @OnClose
    public void onClose(@NotNull Session session) {
        LEDSuiteApplication.getLogger().info("WebSocket connection closed with session ID: " + session.getId(), new LEDSuiteLogAreas.NETWORK());

        var settingsDialog = LEDSuiteApplication.getWindow().getSettingsDialog();

        if (settingsDialog != null) {
            settingsDialog.connectivityManager().disconnected();
        }

        LEDSuiteApplication.getWindow().setServerConnected(false);
    }

    @OnError
    public void onError(@NotNull Session session, @NotNull Throwable throwable) {
        LEDSuiteApplication.getLogger().error("Error in WebSocket session ID " + session.getId() + ": " + throwable.getMessage(), new LEDSuiteLogAreas.NETWORK());
        for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
            LEDSuiteApplication.getLogger().stacktrace(String.valueOf(stackTraceElement), new LEDSuiteLogAreas.NETWORK());
        }
    }
}
