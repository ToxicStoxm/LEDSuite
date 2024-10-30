package com.toxicstoxm.LEDSuite.tools;

import com.toxicstoxm.LEDSuite.communication.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.PacketManager;
import com.toxicstoxm.YAJSI.api.yaml.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * Wrapper class for useful YAML tools.
 * @since 1.0.0
 */
public class YamlTools {

    /**
     * Checks if the given key exists in the given config section.
     * @param key the key to check for
     * @param yaml the config section to check
     * @return {@code true} if the key exists within the given config section, otherwise {@code false}
     */
    public static boolean checkIfKeyExists(String key, @NotNull ConfigurationSection yaml) {
        return yaml.contains(key);
    }

    /**
     * Checks if the given key exists in the given config section. If not this throws a {@link com.toxicstoxm.LEDSuite.communication.DeserializationException}.
     * @param key the key to check for
     * @param yaml the config section to check
     * @throws com.toxicstoxm.LEDSuite.communication.DeserializationException if the given key does not exist in the given config section
     */
    public static void ensureKeyExists(String key, @NotNull ConfigurationSection yaml) throws DeserializationException {
        if (!yaml.contains(key)) throw new DeserializationException("Deserialization failed! Required value " + key + " is missing!");
    }
}
