package com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.PacketManager;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ErrorPacket extends CommunicationPacket {

   private String source;        // guaranteed
   private int code;             // guaranteed
   private String name;          // guaranteed
   private int severity;         // guaranteed

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.PacketTypes.ERROR;
    }

    @Override
    public String getSubType() {
        return "";
    }

    @Override
    public Packet deserialize(String yamlString) throws PacketManager.DeserializationException {
        YamlConfiguration yaml;
        try {
            yaml = loadYAML(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new PacketManager.DeserializationException(e);
        }

        ensureKeyExists(Constants.Communication.YAML.Keys.Error.NAME, yaml);
        name = yaml.getString(Constants.Communication.YAML.Keys.Error.NAME);

        ensureKeyExists(Constants.Communication.YAML.Keys.Error.CODE, yaml);
        code = yaml.getInt(Constants.Communication.YAML.Keys.Error.CODE);

        ensureKeyExists(Constants.Communication.YAML.Keys.Error.SEVERITY, yaml);
        severity = yaml.getInt(Constants.Communication.YAML.Keys.Error.SEVERITY);

        ensureKeyExists(Constants.Communication.YAML.Keys.Error.SOURCE, yaml);
        source = yaml.getString(Constants.Communication.YAML.Keys.Error.SOURCE);

        return this;
    }

    @Override
    public String serialize() {
        YamlConfiguration yaml = saveYAML();

        yaml.set(Constants.Communication.YAML.Keys.Error.NAME, name);
        yaml.set(Constants.Communication.YAML.Keys.Error.CODE, code);
        yaml.set(Constants.Communication.YAML.Keys.Error.SEVERITY, severity);
        yaml.set(Constants.Communication.YAML.Keys.Error.SOURCE, source);

        return yaml.saveToString();
    }

    @Override
    public void handlePacket() {
        LEDSuiteApplication.getLogger().info(toString(), new LEDSuiteLogAreas.COMMUNICATION());
    }
}
