package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
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
@AutoRegister(module = AutoRegisterModules.PACKETS)
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
    public Packet deserialize(String yamlString) throws DeserializationException {
        FileUploadRequestPacket packet = FileUploadRequestPacket.builder().build();
        YamlConfiguration yaml;
        try {
            yaml = loadYAML(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new DeserializationException(e);
        }

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.General.FILE_NAME, yaml);
        packet.requestFile = yaml.getString(Constants.Communication.YAML.Keys.Request.General.FILE_NAME);

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.FileUploadRequest.PACKET_COUNT, yaml);
        packet.packetCount = yaml.getInt(Constants.Communication.YAML.Keys.Request.FileUploadRequest.PACKET_COUNT);

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.FileUploadRequest.UPLOAD_SESSION_ID, yaml);
        packet.uploadSessionId = yaml.getString(Constants.Communication.YAML.Keys.Request.FileUploadRequest.UPLOAD_SESSION_ID);

        return packet;
    }

    @Override
    public String serialize() {
        YamlConfiguration yaml = saveYAML();

        yaml.set(Constants.Communication.YAML.Keys.Request.General.FILE_NAME, requestFile);
        yaml.set(Constants.Communication.YAML.Keys.Request.FileUploadRequest.PACKET_COUNT, packetCount);
        yaml.set(Constants.Communication.YAML.Keys.Request.FileUploadRequest.UPLOAD_SESSION_ID, uploadSessionId);

        return yaml.saveToString();
    }
}
