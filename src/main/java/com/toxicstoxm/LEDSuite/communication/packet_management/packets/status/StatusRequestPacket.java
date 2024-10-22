package com.toxicstoxm.LEDSuite.communication.packet_management.packets.status;


import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.PacketManager;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StatusRequestPacket extends CommunicationPacket {

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.PacketTypes.STATUS;
    }

    @Override
    public String getSubType() {
        return "";
    }

    @Override
    public Packet deserialize(String yamlString) throws PacketManager.DeserializationException {
        return this;
    }
}
