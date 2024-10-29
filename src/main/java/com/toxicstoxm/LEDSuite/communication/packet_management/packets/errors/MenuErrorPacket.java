package com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.AutoRegisterPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.PacketManager;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import lombok.*;

/**
 * <strong>Meaning:</strong><br>
 * Information about a client side error related to animation menus.
 * @since 1.0.0
 */
@AllArgsConstructor
@AutoRegisterPacket
@Builder
@Getter
@NoArgsConstructor
@Setter
public class MenuErrorPacket extends CommunicationPacket {

    private String fileName;    // guaranteed
    private String message;     // guaranteed

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.ERROR;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Error.Types.MENU;
    }

    @Override
    public Packet deserialize(String yamlString) throws PacketManager.DeserializationException {
        MenuErrorPacket packet = MenuErrorPacket.builder().build();
        YamlConfiguration yaml;
        try {
            yaml = loadYAML(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new PacketManager.DeserializationException(e);
        }

        ensureKeyExists(Constants.Communication.YAML.Keys.Error.MenuError.FILE_NAME, yaml);
        fileName = yaml.getString(Constants.Communication.YAML.Keys.Error.MenuError.FILE_NAME);

        ensureKeyExists(Constants.Communication.YAML.Keys.Error.MenuError.MESSAGE, yaml);
        message = yaml.getString(Constants.Communication.YAML.Keys.Error.MenuError.MESSAGE);

        return packet;
    }

    @Override
    public String serialize() {
        YamlConfiguration yaml = saveYAML();

        if (fileName != null) {
            yaml.set(Constants.Communication.YAML.Keys.Error.MenuError.FILE_NAME, fileName);
        }

        if (message != null) {
            yaml.set(Constants.Communication.YAML.Keys.Error.MenuError.MESSAGE, message);
        }

        return yaml.saveToString();
    }
}
