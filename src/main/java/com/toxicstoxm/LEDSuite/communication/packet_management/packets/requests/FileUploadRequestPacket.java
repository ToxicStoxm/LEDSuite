package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.upload_reply.UploadReplyPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.upload_reply.UploadSuccessReplyPacket;
import lombok.*;

/**
 * <strong>Meaning:</strong><br>
 * Request for uploading a file (new animation) with the specified name.
 * @since 1.0.0
 * @see UploadSuccessReplyPacket
 * @see UploadReplyPacket
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

    @Builder.Default
    private boolean forceOverwrite = false;

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
        super.deserialize(yamlString);
        FileUploadRequestPacket packet = FileUploadRequestPacket.builder().build();

        ensureKeyExists(Constants.Communication.YAML.Keys.General.FILE_NAME);
        packet.requestFile = yaml.getString(Constants.Communication.YAML.Keys.General.FILE_NAME);

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.FileUploadRequest.UPLOAD_SESSION_ID);
        packet.uploadSessionId = yaml.getString(Constants.Communication.YAML.Keys.Request.FileUploadRequest.UPLOAD_SESSION_ID);

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.FileUploadRequest.SHA256);
        packet.sha256 = yaml.getString(Constants.Communication.YAML.Keys.Request.FileUploadRequest.SHA256);

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.FileUploadRequest.FORCE_OVERWRITE);
        packet.forceOverwrite = yaml.getBoolean(Constants.Communication.YAML.Keys.Request.FileUploadRequest.FORCE_OVERWRITE);

        return packet;
    }

    @Override
    public String serialize() {
        super.serialize();

        yaml.set(Constants.Communication.YAML.Keys.General.FILE_NAME, requestFile);
        yaml.set(Constants.Communication.YAML.Keys.Request.FileUploadRequest.UPLOAD_SESSION_ID, uploadSessionId);
        yaml.set(Constants.Communication.YAML.Keys.Request.FileUploadRequest.SHA256, sha256);
        yaml.set(Constants.Communication.YAML.Keys.Request.FileUploadRequest.FORCE_OVERWRITE, forceOverwrite);

        return yaml.saveToString();
    }
}
