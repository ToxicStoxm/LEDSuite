package com.toxicstoxm.LEDSuite.communication.packet_management;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.Registrable;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModule;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapper class for managing serialization and deserialization of network packets.
 * @see #serialize(Packet)
 * @see #deserialize(Class, String)
 * @since 1.0.0
 */
public class PacketManager extends Registrable<Packet> {

    private final String packetClassPath;

    public PacketManager(String packetClassPath) {
        this.packetClassPath = packetClassPath;
    }

    @Override
    protected AutoRegisterModule<Packet> autoRegisterModule() {
        return AutoRegisterModule.<Packet>builder()
                .moduleType(Packet.class)
                .module(AutoRegisterModules.PACKETS)
                .classPath(packetClassPath)
                .build();
    }

    /**
     * Attempts to serialize the specified packet using its {@link Packet#serialize()} method.
     * @param packet the packet to serialize
     * @return the serialized string or {@code null} if the specified packet type is not registered
     * @see #deserialize(Class, String)
     */
    public @Nullable String serialize(@NotNull Packet packet) {
        String packetIdentifier = packet.getIdentifier();

        // Validate if the packet type exists in the registered packets
        if (!isRegistered((packetIdentifier))) {
            LEDSuiteApplication.getLogger().info("Error: Packet type not registered: " + packetIdentifier, new LEDSuiteLogAreas.YAML());
            return null;
        }

        // Serialize the packet directly using its method
        return packet.serialize();
    }

    /**
     * Attempts to deserialize the specified YAML string.
     * @param yamlString the YAML string to deserialize
     * @return the deserialized packet as instance of {@link CommunicationPacket}
     * @see #deserialize(Class, String)
     */
    public CommunicationPacket deserialize(String yamlString) throws DeserializationException {
        //LEDSuiteApplication.getLogger().info("----------\n" + yamlString + "\n----------");
        return deserialize(CommunicationPacket.class, yamlString);
    }

    /**
     * Attempts to deserialize the specified YAML string by trying to load the string into a {@link YamlConfiguration} and retrieving its {@code packet-type} value using {@link #extractPacketIdentifier(String)}.
     * If the retrieved packet-type is registered, the string will be deserialized using the specific packets {@link Packet#deserialize(String)} method.
     * @param clazz the corresponding packet implementation class
     * @param yamlString the YAML string to deserialize
     * @return the deserialized packet
     * @param <T> the corresponding packet implementation class
     * @throws DeserializationException if the YAML string is not valid YAML,
     * the {@code packet-type} entry does not exist,
     * the {@code packet-type} value is not registered,
     * the deserialized packet could not be cast the specified packet implementation class
     * @see #deserialize(String)
     */
    public <T extends Packet> T deserialize(@NotNull Class<T> clazz, @NotNull String yamlString) throws DeserializationException {
        String packetIdentifier = extractPacketIdentifier(yamlString);

        if (!isRegistered(packetIdentifier)) {
            throw new DeserializationException("Invalid packet type: '" + packetIdentifier + "'!");
        }

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
     * Tries to extract a {@code packet-type} from this YAML string
     * @param yamlString the YAML string to extract the packet type from
     * @return the retrieved packet type
     * @throws DeserializationException if the given YAML string isn't valid YAML
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
        if (!(subType == null || subType.isBlank())) packetIdentifier = packetIdentifier + "." + subType;

        return packetIdentifier;
    }

}
