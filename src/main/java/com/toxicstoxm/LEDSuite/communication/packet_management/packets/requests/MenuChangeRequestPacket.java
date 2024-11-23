package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import lombok.*;

/**
 * Represents a request to change a setting in an animation menu.
 * <p>
 * This packet notifies the server when a user changes a specific menu setting,
 * such as toggling options or updating values in the animation menu interface.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *     <li>Identifies the setting being changed using {@code objectId}.</li>
 *     <li>Provides the new value of the setting through {@code objectValue}.</li>
 *     <li>Associates the change with a specific animation file using {@code fileName}.</li>
 * </ul>
 *
 * <h3>Serialization and Deserialization:</h3>
 * <ul>
 *     <li><strong>serialize:</strong> Converts this packet to a YAML string.</li>
 *     <li><strong>deserialize:</strong> Populates the packet fields from a YAML string.</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * MenuChangeRequestPacket packet = MenuChangeRequestPacket.builder()
 *         .objectId("setting_brightness")
 *         .objectValue("0.8")
 *         .fileName("example_animation.led")
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
public class MenuChangeRequestPacket extends CommunicationPacket {

    /**
     * The identifier of the menu object whose setting is being changed.
     * <p>
     * For example, it could represent a slider or toggle in the menu.
     * </p>
     */
    private String objectId;

    /**
     * The new value of the menu object.
     * <p>
     * The format and type depend on the specific menu object, e.g., a numerical value for sliders
     * or a string for text fields.
     * </p>
     */
    private String objectValue;

    /**
     * The name of the animation file associated with this menu change.
     * <p>
     * This allows the server to know which animation's settings are being modified.
     * </p>
     */
    private String fileName;

    /**
     * Returns the type of this packet.
     * <p>
     * Identifies this packet as a request in the communication protocol.
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
     * Specifies that this request is a {@code MENU_CHANGE}.
     * </p>
     *
     * @return the subtype string.
     */
    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Request.Types.MENU_CHANGE;
    }

    /**
     * Deserializes a YAML string into a {@code MenuChangeRequestPacket}.
     * <p>
     * Populates the fields {@code objectId}, {@code objectValue}, and {@code fileName}
     * based on the corresponding YAML keys.
     * </p>
     *
     * @param yamlString the YAML string to deserialize.
     * @return a populated {@code MenuChangeRequestPacket}.
     * @throws DeserializationException if required, keys are missing or invalid.
     */
    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        super.deserialize(yamlString);
        MenuChangeRequestPacket packet = MenuChangeRequestPacket.builder().build();

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.MenuChangeRequest.OBJECT_ID);
        packet.objectId = yaml.getString(Constants.Communication.YAML.Keys.Request.MenuChangeRequest.OBJECT_ID);

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.MenuChangeRequest.OBJECT_VALUE);
        packet.objectValue = yaml.getString(Constants.Communication.YAML.Keys.Request.MenuChangeRequest.OBJECT_VALUE);

        ensureKeyExists(Constants.Communication.YAML.Keys.General.FILE_NAME);
        packet.fileName = yaml.getString(Constants.Communication.YAML.Keys.General.FILE_NAME);

        return packet;
    }

    /**
     * Serializes this {@code MenuChangeRequestPacket} into a YAML string.
     * <p>
     * Converts the fields {@code objectId}, {@code objectValue}, and {@code fileName} into corresponding YAML key-value pairs.
     * </p>
     *
     * @return the serialized YAML string representation of this packet.
     */
    @Override
    public String serialize() {
        super.serialize();

        yaml.set(Constants.Communication.YAML.Keys.Request.MenuChangeRequest.OBJECT_ID, objectId);
        yaml.set(Constants.Communication.YAML.Keys.Request.MenuChangeRequest.OBJECT_VALUE, objectValue);
        yaml.set(Constants.Communication.YAML.Keys.General.FILE_NAME, fileName);

        return yaml.saveToString();
    }
}
