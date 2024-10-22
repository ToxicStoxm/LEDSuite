package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.CommunicationPacket;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MenuReplyPacket extends CommunicationPacket {


    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.PacketTypes.REPLY;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.ReplyTypes.MENU;
    }
}
