package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests;


import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.status_reply.StatusReplyPacket;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * <strong>Meaning:</strong><br>
 * Request for getting the current server status.
 * @since 1.0.0
 * @see StatusReplyPacket
 */
@AutoRegister(module = AutoRegisterModules.PACKETS)
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
    public Packet deserialize(String yamlString) throws DeserializationException {
        return StatusRequestPacket.builder().build();
    }
}
