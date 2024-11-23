package com.toxicstoxm.LEDSuite.communication.packet_management;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import org.jetbrains.annotations.NotNull;

/**
 * {@code PacketReceivedHandler} is responsible for handling incoming packets by invoking the {@link Packet#handlePacket()} method.
 * This class is intended to provide a default handling process for packets upon their receipt.
 * The actual behavior is determined by the implementation of the {@code handlePacket()} method in the packet class itself.
 *
 * <p>The main role of this class is to provide a central point for packet processing, where the actual handling logic
 * resides in the respective packet types.</p>
 *
 * <strong>Purpose:</strong><br>
 * This class receives a {@link CommunicationPacket} and delegates the processing task to the specific packet's handling method.
 * It logs the event of receiving and handling a packet, and calls the relevant handling code for the packet's type.
 * <p>
 * <strong>Method:</strong><br>
 * - {@link #handleIncomingPacket(CommunicationPacket)}: Handles the packet by logging the event and invoking its
 * {@code handlePacket()} method for further processing.
 *
 * @since 1.0.0
 * @see Packet#handlePacket()
 */
public class PacketReceivedHandler {

    /**
     * Handles an incoming communication packet by calling the {@link Packet#handlePacket()} method.
     * This method is responsible for processing the packet once it's received,
     * by invoking the specific packet's {@code handlePacket()} method.
     * Additionally, it logs the handling event for debugging purposes.
     *
     * <p>The packet's specific implementation of {@code handlePacket()} defines how the packet will be processed(e.g.,
     * whether it will trigger specific actions,
     * update UI elements, or communicate with other system components).</p>
     *
     * <strong>Note:</strong><br>
     * This method serves as a basic dispatcher for incoming packets and should be customized in the packet's
     * {@code handlePacket()} method for specific packet-related behavior.
     *
     * @param packet the {@link CommunicationPacket} that needs to be handled
     */
    public void handleIncomingPacket(@NotNull CommunicationPacket packet) {
        // Log the packet handling attempt for debugging
        LEDSuiteApplication.getLogger().debug("Handling communication packet " + packet.getIdentifier() + "!", new LEDSuiteLogAreas.COMMUNICATION());

        // Delegate the actual handling of the packet to the packet's handlePacket method
        packet.handlePacket();
    }
}
