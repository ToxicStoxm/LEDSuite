package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@Setter
public class SettingsResetRequestPacket extends CommunicationPacket {
    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REQUEST;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Request.Types.SETTINGS_RESET;
    }

    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        return SettingsResetRequestPacket.builder().build();
    }
}
