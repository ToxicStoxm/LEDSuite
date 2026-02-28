package com.toxicstoxm.LEDSuite.communication.packet_management.packets;

import com.toxicstoxm.LEDSuite.auto_registration.AutoRegistrableItem;
import com.toxicstoxm.LEDSuite.auto_registration.Registrable;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.Serializable;
import com.toxicstoxm.YAJL.core.Logger;

/**
 * <strong>Meaning:</strong><br>
 * Interface for communication packets used for interacting with the server.
 * <p>
 * Implementations of this interface represent different types of communication packets, such as requests, replies, or errors.
 * This interface provides the structure for serializing and deserializing packets, handling their types and subtypes,
 * and registering them automatically with the system. Implementations of this interface are auto-registrable, meaning they
 * can be automatically registered and managed by the system.
 * </p>
 *
 * <strong>Auto-Registration:</strong><br>
 * Derivatives of this interface are automatically registered into the system. For more details on auto-registration,
 * see {@link Registrable}.
 *
 * @since 1.0.0
 */
public interface Packet extends Serializable<String, Packet>, AutoRegistrableItem {
    Logger getLogger();

    /**
     * Returns the type of the packet item (e.g., request, reply, error).
     * <p>
     * This is equivalent to the packet's identifier, as defined by the combination of {@link #getType()} and {@link #getSubType()}.
     * </p>
     *
     * @return the packet's type identifier.
     */
    default String getItemType() {
        return getIdentifier();
    }

    /**
     * Gets the general type of the packet, such as {@code request}, {@code reply}, {@code error}, etc.
     *
     * @return the general type of the packet.
     */
    String getType();

    /**
     * Gets the specific subtype of the packet, such as {@code status request}, {@code menu reply}, {@code parsing error}, etc.
     *
     * @return the packet's subtype or specific type.
     */
    String getSubType();

    /**
     * Combines the {@link #getType()} and {@link #getSubType()} to form a unique identifier for the packet.
     * This is typically in the format of {@code type.subType}.
     *
     * @return the packet's identifier, a combination of the type and subtype.
     */
    default String getIdentifier() {
        String type = getType();
        String subType = getSubType();

        if (subType == null || subType.isBlank()) {
            return type;
        }

        return type + "." + subType;
    }

    /**
     * Deserializes the given YAML string into a packet object.
     * <p>
     * This method attempts to convert the YAML string into a valid packet. If the YAML is invalid or required values
     * are missing, a {@link DeserializationException} is thrown.
     * </p>
     *
     * @param yamlString the YAML string to deserialize.
     * @return the deserialized packet object.
     * @throws DeserializationException if the YAML string is invalid or if required data is missing.
     * @see #serialize()
     */
    Packet deserialize(String yamlString) throws DeserializationException;

    /**
     * Serializes this packet into a YAML string.
     * <p>
     * This method converts the packet object into a YAML string representation that can be sent or stored.
     * </p>
     *
     * @return the serialized YAML string.
     * @see #deserialize(String)
     */
    String serialize();

    /**
     * Attempts to handle the packet using relevant API endpoints.
     * <p>
     * This method logs a warning if no specific implementation for handling this packet is found.
     * The default implementation simply logs the serialized packet.
     * </p>
     *
     * @since 1.0.0
     */
    default void handlePacket() {
        getLogger().warn("Implementation for handling packet type '" + getIdentifier() + "' wasn't found!");
        getLogger().debug("Using default implementation to display packet:\n " + serialize());
    }
}
