package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.SettingsReplyPacket;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * <strong>Meaning:</strong><br>
 * Request for getting the servers current settings.
 * @since 1.0.0
 * @see SettingsReplyPacket
 */
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@Setter
public class SettingsRequestPacket extends CommunicationPacket {
    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REQUEST;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Request.Types.SETTINGS;
    }

    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        return SettingsRequestPacket.builder().build();
    }
}
