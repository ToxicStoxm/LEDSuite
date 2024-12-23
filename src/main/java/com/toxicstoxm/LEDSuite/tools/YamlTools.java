package com.toxicstoxm.LEDSuite.tools;

import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.ErrorCode;
import com.toxicstoxm.YAJSI.api.yaml.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * A utility class providing methods for interacting with YAML configuration sections.
 * <p>
 * This class includes methods for checking the existence of keys within a YAML configuration
 * section and ensuring that required keys are present. If a required key is missing, an exception is thrown.
 * </p>
 *
 * @since 1.0.0
 */
public class YamlTools {

    /**
     * Checks if the specified key exists in the given configuration section.
     *
     * @param key the key to check for in the configuration section
     * @param yaml the configuration section to check
     * @return {@code true} if the key exists in the configuration section, otherwise {@code false}
     */
    public static boolean checkIfKeyExists(String key, @NotNull ConfigurationSection yaml) {
        return yaml.contains(key);
    }

    /**
     * Ensures that the specified key exists in the given configuration section.
     *
     * @param key the key to check for in the configuration section
     * @param yaml the configuration section to check
     * @throws DeserializationException if the specified key does not exist in the configuration section
     * @see DeserializationException
     */
    public static void ensureKeyExists(String key, @NotNull ConfigurationSection yaml) throws DeserializationException {
        if (!yaml.contains(key)) {
            throw new DeserializationException("Deserialization failed! Required key '" + key + "' is missing.", ErrorCode.RequiredKeyIsMissing);
        }
    }

    /**
     * Ensures that the specified key exists in the given configuration section.
     * If the key is missing, a {@link DeserializationException} is thrown with a custom error code.
     *
     * @param key the key to check for in the configuration section
     * @param yaml the configuration section to check
     * @param errorCode the custom error code to return if the key is missing
     * @throws DeserializationException if the specified key does not exist in the configuration section
     * @see DeserializationException
     */
    public static void ensureKeyExists(String key, @NotNull ConfigurationSection yaml, ErrorCode errorCode) throws DeserializationException {
        if (!yaml.contains(key)) {
            throw new DeserializationException("Deserialization failed! Required key '" + key + "' is missing.", errorCode);
        }
    }

    public static String getStringIfAvailable(String key, @NotNull ConfigurationSection yaml) {
        if (checkIfKeyExists(key, yaml)) {
            return yaml.getString(key);
        }
        return "";
    }

    public static String getStringIfAvailable(String key, String defaultValue, @NotNull ConfigurationSection yaml) {
        if (checkIfKeyExists(key, yaml)) {
            return yaml.getString(key);
        }
        return defaultValue;
    }

    public static boolean getBooleanIfAvailable(String key, @NotNull ConfigurationSection yaml) {
        if (checkIfKeyExists(key, yaml)) {
            return yaml.getBoolean(key);
        }
        return false;
    }

    public static boolean getBooleanIfAvailable(String key, boolean defaultValue, @NotNull ConfigurationSection yaml) {
        if (checkIfKeyExists(key, yaml)) {
            return yaml.getBoolean(key);
        }
        return defaultValue;
    }

    public static int getIntIfAvailable(String key, @NotNull ConfigurationSection yaml) {
        if (checkIfKeyExists(key, yaml)) {
            return yaml.getInt(key);
        }
        return 0;
    }

    public static int getIntIfAvailable(String key, int defaultValue, @NotNull ConfigurationSection yaml) {
        if (checkIfKeyExists(key, yaml)) {
            return yaml.getInt(key);
        }
        return defaultValue;
    }

    public static double getDoubleIfAvailable(String key, @NotNull ConfigurationSection yaml) {
        if (checkIfKeyExists(key, yaml)) {
            return yaml.getDouble(key);
        }
        return 0;
    }

    public static double getDoubleIfAvailable(String key, double defaultValue, @NotNull ConfigurationSection yaml) {
        if (checkIfKeyExists(key, yaml)) {
            return yaml.getDouble(key);
        }
        return defaultValue;
    }

    public static long getLongIfAvailable(String key, @NotNull ConfigurationSection yaml) {
        if (checkIfKeyExists(key, yaml)) {
            return yaml.getLong(key);
        }
        return 0;
    }

    public static long getLongIfAvailable(String key, long defaultValue, @NotNull ConfigurationSection yaml) {
        if (checkIfKeyExists(key, yaml)) {
            return yaml.getLong(key);
        }
        return defaultValue;
    }
}
