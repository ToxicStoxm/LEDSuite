package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.media_request;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;

/**
 * Abstract base class for media-related request packets, such as play, pause, and other animation controls.
 * <p>
 * This class provides a common framework for handling media operations that require a file as input.
 * Subclasses must implement the {@link #setRequestFile(String)} and {@link #getRequestFile()} methods to
 * define how the requested file is stored and retrieved.
 * They must also specify their unique subtype
 * by overriding {@link #getSubType()}.
 * </p>
 *
 * <h3>Key Responsibilities:</h3>
 * <ul>
 *     <li>Handles serialization and deserialization of the media request packet.</li>
 *     <li>Ensures that the required file name key exists during deserialization.</li>
 *     <li>Defines a common request packet type ({@code REQUEST}).</li>
 * </ul>
 *
 * <h3>Usage:</h3>
 * <p>
 * To create a new media request packet, extend this class and provide implementations for
 * {@link #setRequestFile(String)}, {@link #getRequestFile()}, and {@link #getSubType()}.
 * Example derived classes include {@link PlayRequestPacket} and {@link PauseRequestPacket}.
 * </p>
 *
 * @see PlayRequestPacket
 * @see PauseRequestPacket
 * @since 1.0.0
 */
public abstract class MediaRequestPacket extends CommunicationPacket {
    /**
     * Sets the file associated with this media request.
     * <p>
     * Subclasses must provide an implementation to specify how the file name or path is managed.
     * </p>
     *
     * @param requestFile the file name or path for the media operation.
     */
    public abstract void setRequestFile(String requestFile);

    /**
     * Gets the file associated with this media request.
     * <p>
     * Subclasses must provide an implementation to specify how the file name or path is retrieved.
     * </p>
     *
     * @return the file name or path for the media operation.
     */
    public abstract String getRequestFile();

    /**
     * Returns the packet type for media requests.
     * <p>
     * The type for all media request packets is defined as {@code REQUEST} in the YAML configuration.
     * </p>
     *
     * @return a string representing the general packet type.
     */
    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REQUEST;
    }

    /**
     * Returns the specific sub-type of this media request packet.
     * <p>
     * Subclasses must override this method to provide their unique sub-type, such as
     * {@code "PLAY"} for a {@link PlayRequestPacket}.
     * </p>
     *
     * @return a string representing the sub-type of the packet.
     */
    @Override
    public abstract String getSubType();

    /**
     * Deserializes the packet from its YAML representation.
     * <p>
     * Reads the required file name key from the YAML configuration and stores it using
     * {@link #setRequestFile(String)}. Throws a {@link DeserializationException} if the key is missing.
     * </p>
     *
     * @param yamlString the YAML string representation of the packet.
     * @return the deserialized packet.
     * @throws DeserializationException if required keys are missing or invalid.
     */
    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        super.deserialize(yamlString);

        ensureKeyExists(Constants.Communication.YAML.Keys.General.FILE_NAME);
        setRequestFile(yaml.getString(Constants.Communication.YAML.Keys.General.FILE_NAME));

        return this;
    }

    /**
     * Serializes the packet to its YAML representation.
     * <p>
     * Adds the file name to the YAML structure using {@link #getRequestFile()}.
     * This ensures the file information is included when the packet is sent.
     * </p>
     *
     * @return a YAML string representation of the packet.
     */
    @Override
    public String serialize() {
        super.serialize();

        yaml.set(Constants.Communication.YAML.Keys.General.FILE_NAME, getRequestFile());

        return yaml.saveToString();
    }
}
