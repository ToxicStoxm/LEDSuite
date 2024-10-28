package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.PacketManager;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
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
    public Packet deserialize(String yamlString) throws PacketManager.DeserializationException {
        MenuReplyPacket packet = MenuReplyPacket.builder().build();
        YamlConfiguration yaml;
        try {
            yaml = loadYAML(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new PacketManager.DeserializationException(e);
        }

        ensureKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT, yaml);
        packet.menuYAML = yaml.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT);
        return packet;
    }

    @Override
    public void handlePacket() {

    }
}
