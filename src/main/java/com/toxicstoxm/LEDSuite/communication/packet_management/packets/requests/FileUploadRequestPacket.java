package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.ErrorCode;
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
    private String uploadSessionId;
    private String sha256;

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
            throw new DeserializationException(e, ErrorCode.FailedToParseYAML);
        }

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.General.FILE_NAME, yaml);
        packet.requestFile = yaml.getString(Constants.Communication.YAML.Keys.Request.General.FILE_NAME);

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.FileUploadRequest.UPLOAD_SESSION_ID, yaml);
        packet.uploadSessionId = yaml.getString(Constants.Communication.YAML.Keys.Request.FileUploadRequest.UPLOAD_SESSION_ID);

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.FileUploadRequest.SHA256, yaml);
        packet.sha256 = yaml.getString(Constants.Communication.YAML.Keys.Request.FileUploadRequest.SHA256);

        return packet;
    }

    @Override
    public String serialize() {
        YamlConfiguration yaml = saveYAML();

        yaml.set(Constants.Communication.YAML.Keys.Request.General.FILE_NAME, requestFile);
        yaml.set(Constants.Communication.YAML.Keys.Request.FileUploadRequest.UPLOAD_SESSION_ID, uploadSessionId);
        yaml.set(Constants.Communication.YAML.Keys.Request.FileUploadRequest.SHA256, sha256);

        return yaml.saveToString();
    }
}
