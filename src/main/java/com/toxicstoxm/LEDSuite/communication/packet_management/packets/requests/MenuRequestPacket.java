package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.MenuReplyPacket;
import lombok.*;

/**
 * <strong>Meaning:</strong><br>
 * Request for getting the specified animations menu.
 * @since 1.0.0
 * @see MenuReplyPacket
 */
@AllArgsConstructor
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@NoArgsConstructor
@Setter
public class MenuRequestPacket extends CommunicationPacket {

    private String requestFile;

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REQUEST;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Request.Types.MENU;
    }

    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        super.deserialize(yamlString);
        MenuRequestPacket packet = MenuRequestPacket.builder().build();

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.General.FILE_NAME);
        packet.requestFile = yaml.getString(Constants.Communication.YAML.Keys.Request.General.FILE_NAME);

        return packet;
    }

    @Override
    public String serialize() {
        super.serialize();

        yaml.set(Constants.Communication.YAML.Keys.Request.General.FILE_NAME, requestFile);

        return yaml.saveToString();
    }

}
