package com.toxicstoxm.LEDSuite.communication.packet_management;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * Wrapper class for managing serialization and deserialization of network packets.
 * @see #registerPacket(String, Packet)
 * @see #serialize(Packet)
 * @see #deserialize(Class, String)
 * @since 1.0
 */
public class PacketManager {

    private final HashMap<String, Packet> registeredPackets = new HashMap<>();

    /**
     * Registers the specified packet interface under the specified name. After a packet is registered it can be serialized and deserialized using the {@link #serialize(Packet)} and {@link #deserialize(Class, String)} methods.<br>
     * If another packet is already registered under the specified packet name. This method will fail and return {@code false}.
     * @param packet_type the packet type (name)
     * @param packet the packet interface with serialization and deserialization methods to register
     * @return {@code true} if the packet was successfully registered, otherwise {@code false}
     * @see #unregisterPacket(String)
     * @see #clearPackets()
     */
    public boolean registerPacket(String packet_type, Packet packet) {
        return registeredPackets.putIfAbsent(packet_type, packet) == null;
    }

    /**
     * Unregisters the packet registered under the specified packet name.
     * @param packet_type the packet type (name) to unregister
     * @return {@code true} if the specified packet was previously registered and was successfully unregistered, otherwise {@code false}
     * @see #registerPacket(String, Packet)
     * @see #clearPackets()
     */
    public boolean unregisterPacket(String packet_type) {
        return registeredPackets.remove(packet_type) != null;
    }

    /**
     * Clears all registered packets.
     * @see #registerPacket(String, Packet)
     * @see #unregisterPacket(String)
     */
    public void clearPackets() {
        registeredPackets.clear();
    }

    /**
     * Attempts to serialize the specified packet using its {@link Packet#serialize()} method.
     * @param packet the packet to serialize
     * @return the serialized string or {@code null} if the specified packet type is not registered
     * @see #deserialize(Class, String)
     */
    public @Nullable String serialize(@NotNull Packet packet) {
        String packetType = packet.getPacketType();

        // Validate if the packet type exists in the registered packets
        if (!registeredPackets.containsKey(packetType)) {
            LEDSuiteApplication.getLogger().info("Error: Packet type not registered: " + packetType, new LEDSuiteLogAreas.YAML());
            System.err.println();
            return null;
        }

        // Serialize the packet directly using its method
        return packet.serialize();
    }

    /**
     * Attempts to deserialize the specified YAML string by trying to load the string into a {@link YamlConfiguration} and retrieving its {@code packet-type} value using {@link #extractPacketType(String)}.
     * If the retrieved packet-type is registered, the string will be deserialized using the specific packets {@link Packet#deserialize(String)} method.
     * @param clazz the corresponding packet implementation class
     * @param yamlString the YAML string to deserialize
     * @return the deserialized packet
     * @param <T> the corresponding packet implementation class
     * @throws DeserializationException if the YAML string is not valid YAML,
     * the {@code packet-type} entry does not exist,
     * the {@code packet-type} value is not registered,
     * the deserialized packet could not be cast the specified packet implementation class
     */
    public <T extends Packet> T deserialize(@NotNull Class<T> clazz, @NotNull String yamlString) throws DeserializationException {
        String packetType = extractPacketType(yamlString);

        // Retrieve the packet type and perform the deserialization
        try {
            Packet packetInstance = registeredPackets.get(packetType).deserialize(yamlString);
            if (!clazz.isInstance(packetInstance)) {
                throw new ClassCastException("Deserialized object is not of the expected type: " + clazz.getName());
            }
            return clazz.cast(packetInstance);
        } catch (ClassCastException e) {
            throw new DeserializationException("Couldn't create " + clazz.getName() + " from the provided YAML string!", e);
        }
    }

    /**
     * Tries to extract a {@code packet-type} from this YAML string
     * @param yamlString the YAML string to extract the packet type from
     * @return the retrieved packet type
     * @throws DeserializationException if the given YAML string isn't valid YAML or the extracted packet type is not registered
     */
    private @NotNull String extractPacketType(@NotNull String yamlString) throws DeserializationException {
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.loadFromString(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new DeserializationException("Failed to deserialize YAML!", e);
        }

        String packetType = yaml.getString(Constants.Communication.YAML.Keys.General.PacketType);
        if (packetType == null || !registeredPackets.containsKey(packetType)) {
            throw new DeserializationException("Invalid or unknown packet type '" + packetType + "'");
        }
        return packetType;
    }

    /**
     * {@code DeserializationException} is an unchecked exception and is used to express an error during deserialization of a communication packet.
     */
    public static class DeserializationException extends RuntimeException {
        public DeserializationException(String message) {
            super(message);
        }

        public DeserializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
