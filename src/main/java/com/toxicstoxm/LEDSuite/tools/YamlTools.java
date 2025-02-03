package com.toxicstoxm.LEDSuite.tools;

import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.ErrorCode;
import com.toxicstoxm.YAJL.Logger;
import com.toxicstoxm.YAJSI.api.yaml.ConfigurationSection;
import io.github.jwharm.javagi.base.GErrorException;
import org.gnome.gdk.Paintable;
import org.gnome.gdk.Texture;
import org.gnome.glib.Bytes;
import org.gnome.gtk.Image;
import org.jetbrains.annotations.NotNull;

import java.util.Base64;

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

    private static final Logger logger = Logger.autoConfigureLogger();

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

    /**
     * If {@code iconIsName} is {@code true} then a new {@link Image} will be constructed using {@link Image#fromIconName(String)}.
     * <p>
     * Otherwise, the string is treated as base64. This means that it is first decoded using {@link Base64#getDecoder()}
     * and then a {@link Texture} will be created using {@link Texture#fromBytes(Bytes)}.
     * Finally, an {@link Image} will be created using {@link Image#fromPaintable(Paintable)} and passing in the {@link Texture}.
     * @param iconString the icon name or base64
     * @param iconIsName true if {@code iconString} should be treated as name, otherwise {@code false}
     * @return the constructed {@link Image} or a 'broken image' if something went wrong or the name/base64 was invalid.
     */
    public static Image constructIcon(String iconString, boolean iconIsName) {
        Image finalImage = Image.fromIconName("");
        if (iconIsName) {
            finalImage = Image.fromIconName(iconString);
        } else {
            byte[] decodedBytes = Base64.getDecoder().decode(iconString);
            try {
                finalImage = Image.fromPaintable(Texture.fromBytes(Bytes.static_(decodedBytes)));
            } catch (GErrorException e) {
                logger.warn("Failed to decode icon from base64! Error message: '{}'!", e.getMessage());
                logger.debug("Base64: {}", iconString);
            }
        }

        return finalImage;
    }
}
