package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.upload_reply;

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
public class UploadFileCollisionReplyPacket extends CommunicationPacket {

    private String currentName;

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REPLY;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Reply.Types.UPLOAD_FILE_COLLISION_REPLY;
    }

    @Override
    public Packet deserialize(String yamlString) throws PacketManager.DeserializationException {
        UploadFileCollisionReplyPacket packet = UploadFileCollisionReplyPacket.builder().build();
        YamlConfiguration yaml;
        try {
            yaml = loadYAML(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new PacketManager.DeserializationException(e);
        }

        ensureKeyExists(Constants.Communication.YAML.Keys.Reply.UploadFileCollisionReply.CURRENT_NAME, yaml);
        packet.currentName = yaml.getString(Constants.Communication.YAML.Keys.Reply.UploadFileCollisionReply.CURRENT_NAME);

        return packet;
    }

    @Override
    public String serialize() {
        YamlConfiguration yaml = saveYAML();

        yaml.set(Constants.Communication.YAML.Keys.Reply.UploadFileCollisionReply.CURRENT_NAME, currentName);

        return yaml.saveToString();
    }
}
