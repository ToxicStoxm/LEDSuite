package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.AutoRegisterPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.Packet;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import lombok.*;

/**
 * <strong>Meaning:</strong><br>
 * Request for changing a setting in an animation menu.
 * Used to notify the server if the user changes a setting in some animation menu.
 * @since 1.0.0
 */
@AllArgsConstructor
@AutoRegisterPacket
@Builder
@Getter
@NoArgsConstructor
@Setter
public class MenuChangeRequestPacket extends CommunicationPacket {

    private String objectPath;
    private String objectValue;

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REQUEST;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Request.Types.MENU_CHANGE;
    }

    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        MenuChangeRequestPacket packet = MenuChangeRequestPacket.builder().build();
        YamlConfiguration yaml;
        try {
            yaml = loadYAML(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new DeserializationException(e);
        }

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.MenuChangeRequest.OBJECT_PATH, yaml);
        packet.objectPath = yaml.getString(Constants.Communication.YAML.Keys.Request.MenuChangeRequest.OBJECT_PATH);

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.MenuChangeRequest.OBJECT_VALUE, yaml);
        packet.objectValue = yaml.getString(Constants.Communication.YAML.Keys.Request.MenuChangeRequest.OBJECT_VALUE);

        return packet;
    }

    @Override
    public String serialize() {
        YamlConfiguration yaml = saveYAML();

        yaml.set(Constants.Communication.YAML.Keys.Request.MenuChangeRequest.OBJECT_PATH, objectPath);
        yaml.set(Constants.Communication.YAML.Keys.Request.MenuChangeRequest.OBJECT_VALUE, objectValue);

        return yaml.saveToString();
    }
}
