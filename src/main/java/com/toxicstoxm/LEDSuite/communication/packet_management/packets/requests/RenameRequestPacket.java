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
 * Represents a request to rename a file.
 * <p>
 * This packet is used to request the renaming of a specific file, identified by {@code requestFile},
 * to a new name specified in {@code newName}.
 * </p>
 *
 * <h3>Serialization and Deserialization:</h3>
 * <ul>
 *     <li><strong>serialize:</strong> Converts this packet into a YAML string.</li>
 *     <li><strong>deserialize:</strong> Populates this packet from a YAML string.</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * RenameRequestPacket packet = RenameRequestPacket.builder()
 *         .requestFile("old_name.jar")
 *         .newName("new_name.jar")
 *         .build();
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
public class RenameRequestPacket extends CommunicationPacket {

    private static final Logger logger = Logger.autoConfigureLogger();

    @Override
    public Logger getLogger() {
        return logger;
    }

    /**
     * The name of the file to be renamed.
     */
    private String requestFile;

    /**
     * The new name for the specified file.
     */
    private String newName;

    /**
     * Returns the type of this packet.
     * <p>
     * This method identifies the packet type as a {@code REQUEST}.
     * </p>
     *
     * @return the packet type string.
     */
    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REQUEST;
    }

    /**
     * Returns the subtype of this packet.
     * <p>
     * This method identifies the specific purpose of the packet as a {@code RENAME_REQUEST}.
     * </p>
     *
     * @return the packet subtype string.
     */
    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Request.Types.RENAME_REQUEST;
    }

    /**
     * Deserializes a YAML string into a {@code RenameRequestPacket}.
     * <p>
     * Populates the {@code requestFile} and {@code newName} fields based on their respective YAML keys.
     * </p>
     *
     * @param yamlString the YAML string to deserialize.
     * @return a populated {@code RenameRequestPacket}.
     * @throws DeserializationException if the required keys are missing or invalid.
     */
    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        super.deserialize(yamlString);
        RenameRequestPacket packet = RenameRequestPacket.builder().build();

        ensureKeyExists(Constants.Communication.YAML.Keys.General.FILE_NAME);
        packet.requestFile = yaml.getString(Constants.Communication.YAML.Keys.General.FILE_NAME);

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.RenameRequest.NEW_NAME);
        packet.newName = yaml.getString(Constants.Communication.YAML.Keys.Request.RenameRequest.NEW_NAME);

        return packet;
    }

    /**
     * Serializes this {@code RenameRequestPacket} into a YAML string.
     * <p>
     * Converts the {@code requestFile} and {@code newName} fields into YAML key-value pairs.
     * </p>
     *
     * @return the serialized YAML string representation of this packet.
     */
    @Override
    public String serialize() {
        super.serialize();

        yaml.set(Constants.Communication.YAML.Keys.General.FILE_NAME, requestFile);
        yaml.set(Constants.Communication.YAML.Keys.Request.RenameRequest.NEW_NAME, newName);

        return yaml.saveToString();
    }
}
