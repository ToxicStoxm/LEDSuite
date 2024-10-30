package com.toxicstoxm.LEDSuite.communication.packet_management;

import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import org.jetbrains.annotations.NotNull;

/**
 * Handles received packets using their default handle method.
 * @since 1.0.0
 * @see Packet#handlePacket()
 */
public class PacketReceivedHandler {

    public void handleIncomingPacket(@NotNull CommunicationPacket packet) {
        LEDSuiteApplication.getLogger().debug("Handling communication packet " + packet.getIdentifier() + "!", new LEDSuiteLogAreas.COMMUNICATION());
        packet.handlePacket();
    }
}
