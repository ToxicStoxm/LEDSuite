package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.upload_reply;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import lombok.*;

/**
 * Represents a reply to an upload request.
 * This packet contains information about whether the upload is permitted and the associated file name.
 *
 * @since 1.0.0
 */
@AllArgsConstructor
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@NoArgsConstructor
@Setter
public class UploadReplyPacket extends CommunicationPacket {

    /**
     * Indicates whether the upload is permitted.
     */
    private boolean uploadPermitted;

    /**
     * The name of the file associated with the upload request.
     */
    private String fileName;

    /**
     * Returns the type of this packet, which is a general reply type.
     *
     * @return the packet type as a string
     */
    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REPLY;
    }

    /**
     * Returns the subtype of this packet, which is specific to uploads.
     *
     * @return the packet subtype as a string
     */
    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Reply.Types.UPLOAD;
    }

    /**
     * Deserializes the given YAML string into an {@link UploadReplyPacket} object.
     *
     * @param yamlString the YAML string to deserialize
     * @return the deserialized {@link UploadReplyPacket}
     * @throws DeserializationException if any required key is missing or the YAML format is invalid
     */
    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        super.deserialize(yamlString);
        UploadReplyPacket packet = UploadReplyPacket.builder().build();

        ensureKeyExists(Constants.Communication.YAML.Keys.Reply.UploadReply.UPLOAD_PERMITTED);
        packet.uploadPermitted = yaml.getBoolean(Constants.Communication.YAML.Keys.Reply.UploadReply.UPLOAD_PERMITTED);

        ensureKeyExists(Constants.Communication.YAML.Keys.General.FILE_NAME);
        packet.fileName = yaml.getString(Constants.Communication.YAML.Keys.General.FILE_NAME);

        return packet;
    }

    /**
     * Serializes the {@link UploadReplyPacket} to a YAML string.
     *
     * @return the serialized YAML string
     */
    @Override
    public String serialize() {
        super.serialize();

        yaml.set(Constants.Communication.YAML.Keys.Reply.UploadReply.UPLOAD_PERMITTED, uploadPermitted);
        yaml.set(Constants.Communication.YAML.Keys.General.FILE_NAME, fileName);

        return yaml.saveToString();
    }

    /**
     * Handles the upload reply by invoking the upload manager to process the result of the upload request.
     * This informs the application whether the upload is permitted and passes the associated file name.
     */
    @Override
    public void handlePacket() {
        LEDSuiteApplication.getUploadManager().call(fileName, uploadPermitted);
    }
}
