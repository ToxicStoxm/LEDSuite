package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.status_reply.StatusReplyPacket;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a request to retrieve the current status of the server.
 * <p>
 * This packet is used to request information about the server's operational state.
 * The server will typically respond with a {@link StatusReplyPacket} containing details about its current status.
 * </p>
 *
 * @since 1.0.0
 * @see StatusReplyPacket The packet the server will respond with, containing the status information.
 */
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@Setter
public class StatusRequestPacket extends CommunicationPacket {

    /**
     * Gets the type of the packet. The type is used to classify the packet as a request.
     *
     * @return the packet type (e.g., "REQUEST").
     */
    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REQUEST;
    }

    /**
     * Gets the subtype of the packet. The subtype specifies the specific request type—this one is for status requests.
     *
     * @return the packet subtype (e.g., "STATUS").
     */
    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Request.Types.STATUS;
    }

    /**
     * Deserializes the YAML string into a {@link StatusRequestPacket} instance.
     * This packet doesn't contain additional fields, so the YAML string is ignored during deserialization.
     *
     * @param yamlString the YAML string representing the packet data (not used in this case).
     * @return a new instance of {@code StatusRequestPacket}.
     * @throws DeserializationException if an error occurs during deserialization (though it isn't relevant for this packet).
     */
    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        return StatusRequestPacket.builder().build();
    }
}
