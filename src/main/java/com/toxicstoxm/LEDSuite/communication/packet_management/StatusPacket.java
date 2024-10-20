package com.toxicstoxm.LEDSuite.communication.packet_management;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class StatusPacket implements Packet {

    private String statusMessage;

    @Override
    public String getPacketType() {
        return "status_request";
    }

    @Override
    public Packet deserialize(String yamlString) {
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.loadFromString(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        this.statusMessage = yaml.getString("status-message");

        return this;
    }

    @Override
    public String serialize() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set(Constants.Communication.YAML.Keys.General.PacketType, getPacketType());
        yaml.set("status-message", statusMessage);
        return yaml.saveToString();
    }

    @Override
    public String toString() {
        return serialize();
    }
}
