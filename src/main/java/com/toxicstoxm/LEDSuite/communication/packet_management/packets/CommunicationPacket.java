package com.toxicstoxm.LEDSuite.communication.packet_management.packets;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.formatting.StringFormatter;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.tools.YamlTools;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.ConfigurationSection;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Default implementation for a {@link Packet}
 * @since 1.0.0
 */
@NoArgsConstructor
public abstract class CommunicationPacket implements Packet {

    @Override
    public String serialize() {
        return saveYAML().saveToString();
    }

    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        LEDSuiteApplication.getLogger().warn("Deserialization implementation missing for:\n" + yamlString, new LEDSuiteLogAreas.YAML());
        return null;
    }

    protected YamlConfiguration loadYAML(String yamlString) throws InvalidConfigurationException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString(yamlString);
        return yaml;
    }

    protected YamlConfiguration saveYAML() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set(Constants.Communication.YAML.Keys.General.PACKET_TYPE, getType());
        if (getSubType() != null && !getSubType().isBlank()) yaml.set(Constants.Communication.YAML.Keys.General.SUB_TYPE, getSubType());
        return yaml;
    }

    @Override
    public String toString() {
        return StringFormatter.getClassName(getClass()) + "(Type = " + getIdentifier() + ")" + " --> " + "\n[\n" + serialize() + "]";
    }

    protected boolean checkIfKeyExists(String key, @NotNull ConfigurationSection yaml) {
        return YamlTools.checkIfKeyExists(key, yaml);
    }

    protected void ensureKeyExists(String key, @NotNull ConfigurationSection yaml) throws DeserializationException {
        YamlTools.ensureKeyExists(key, yaml);
    }
}
