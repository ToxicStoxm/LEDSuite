package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.YAJL.Logger;
import lombok.*;

/**
 * Represents a request to delete an animation file from the server.
 * <p>
 * This packet is used to communicate with the server, instructing it to remove
 * the specified animation file from its storage. It ensures the proper handling
 * of animation deletion requests in the communication protocol.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *     <li>Supports automatic registration via the {@link AutoRegister} annotation.</li>
 *     <li>Implements the {@link CommunicationPacket} abstract class, inheriting general packet functionalities.</li>
 *     <li>Defines a specific subtype, {@code ANIMATION_DELETE}, to classify the packet.</li>
 * </ul>
 *
 * <h3>Serialization and Deserialization:</h3>
 * <p>
 * The class provides methods to convert the packet to and from YAML format:
 * </p>
 * <ul>
 *     <li><strong>serialize:</strong> Converts the packet into a YAML string.</li>
 *     <li><strong>deserialize:</strong> Populates the packet fields using data from a YAML string.</li>
 * </ul>
 *
 * <h3>Usage:</h3>
 * <p>
 * To create and send a deletion request:
 * </p>
 * <pre>
 *     AnimationDeleteRequestPacket packet = AnimationDeleteRequestPacket.builder()
 *             .fileName("animation_to_delete.yaml")
 *             .build();
 * </pre>
 *
 * @since 1.0.0
 */
@AllArgsConstructor
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@NoArgsConstructor
@Setter
public class AnimationDeleteRequestPacket extends CommunicationPacket {

    private static final Logger logger = Logger.autoConfigureLogger();

    @Override
    public Logger getLogger() {
        return logger;
    }

    /**
     * The name of the animation file to delete.
     * <p>
     * This field specifies the target animation file that the server should delete.
     * </p>
     */
    private String fileName;

    /**
     * Returns the type of this packet.
     * <p>
     * The type is categorized as a request packet in the communication protocol.
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
     * The subtype identifies this packet as an {@code ANIMATION_DELETE} request.
     * </p>
     *
     * @return the subtype string.
     */
    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Request.Types.ANIMATION_DELETE;
    }

    /**
     * Deserializes a YAML string into an {@code AnimationDeleteRequestPacket}.
     * <p>
     * This method populates the {@code fileName} field using the YAML key
     * {@code Constants.Communication.YAML.Keys.General.FILE_NAME}.
     * </p>
     *
     * @param yamlString the YAML string to deserialize.
     * @return a populated {@code AnimationDeleteRequestPacket}.
     * @throws DeserializationException if required, keys are missing or invalid.
     */
    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        super.deserialize(yamlString);
        AnimationDeleteRequestPacket packet = AnimationDeleteRequestPacket.builder().build();

        ensureKeyExists(Constants.Communication.YAML.Keys.General.FILE_NAME);
        fileName = yaml.getString(Constants.Communication.YAML.Keys.General.FILE_NAME);

        return packet;
    }

    /**
     * Serializes this {@code AnimationDeleteRequestPacket} into a YAML string.
     * <p>
     * Converts the {@code fileName} field into the corresponding YAML key-value pair.
     * </p>
     *
     * @return the serialized YAML string representation of this packet.
     */
    @Override
    public String serialize() {
        super.serialize();

        yaml.set(Constants.Communication.YAML.Keys.General.FILE_NAME, fileName);

        return yaml.saveToString();
    }
}
