package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.upload_reply;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.FileUploadRequestPacket;
import com.toxicstoxm.YAJL.Logger;
import lombok.*;

/**
 * Represents a reply packet indicating the successful upload of a file to the server.
 *
 * <p><strong>Usage:</strong><br>
 * This packet confirms that the file was successfully uploaded and is typically a response to {@link FileUploadRequestPacket}.
 * </p>
 *
 * @since 1.0.0
 * @see FileUploadRequestPacket
 */
@AllArgsConstructor
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@NoArgsConstructor
@Setter
public class UploadSuccessReplyPacket extends CommunicationPacket {

    private static final Logger logger = Logger.autoConfigureLogger();

    @Override
    public Logger getLogger() {
        return logger;
    }

    /**
     * The name of the successfully uploaded file.
     */
    private String fileName;

    /**
     * Retrieves the type of this packet, identifying it as a reply.
     *
     * @return the packet type as a string
     */
    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REPLY;
    }

    /**
     * Retrieves the specific subtype of this packet, identifying it as an upload success reply.
     *
     * @return the packet subtype as a string
     */
    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Reply.Types.UPLOAD_SUCCESS;
    }

    /**
     * Deserializes the given YAML string into an {@link UploadSuccessReplyPacket} object.
     *
     * @param yamlString the YAML string to deserialize
     * @return the deserialized {@link UploadSuccessReplyPacket}
     * @throws DeserializationException if any required keys are missing or invalid
     */
    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        super.deserialize(yamlString);

        ensureKeyExists(Constants.Communication.YAML.Keys.General.FILE_NAME);
        fileName = yaml.getString(Constants.Communication.YAML.Keys.General.FILE_NAME);

        return this;
    }

    /**
     * Serializes the {@link UploadSuccessReplyPacket} to a YAML string.
     *
     * @return the serialized YAML string
     */
    @Override
    public String serialize() {
        super.serialize();

        yaml.set(Constants.Communication.YAML.Keys.General.FILE_NAME, fileName);

        return yaml.saveToString();
    }

    /**
     * Handles the packet by logging the success message and updating the application's state accordingly.
     */
    @Override
    public void handlePacket() {
        logger.info("File upload completed successfully. Server confirmation received");
        logger.info(" > Filename: {}", fileName);
    }
}
