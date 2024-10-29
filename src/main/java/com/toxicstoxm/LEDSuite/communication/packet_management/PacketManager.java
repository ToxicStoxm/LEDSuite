package com.toxicstoxm.LEDSuite.communication.packet_management;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Wrapper class for managing serialization and deserialization of network packets.
 * @see #registerPacket(Packet)
 * @see #serialize(Packet)
 * @see #deserialize(Class, String)
 * @since 1.0.0
 */
public class PacketManager {

    Class<? extends Packet> defaultPacket;

    public PacketManager(Class<? extends Packet> defaultPacket) {
        this.defaultPacket = defaultPacket;

    }

    private final HashMap<String, Packet> registeredPackets = new HashMap<>();

    /**
     * Registers the specified packet interface under the specified name. After a packet is registered it can be serialized and deserialized using the {@link #serialize(Packet)} and {@link #deserialize(Class, String)} methods.<br>
     * If another packet is already registered under the specified packet name. This method will fail and return {@code false}.
     * @param packet the packet interface with serialization and deserialization methods to register
     * @return {@code true} if the packet was successfully registered, otherwise {@code false}
     * @see #unregisterPacket(String)
     * @see #clearPackets()
     */
    public boolean registerPacket(Packet packet) {
        return registeredPackets.putIfAbsent(packet.getIdentifier(), packet) == null;
    }

    /**
     * Automatically registers classes annotated with {@link AutoRegisterPacket} as packets. <br>
     * Annotated classes are required to implement the {@link Packet} interface.
     */
    public void autoRegisterPackets(@NotNull String classPath) {
        Set<Class<?>> annotatedClasses = new HashSet<>();

        // Scan the specified package for classes with @AutoRegisterPacket annotation
        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .acceptPackages(classPath)
                .scan()) {

            scanResult.getClassesWithAnnotation(AutoRegisterPacket.class.getName())
                    .forEach(classInfo -> {
                        try {
                            Class<?> loadedClass = classInfo.loadClass();
                            if (Packet.class.isAssignableFrom(loadedClass)) {
                                annotatedClasses.add(classInfo.loadClass());
                            } else LEDSuiteApplication.getLogger().error("Failed to load class: " + classInfo.getName() + ". Class doesn't implement packet interface!", new LEDSuiteLogAreas.COMMUNICATION());
                        } catch (Exception e) {
                            LEDSuiteApplication.getLogger().error("Failed to load class: " + classInfo.getName(), new LEDSuiteLogAreas.COMMUNICATION());
                            LEDSuiteApplication.getLogger().error(e.getMessage(), new LEDSuiteLogAreas.COMMUNICATION());
                            throw new RuntimeException(e);
                        }
                    });
        }

        for (Class<?> clazz : annotatedClasses) {
            try {
                // Use reflection to bypass access check
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true); // Enable access to non-public constructors
                Packet packet = (Packet) constructor.newInstance();
                this.registerPacket(packet);
                LEDSuiteApplication.getLogger().info("Registered packet: " + packet.getIdentifier());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                LEDSuiteApplication.getLogger().error("Failed to auto-register packet: " + clazz.getName(), new LEDSuiteLogAreas.COMMUNICATION());
                LEDSuiteApplication.getLogger().error(e.getMessage(), new LEDSuiteLogAreas.COMMUNICATION());
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * Unregisters the packet registered under the specified packet name.
     * @param packet_type the packet type (name) to unregister
     * @return {@code true} if the specified packet was previously registered and was successfully unregistered, otherwise {@code false}
     * @see #registerPacket(Packet)
     * @see #clearPackets()
     */
    public boolean unregisterPacket(String packet_type) {
        return registeredPackets.remove(packet_type) != null;
    }

    /**
     * Clears all registered packets.
     * @see #registerPacket(Packet)
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
        String packetIdentifier = packet.getIdentifier();

        // Validate if the packet type exists in the registered packets
        if (!registeredPackets.containsKey(packetIdentifier)) {
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

        if (!registeredPackets.containsKey(packetIdentifier)) {
            throw new DeserializationException("Invalid packet type: '" + packetIdentifier + "'!");
        }

        // Retrieve the packet type and perform the deserialization
        try {
            Packet packetInstance = registeredPackets.get(packetIdentifier).deserialize(yamlString);
            if (!clazz.isInstance(packetInstance)) {
                throw new ClassCastException("Deserialized object is not of the expected type: " + clazz.getName());
            }
            return clazz.cast(packetInstance);
        } catch (ClassCastException e) {
            throw new DeserializationException("Couldn't create " + clazz.getName() + " from the provided YAML string!", e);
        } catch (Exception e) {
            throw new DeserializationException("Deserialization for " + clazz.getName() + " failed!", e);
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

    /**
     * {@code DeserializationException} is an unchecked exception and is used to express an error during deserialization of a communication packet.
     */
    public static class DeserializationException extends RuntimeException {
        public DeserializationException(Throwable cause) {
            super(cause);
        }

        public DeserializationException(String message) {
            super(message);
        }

        public DeserializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
