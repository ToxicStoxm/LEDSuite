package com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.menu_error;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.ErrorCode;
import com.toxicstoxm.YAJL.Logger;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import lombok.*;

/**
 * Represents a client-side error related to animation menus in the communication packet.
 * This class is automatically registered as part of the communication packet system.
 *
 * @since 1.0.0
 */
@AllArgsConstructor
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@NoArgsConstructor
@Setter
public class MenuErrorPacket extends CommunicationPacket {

    private static final Logger logger = Logger.autoConfigureLogger();

    @Override
    public Logger getLogger() {
        return logger;
    }

    private String fileName;    // Optional field (may be null)
    private String message;     // Mandatory field (must always be provided)
    private Severity severity;  // Mandatory field (must always be provided)
    private ErrorCode code;     // Mandatory field (must always be provided)

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.ERROR;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Error.Types.MENU;
    }

    /**
     * Deserializes the provided YAML string to create a MenuErrorPacket instance.
     *
     * @param yamlString the YAML string to deserialize
     * @return a populated MenuErrorPacket instance
     * @throws DeserializationException if the YAML string is invalid or missing required fields
     */
    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        MenuErrorPacket packet = MenuErrorPacket.builder().build();
        YamlConfiguration yaml;

        try {
            yaml = loadYAML(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new DeserializationException("Failed to load YAML configuration", e);
        }

        // Optional: check for and load the file name if it exists
        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Error.MenuError.FILE_NAME, yaml)) {
            packet.fileName = yaml.getString(Constants.Communication.YAML.Keys.Error.MenuError.FILE_NAME);
        }

        // Mandatory fields: message, severity, and code must always be provided
        ensureKeyExists(Constants.Communication.YAML.Keys.Error.MenuError.MESSAGE, yaml);
        packet.message = yaml.getString(Constants.Communication.YAML.Keys.Error.MenuError.MESSAGE);

        ensureKeyExists(Constants.Communication.YAML.Keys.Error.MenuError.SEVERITY, yaml);
        packet.severity = Severity.fromValue(yaml.getInt(Constants.Communication.YAML.Keys.Error.MenuError.SEVERITY));

        ensureKeyExists(Constants.Communication.YAML.Keys.Error.MenuError.CODE, yaml);
        packet.code = ErrorCode.fromInt(yaml.getInt(Constants.Communication.YAML.Keys.Error.MenuError.CODE));

        return packet;
    }

    /**
     * Serializes the current MenuErrorPacket instance to a YAML string.
     *
     * @return a YAML representation of the MenuErrorPacket
     */
    @Override
    public String serialize() {
        YamlConfiguration yaml = saveYAML();

        // Set all fields in the YAML
        yaml.set(Constants.Communication.YAML.Keys.Error.MenuError.FILE_NAME, fileName);
        yaml.set(Constants.Communication.YAML.Keys.Error.MenuError.MESSAGE, message);

        if (severity != null) {
            yaml.set(Constants.Communication.YAML.Keys.Error.MenuError.SEVERITY, severity.getValue());
        }

        yaml.set(Constants.Communication.YAML.Keys.Error.MenuError.CODE, code.getCode());

        return yaml.saveToString();
    }
}
