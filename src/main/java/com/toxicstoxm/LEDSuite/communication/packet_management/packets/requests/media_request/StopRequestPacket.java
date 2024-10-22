package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.media_request;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.PacketManager;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StopRequestPacket extends CommunicationPacket {

    private String requestFile;

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.PacketTypes.REQUEST;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.RequestTypes.STOP;

    }

    @Override
    public Packet deserialize(String yamlString) throws PacketManager.DeserializationException {
        YamlConfiguration yaml;
        try {
            yaml = loadYAML(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new PacketManager.DeserializationException(e);
        }

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.FILE, yaml);
        requestFile = yaml.getString(Constants.Communication.YAML.Keys.Request.FILE);

        return this;
    }

    @Override
    public String serialize() {
        YamlConfiguration yaml = saveYAML();

        yaml.set(Constants.Communication.YAML.Keys.Request.FILE, requestFile);

        return yaml.saveToString();
    }
}
