package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.MenuReplyPacket;
import com.toxicstoxm.YAJL.Logger;
import lombok.*;

/**
 * Represents a request to retrieve the menu for a specific animation file.
 * <p>
 * This packet is used to request the server to provide the menu associated with
 * a specified animation, identified by {@code requestFile}.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *     <li>Specifies the animation file whose menu is being requested.</li>
 *     <li>Used in conjunction with a {@link MenuReplyPacket} to retrieve the menu data.</li>
 * </ul>
 *
 * <h3>Serialization and Deserialization:</h3>
 * <ul>
 *     <li><strong>serialize:</strong> Converts this packet into a YAML string.</li>
 *     <li><strong>deserialize:</strong> Populates this packet from a YAML string.</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * MenuRequestPacket packet = MenuRequestPacket.builder()
 *         .requestFile("example_animation.led")
 *         .build();
 * </pre>
 *
 * @since 1.0.0
 * @see MenuReplyPacket
 */
@AllArgsConstructor
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@NoArgsConstructor
@Setter
public class MenuRequestPacket extends CommunicationPacket {

    private static final Logger logger = Logger.autoConfigureLogger();

    @Override
    public Logger getLogger() {
        return logger;
    }

    /**
     * The name of the animation file for which the menu is being requested.
     * <p>
     * This file name helps the server identify which animation's menu to retrieve.
     * </p>
     */
    private String requestFile;

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
     * This method identifies the specific purpose of the packet as a {@code MENU} request.
     * </p>
     *
     * @return the packet subtype string.
     */
    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Request.Types.MENU;
    }

    /**
     * Deserializes a YAML string into a {@code MenuRequestPacket}.
     * <p>
     * Populates the {@code requestFile} field based on the corresponding YAML key.
     * </p>
     *
     * @param yamlString the YAML string to deserialize.
     * @return a populated {@code MenuRequestPacket}.
     * @throws DeserializationException if the required key is missing or invalid.
     */
    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        super.deserialize(yamlString);
        MenuRequestPacket packet = MenuRequestPacket.builder().build();

        ensureKeyExists(Constants.Communication.YAML.Keys.General.FILE_NAME);
        packet.requestFile = yaml.getString(Constants.Communication.YAML.Keys.General.FILE_NAME);

        return packet;
    }

    /**
     * Serializes this {@code MenuRequestPacket} into a YAML string.
     * <p>
     * Converts the {@code requestFile} field into a YAML key-value pair.
     * </p>
     *
     * @return the serialized YAML string representation of this packet.
     */
    @Override
    public String serialize() {
        super.serialize();

        yaml.set(Constants.Communication.YAML.Keys.General.FILE_NAME, requestFile);

        return yaml.saveToString();
    }
}
