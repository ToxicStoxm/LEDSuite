package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.AutoRegisterPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.PacketManager;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.upload_reply.UploadFileCollisionReplyPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.upload_reply.UploadSuccessReplyPacket;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import lombok.*;

/**
 * <strong>Meaning:</strong><br>
 * Request for uploading a file (new animation) with the specified name.
 * @since 1.0.0
 * @see UploadSuccessReplyPacket
 * @see UploadFileCollisionReplyPacket
 */
@AllArgsConstructor
@AutoRegisterPacket
@Builder
@Getter
@NoArgsConstructor
@Setter
public class FileUploadRequestPacket extends CommunicationPacket {

    private String requestFile;
    private int packetCount;
    private String uploadSessionId;

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REQUEST;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Request.Types.FILE_UPLOAD;
    }

    @Override
    public Packet deserialize(String yamlString) throws PacketManager.DeserializationException {
        FileUploadRequestPacket packet = FileUploadRequestPacket.builder().build();
        YamlConfiguration yaml;
        try {
            yaml = loadYAML(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new PacketManager.DeserializationException(e);
        }

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.General.FILE, yaml);
        packet.requestFile = yaml.getString(Constants.Communication.YAML.Keys.Request.General.FILE);

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.FileUploadRequest.PACKET_COUNT, yaml);
        packet.packetCount = yaml.getInt(Constants.Communication.YAML.Keys.Request.FileUploadRequest.PACKET_COUNT);

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.FileUploadRequest.UPLOAD_SESSION_ID, yaml);
        packet.uploadSessionId = yaml.getString(Constants.Communication.YAML.Keys.Request.FileUploadRequest.UPLOAD_SESSION_ID);

        return packet;
    }

    @Override
    public String serialize() {
        YamlConfiguration yaml = saveYAML();

        yaml.set(Constants.Communication.YAML.Keys.Request.General.FILE, requestFile);
        yaml.set(Constants.Communication.YAML.Keys.Request.FileUploadRequest.PACKET_COUNT, packetCount);
        yaml.set(Constants.Communication.YAML.Keys.Request.FileUploadRequest.UPLOAD_SESSION_ID, uploadSessionId);

        return yaml.saveToString();
    }
}
