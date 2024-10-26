package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.PacketManager;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class MenuChangeRequestPacket extends CommunicationPacket {

    private String objectPath;
    private String objectValue;

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.PacketTypes.REQUEST;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.RequestTypes.MENU_CHANGE;
    }

    @Override
    public Packet deserialize(String yamlString) throws PacketManager.DeserializationException {
        YamlConfiguration yaml;
        try {
            yaml = loadYAML(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new PacketManager.DeserializationException(e);
        }

        ensureKeyExists(Constants.Communication.YAML.Keys.MenuChangeRequest.OBJECT_PATH, yaml);
        objectPath = yaml.getString(Constants.Communication.YAML.Keys.MenuChangeRequest.OBJECT_PATH);

        ensureKeyExists(Constants.Communication.YAML.Keys.MenuChangeRequest.OBJECT_VALUE, yaml);
        objectValue = yaml.getString(Constants.Communication.YAML.Keys.MenuChangeRequest.OBJECT_VALUE);

        return this;
    }

    @Override
    public String serialize() {
        YamlConfiguration yaml = saveYAML();

        yaml.set(Constants.Communication.YAML.Keys.MenuChangeRequest.OBJECT_PATH, objectPath);
        yaml.set(Constants.Communication.YAML.Keys.MenuChangeRequest.OBJECT_VALUE, objectValue);

        return yaml.saveToString();
    }
}
