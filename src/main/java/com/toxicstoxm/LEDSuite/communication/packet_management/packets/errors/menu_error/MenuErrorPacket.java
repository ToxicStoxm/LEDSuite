package com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.menu_error;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.ErrorCode;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import lombok.*;

/**
 * <strong>Meaning:</strong><br>
 * Information about a client side error related to animation menus.
 * @since 1.0.0
 */
@AllArgsConstructor
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@NoArgsConstructor
@Setter
public class MenuErrorPacket extends CommunicationPacket {

    private String fileName;    // not guaranteed
    private String message;     // guaranteed
    private Severity severity;  // guaranteed
    private ErrorCode code;     // guaranteed

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.ERROR;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Error.Types.MENU;
    }

    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        MenuErrorPacket packet = MenuErrorPacket.builder().build();
        YamlConfiguration yaml;
        try {
            yaml = loadYAML(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new DeserializationException(e);
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Error.MenuError.FILE_NAME, yaml)) {
            packet.fileName = yaml.getString(Constants.Communication.YAML.Keys.Error.MenuError.FILE_NAME);
        }

        ensureKeyExists(Constants.Communication.YAML.Keys.Error.MenuError.MESSAGE, yaml);
        packet.message = yaml.getString(Constants.Communication.YAML.Keys.Error.MenuError.MESSAGE);

        ensureKeyExists(Constants.Communication.YAML.Keys.Error.MenuError.SEVERITY, yaml);
        packet.severity = Severity.fromValue(yaml.getInt(Constants.Communication.YAML.Keys.Error.MenuError.SEVERITY));

        ensureKeyExists(Constants.Communication.YAML.Keys.Error.MenuError.CODE, yaml);
        packet.code = ErrorCode.fromInt(yaml.getInt(Constants.Communication.YAML.Keys.Error.MenuError.CODE));

        return packet;
    }

    @Override
    public String serialize() {
        YamlConfiguration yaml = saveYAML();

        yaml.set(Constants.Communication.YAML.Keys.Error.MenuError.FILE_NAME, fileName);
        yaml.set(Constants.Communication.YAML.Keys.Error.MenuError.MESSAGE, message);
        if (severity != null) yaml.set(Constants.Communication.YAML.Keys.Error.MenuError.SEVERITY, severity.value);
        yaml.set(Constants.Communication.YAML.Keys.Error.MenuError.CODE, code.getCode());

        return yaml.saveToString();
    }
}
