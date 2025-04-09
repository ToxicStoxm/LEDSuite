package com.toxicstoxm.LEDSuite.communication.packet_management;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.Registrable;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModule;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogLevels;
import com.toxicstoxm.YAJL.Logger;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <strong>Meaning:</strong><br>
 * The {@code PacketManager} class is responsible for handling the serialization and deserialization of communication packets
 * within the system. It ensures that packets are correctly converted between their object representation and their serialized
 * YAML format.
 * <p>
 * <strong>Purpose:</strong><br>
 * This class provides functionality for registering, serializing, and deserializing packets. It ensures that packets are
 * correctly serialized into YAML and deserialized back into appropriate packet objects, while managing errors that may occur
 * during these processes.
 * <p>
 * The class uses automatic registration of packet types and can handle serialization and deserialization for any registered packet type.
 *
 * @since 1.0.0
 */
public class PacketManager extends Registrable<Packet> {

    private static final Logger logger = Logger.autoConfigureLogger();

    private final String packetClassPath;

    /**
     * Constructs a {@code PacketManager} instance with a given class path for packet types.
     *
     * @param packetClassPath the class path to use for automatically registering packet types
     */
    public PacketManager(String packetClassPath) {
        this.packetClassPath = packetClassPath;
    }

    @Override
    protected AutoRegisterModule<Packet> autoRegisterModule() {
        logger.verbose("Auto-registering packets within module -> '{}'", packetClassPath);
        return AutoRegisterModule.<Packet>builder()
                .moduleType(Packet.class)
                .module(AutoRegisterModules.PACKETS)
                .classPath(packetClassPath)
                .build();
    }

    /**
     * Serializes a packet into a YAML string using the packet's {@link Packet#serialize()} method.
     * <br><br>
     * <strong>Note:</strong><br>
     * This method first checks if the packet type is registered. If not, it logs an error and returns {@code null}.
     *
     * @param packet the packet to serialize
     * @return the serialized YAML string, or {@code null} if the packet type is not registered
     * @see #deserialize(Class, String)
     */
    public @Nullable String serialize(@NotNull Packet packet) {
        logger.log(LEDSuiteLogLevels.COMMUNICATION_OUT, "Serializing packet -> '{}.{}'", packet.getType(), packet.getSubType());
        String packetIdentifier = packet.getIdentifier();

        // Validate if the packet type exists in the registered packets
        if (!isRegistered(packetIdentifier)) {
            logger.info("Error: Packet type not registered: {}", packetIdentifier);
            return null;
        }

        // Serialize the packet using its serialize method
        return packet.serialize();
    }

    /**
     * Deserializes a YAML string into a {@link CommunicationPacket}.
     *
     * <strong>Note:</strong><br>
     * This method is a convenient wrapper that uses the default {@link CommunicationPacket} class for deserialization.
     *
     * @param yamlString the YAML string to deserialize
     * @return the deserialized packet as a {@link CommunicationPacket} instance
     * @throws DeserializationException if deserialization fails
     * @see #deserialize(Class, String)
     */
    public CommunicationPacket deserialize(String yamlString) throws DeserializationException {
        return deserialize(CommunicationPacket.class, yamlString);
    }

    /**
     * Deserializes a YAML string into the specific packet type defined by the {@code clazz} parameter.
     * This method uses the packet's {@link Packet#deserialize(String)} method to convert the YAML string into a packet instance.
     * It first extracts the packet identifier from the YAML and checks if the packet type is registered.
     *
     * @param clazz the class of the expected packet implementation (e.g., {@link CommunicationPacket})
     * @param yamlString the YAML string to deserialize
     * @param <T> the type of the expected packet class
     * @return the deserialized packet as an instance of the specified class
     * @throws DeserializationException if any issues arise during deserialization:
     *         - Invalid YAML format
     *         - Unregistered packet type
     *         - Class cast issues when the deserialized object does not match the expected class
     * @see #deserialize(String)
     */
    public <T extends Packet> T deserialize(@NotNull Class<T> clazz, @NotNull String yamlString) throws DeserializationException {
        String packetIdentifier = extractPacketIdentifier(yamlString);

        if (!isRegistered(packetIdentifier)) {
            throw new DeserializationException("Invalid packet type: '" + packetIdentifier + "'!");
        }

        logger.log(LEDSuiteLogLevels.COMMUNICATION_IN, "Deserializing packet -> '{}'", packetIdentifier);

        // Retrieve the packet type and perform the deserialization
        try {
            Packet packetInstance = get(packetIdentifier).deserialize(yamlString);
            if (!clazz.isInstance(packetInstance)) {
                throw new ClassCastException("Deserialized object is not of the expected type: " + clazz.getName());
            }
            return clazz.cast(packetInstance);
        } catch (ClassCastException e) {
            throw new DeserializationException("Couldn't create " + clazz.getName() + " from the provided YAML string!", e);
        } catch (DeserializationException e) {
            throw new DeserializationException("Deserialization for " + clazz.getName() + " failed! " + e.getMessage(), e);
        }
    }

    /**
     * Extracts the {@code packet-type} identifier from a YAML string. This method looks for the {@code packet-type} key in the YAML
     * configuration, and constructs the packet identifier using the packet type and subtype if available.
     *
     * @param yamlString the YAML string to extract the packet type from
     * @return the packet identifier (e.g., "request.status")
     * @throws DeserializationException if the YAML string is invalid or the packet-type is not found
     */
    private @NotNull String extractPacketIdentifier(@NotNull String yamlString) throws DeserializationException {
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.loadFromString(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new DeserializationException("Failed to deserialize YAML!", e);
        }

        String packetType = yaml.getString(Constants.Communication.YAML.Keys.General.PACKET_TYPE);
        String subType = yaml.getString(Constants.Communication.YAML.Keys.General.SUB_TYPE);
        if (packetType == null) {
            throw new DeserializationException("Invalid packet type '" + null + "'");
        }

        String packetIdentifier = packetType;
        if (!(subType == null || subType.isBlank())) {
            packetIdentifier = packetIdentifier + "." + subType;
        }

        return packetIdentifier;
    }

}
