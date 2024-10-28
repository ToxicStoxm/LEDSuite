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
public class UploadSuccessReplyPacket extends CommunicationPacket {

    private String fileName;

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REPLY;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Reply.Types.UPLOAD_SUCCESS;
    }

    @Override
    public Packet deserialize(String yamlString) throws PacketManager.DeserializationException {
        UploadSuccessReplyPacket packet = UploadSuccessReplyPacket.builder().build();
        YamlConfiguration yaml;
        try {
            yaml = loadYAML(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new PacketManager.DeserializationException(e);
        }

        ensureKeyExists(Constants.Communication.YAML.Keys.Reply.UploadSuccessReply.FILE_NAME, yaml);
        packet.fileName = yaml.getString(Constants.Communication.YAML.Keys.Reply.UploadSuccessReply.FILE_NAME);

        return packet;
    }

    @Override
    public String serialize() {
        YamlConfiguration yaml = saveYAML();

        yaml.set(Constants.Communication.YAML.Keys.Reply.UploadSuccessReply.FILE_NAME, fileName);

        return yaml.saveToString();
    }
}
