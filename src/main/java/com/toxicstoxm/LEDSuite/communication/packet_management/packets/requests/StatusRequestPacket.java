package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests;


import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.AutoRegisterPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.PacketManager;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.StatusReplyPacket;
import lombok.*;

/**
 * <strong>Meaning:</strong><br>
 * Request for getting the current server status.
 * @since 1.0.0
 * @see StatusReplyPacket
 */
@AutoRegisterPacket
@Builder
@Getter
@Setter
public class StatusRequestPacket extends CommunicationPacket {

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REQUEST;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Request.Types.STATUS;
    }

    @Override
    public Packet deserialize(String yamlString) throws PacketManager.DeserializationException {
        return StatusRequestPacket.builder().build();
    }
}
