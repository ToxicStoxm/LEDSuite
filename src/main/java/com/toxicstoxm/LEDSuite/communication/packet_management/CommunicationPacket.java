package com.toxicstoxm.LEDSuite.communication.packet_management;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.formatting.StringFormatter;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Default implementation for a {@link Packet}
 * @since 1.0.0
 */
public abstract class CommunicationPacket implements Packet {

    @Override
    public String serialize() {
        return saveYAML().saveToString();
    }

    @Override
    public Packet deserialize(String yamlString) throws PacketManager.DeserializationException {
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
        yaml.set(Constants.Communication.YAML.Keys.General.SUB_TYPE, getSubType());
        return yaml;
    }

    @Override
    public String toString() {
        return StringFormatter.getClassName(getClass()) + "(Type = " + getIdentifier() + ")" + " --> " + "\n[\n" + serialize() + "]";
    }

    protected <T> @Nullable T convert(@NotNull Class<T > clazz, CommunicationPacket packet) {
        if (clazz.isInstance(packet)) return clazz.cast(packet);
        return null;
    }
}
