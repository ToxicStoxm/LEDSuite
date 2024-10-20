package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests;


import com.toxicstoxm.LEDSuite.communication.packet_management.CommunicationPacket;
import lombok.Builder;

@Builder
public class StatusRequestPacket extends CommunicationPacket {

    @Override
    public String getType() {
        return "request";
    }

    @Override
    public String getSubType() {
        return "status";
    }
}
