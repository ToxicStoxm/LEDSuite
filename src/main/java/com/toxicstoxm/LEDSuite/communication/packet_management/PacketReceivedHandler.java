package com.toxicstoxm.LEDSuite.communication.packet_management;

import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import org.jetbrains.annotations.NotNull;

public class PacketReceivedHandler {

    public void handleIncomingPacket(@NotNull CommunicationPacket packet) {
        LEDSuiteApplication.getLogger().info("Handling communication packet " + packet.getIdentifier() + "!", new LEDSuiteLogAreas.COMMUNICATION());
        packet.handlePacket();
    }
}