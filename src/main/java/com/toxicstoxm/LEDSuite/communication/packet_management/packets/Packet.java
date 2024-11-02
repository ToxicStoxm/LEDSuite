package com.toxicstoxm.LEDSuite.communication.packet_management.packets;

import com.toxicstoxm.LEDSuite.auto_registration.AutoRegistrableItem;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.Serializable;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;

/**
 *
 * @since 1.0.0
 */
public interface Packet extends Serializable<String, Packet>, AutoRegistrableItem {

    default String getItemType() {
        return getIdentifier();
    }

    /**
     * The packets general type. E.g. {@code request} , {@code reply}, {@code error}, ...
     * @return the packets general type
     */
    String getType();

    /**
     * The packet-subtype. E.g. {@code status request}, {@code menu reply}, {@code parsing error}, ...
     * @return the packets subtype / more specific type
     */
    String getSubType();

    /**
     * The packet identifier (packet-type.packet-subtype). Combination of {@link #getType()} and {@link #getSubType()}.
     * @return the packets identifier
     */
    default String getIdentifier() {

        String type = getType();
        String subType = getSubType();

        if (subType == null || subType.isBlank()) return type;

        return type + "." + subType;
    }

    /**
     * Attempts to deserialize a given YAML string.
     * @param yamlString YAML string to deserialize
     * @return the deserialized packet
     * @throws DeserializationException if deserialization fails, because invalid YAML or missing values
     * @see #serialize()
     */
    Packet deserialize(String yamlString) throws DeserializationException;

    /**
     * Attempts to serialize this packet.
     * @return the serialized string
     * @see #deserialize(String)
     */
    String serialize();

    /**
     * Tries to handle itself using various API endpoints.
     */
    default void handlePacket() {
        LEDSuiteApplication.getLogger().warn("Implementation for handling packet type '" + getIdentifier() + "' wasn't found!", new LEDSuiteLogAreas.COMMUNICATION());
        LEDSuiteApplication.getLogger().debug("Using default implementation to display packet:\n " + serialize(), new LEDSuiteLogAreas.COMMUNICATION());
    }
}
