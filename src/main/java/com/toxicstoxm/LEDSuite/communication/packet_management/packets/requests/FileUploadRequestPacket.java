package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.upload_reply.UploadReplyPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.upload_reply.UploadSuccessReplyPacket;
import com.toxicstoxm.YAJL.Logger;
import lombok.*;

/**
 * Represents a request to upload a new animation file to the server.
 * <p>
 * This packet is used to initiate a file upload process by specifying the file name,
 * an upload session identifier, the file's SHA-256 hash, and an optional flag to force overwrite.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *     <li>Allows uploading new animations or replacing existing ones if {@code forceOverwrite} is set.</li>
 *     <li>Supports automatic registration with the {@link AutoRegister} annotation.</li>
 *     <li>Can be deserialized from and serialized into a YAML string.</li>
 * </ul>
 *
 * <h3>Serialization and Deserialization:</h3>
 * <ul>
 *     <li><strong>serialize:</strong> Converts this packet to a YAML string.</li>
 *     <li><strong>deserialize:</strong> Constructs the packet from a YAML string, ensuring all required fields are populated.</li>
 * </ul>
 *
 * <h3>Related Reply Packets:</h3>
 * <ul>
 *     <li>{@link UploadReplyPacket} - Base reply type for upload responses.</li>
 *     <li>{@link UploadSuccessReplyPacket} - Indicates successful file upload completion.</li>
 * </ul>
 *
 * <h3>Usage:</h3>
 * <pre>
 * FileUploadRequestPacket packet = FileUploadRequestPacket.builder()
 *         .requestFile("example_animation.led")
 *         .uploadSessionId("session123")
 *         .sha256("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
 *         .forceOverwrite(true)
 *         .build();
 * </pre>
 *
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

    private static final Logger logger = Logger.autoConfigureLogger();

    @Override
    public Logger getLogger() {
        return logger;
    }

    /**
     * The name of the file being uploaded.
     * <p>
     * This is the unique identifier for the animation file being transmitted.
     * </p>
     */
    private String requestFile;

    /**
     * The session ID associated with this upload.
     * <p>
     * The server may use this to track and manage the upload process.
     * </p>
     */
    private String uploadSessionId;

    /**
     * The SHA-256 hash of the file being uploaded.
     * <p>
     * Used for verifying file integrity during or after the upload process.
     * </p>
     */
    private String sha256;

    /**
     * Whether to overwrite an existing file with the same name.
     * <p>
     * If {@code true}, any existing file with the specified name will be replaced.
     * </p>
     */
    @Builder.Default
    private boolean forceOverwrite = false;

    /**
     * Returns the type of this packet.
     * <p>
     * This packet is categorized as a request in the communication protocol.
     * </p>
     *
     * @return the packet type, which is {@code REQUEST}.
     */
    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REQUEST;
    }

    /**
     * Returns the subtype of this packet.
     * <p>
     * Identifies this packet as a {@code FILE_UPLOAD} request.
     * </p>
     *
     * @return the subtype string.
     */
    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Request.Types.FILE_UPLOAD;
    }

    /**
     * Deserializes a YAML string into a {@code FileUploadRequestPacket}.
     * <p>
     * This method populates the {@code requestFile}, {@code uploadSessionId}, {@code sha256},
     * and {@code forceOverwrite} fields based on YAML keys.
     * </p>
     *
     * @param yamlString the YAML string to deserialize.
     * @return a populated {@code FileUploadRequestPacket}.
     * @throws DeserializationException if required, keys are missing or invalid.
     */
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

    /**
     * Serializes this {@code FileUploadRequestPacket} into a YAML string.
     * <p>
     * Converts the fields {@code requestFile}, {@code uploadSessionId}, {@code sha256},
     * and {@code forceOverwrite} into corresponding YAML key-value pairs.
     * </p>
     *
     * @return the serialized YAML string representation of this packet.
     */
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
