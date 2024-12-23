package com.toxicstoxm.LEDSuite.communication.packet_management.packets;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.ErrorCode;
import com.toxicstoxm.LEDSuite.formatting.StringFormatter;
import com.toxicstoxm.LEDSuite.tools.YamlTools;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.ConfigurationSection;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * <strong>Meaning:</strong><br>
 * Default implementation for a {@link Packet}.
 * This class provides methods for serializing and deserializing communication packets to and from YAML format.
 * It also ensures that the necessary keys are present in the YAML configuration during the deserialization process.
 *
 * @since 1.0.0
 */
@NoArgsConstructor
public abstract class CommunicationPacket implements Packet {

    protected YamlConfiguration yaml;

    /**
     * Serializes the packet to a YAML string.
     * <p>
     * This method calls the {@link #saveYAML()} method to generate a YamlConfiguration
     * and then converts it to a string representation.
     * </p>
     *
     * @return the YAML string representation of the packet.
     */
    @Override
    public String serialize() {
        yaml = saveYAML();
        return yaml.saveToString();
    }

    /**
     * Deserializes the packet from a YAML string.
     * <p>
     * This method attempts to load the YAML string into a YamlConfiguration.
     * If the YAML is invalid or cannot be loaded, a {@link DeserializationException} will be thrown.
     * </p>
     *
     * @param yamlString the YAML string to deserialize the packet from.
     * @return the deserialized packet.
     * @throws DeserializationException if the YAML is invalid or the packet cannot be deserialized.
     */
    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        try {
            yaml = loadYAML(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new DeserializationException("Failed to deserialize packet '" + StringFormatter.getClassName(getClass()) + "'!", ErrorCode.FailedToParseYAML);
        }
        return this;
    }

    /**
     * Loads the YAML configuration from a string.
     *
     * @param yamlString the YAML string to load.
     * @return the loaded YamlConfiguration.
     * @throws InvalidConfigurationException if the YAML string is invalid.
     */
    protected YamlConfiguration loadYAML(String yamlString) throws InvalidConfigurationException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString(yamlString);
        return yaml;
    }

    /**
     * Creates a base YamlConfiguration for the packet, including its type and subtype.
     *
     * @return a YamlConfiguration representing the packet.
     */
    protected YamlConfiguration saveYAML() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set(Constants.Communication.YAML.Keys.General.PACKET_TYPE, getType());
        if (getSubType() != null && !getSubType().isBlank()) {
            yaml.set(Constants.Communication.YAML.Keys.General.SUB_TYPE, getSubType());
        }
        return yaml;
    }

    /**
     * Returns a string representation of the packet, including its type and serialized content.
     * <p>
     * The output includes the class name, type, and serialized YAML content of the packet.
     * </p>
     *
     * @return a string representation of the packet.
     */
    @Override
    public String toString() {
        return StringFormatter.getClassName(getClass()) + "(Type = " + getIdentifier() + ")" + " --> " + "\n[\n" + serialize() + "]";
    }

    /**
     * Checks if the specified key exists in the current YAML configuration.
     *
     * @param key the key to check.
     * @return {@code true} if the key exists in the YAML configuration, {@code false} otherwise.
     */
    protected boolean checkIfKeyExists(String key) {
        return YamlTools.checkIfKeyExists(key, yaml);
    }

    /**
     * Checks if the specified key exists in the provided YAML configuration section.
     *
     * @param key the key to check.
     * @param yaml the YAML configuration section to check within.
     * @return {@code true} if the key exists in the provided YAML section, {@code false} otherwise.
     */
    protected boolean checkIfKeyExists(String key, @NotNull ConfigurationSection yaml) {
        return YamlTools.checkIfKeyExists(key, yaml);
    }

    /**
     * Ensures that the specified key exists in the current YAML configuration.
     *
     * @param key the key to check.
     * @throws DeserializationException if the key is not present in the YAML.
     */
    protected void ensureKeyExists(String key) throws DeserializationException {
        YamlTools.ensureKeyExists(key, yaml);
    }

    /**
     * Ensures that the specified key exists in the provided YAML configuration section.
     *
     * @param key the key to check.
     * @param yaml the YAML configuration section to check within.
     * @throws DeserializationException if the key is not present in the provided YAML section.
     */
    protected void ensureKeyExists(String key, @NotNull ConfigurationSection yaml) throws DeserializationException {
        YamlTools.ensureKeyExists(key, yaml);
    }

    protected String getStringIfAvailable(String key) {
        return YamlTools.getStringIfAvailable(key, yaml);
    }

    protected String getStringIfAvailable(String key, String defaultValue) {
        return YamlTools.getStringIfAvailable(key, defaultValue, yaml);
    }

    protected boolean getBooleanIfAvailable(String key) {
        return YamlTools.getBooleanIfAvailable(key, yaml);
    }

    protected boolean getBooleanIfAvailable(String key, boolean defaultValue) {
        return YamlTools.getBooleanIfAvailable(key, defaultValue, yaml);
    }

    protected int getIntIfAvailable(String key) {
        return YamlTools.getIntIfAvailable(key, yaml);
    }

    protected int getIntIfAvailable(String key, int defaultValue) {
        return YamlTools.getIntIfAvailable(key, defaultValue, yaml);
    }

    protected double getDoubleIfAvailable(String key) {
        return YamlTools.getDoubleIfAvailable(key, yaml);
    }

    protected double getDoubleIfAvailable(String key, double defaultValue) {
        return YamlTools.getDoubleIfAvailable(key, defaultValue, yaml);
    }

    protected long getLongIfAvailable(String key) {
        return YamlTools.getLongIfAvailable(key, yaml);
    }

    protected long getLongIfAvailable(String key, long defaultValue) {
        return YamlTools.getLongIfAvailable(key, defaultValue, yaml);
    }
}
