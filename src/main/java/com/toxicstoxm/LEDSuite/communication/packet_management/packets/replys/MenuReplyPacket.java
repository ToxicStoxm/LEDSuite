package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.AutoRegisterPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.MenuRequestPacket;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import lombok.*;

/**
 * <strong>Meaning:</strong><br>
 * Structure and contents of an animation menu.
 * @since 1.0.0
 * @see MenuRequestPacket
 */
@AllArgsConstructor
@AutoRegisterPacket
@Builder
@Getter
@NoArgsConstructor
@Setter
public class MenuReplyPacket extends CommunicationPacket {

    private String menuYAML;

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REPLY;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Reply.Types.MENU;
    }

    @Override
    public String serialize() {
        YamlConfiguration yaml = saveYAML();
        yaml.set(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT, menuYAML);
        return yaml.saveToString();
    }

    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        MenuReplyPacket packet = MenuReplyPacket.builder().build();
        YamlConfiguration yaml;
        try {
            yaml = loadYAML(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new DeserializationException(e);
        }

        ensureKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT, yaml);
        packet.menuYAML = yaml.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT);
        return packet;
    }

    @Override
    public void handlePacket() {

    }
}
