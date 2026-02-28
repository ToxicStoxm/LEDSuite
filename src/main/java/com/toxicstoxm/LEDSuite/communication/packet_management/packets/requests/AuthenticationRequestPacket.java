package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.YAJL.core.Logger;
import com.toxicstoxm.YAJL.core.LoggerManager;
import lombok.*;

/**
 * Represents a request to authenticate a user with the server.
 * <p>
 * This packet is used to transmit login credentials, such as a username and
 * a hashed password, from the client to the server for authentication purposes.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *     <li>Supports automatic registration via the {@link AutoRegister} annotation.</li>
 *     <li>Implements the {@link CommunicationPacket} abstract class, inheriting general packet functionalities.</li>
 *     <li>Defines a specific sub-type, {@code AUTHENTICATE}, to classify the packet.</li>
 * </ul>
 *
 * <h3>Serialization and Deserialization:</h3>
 * <p>
 * The class provides methods to convert the packet to and from YAML format:
 * </p>
 * <ul>
 *     <li><strong>serialize:</strong> Converts the packet into a YAML string.</li>
 *     <li><strong>deserialize:</strong> Populates the packet fields using data from a YAML string.</li>
 * </ul>
 *
 * <h3>Usage:</h3>
 * <p>
 * To create and send an authentication request:
 * </p>
 * <pre>
 *     AuthenticationRequestPacket packet = AuthenticationRequestPacket.builder()
 *             .username("user123")
 *             .passwordHash("hashed_password")
 *             .build();
 * </pre>
 *
 * @since 1.0.0
 */
@AllArgsConstructor
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@NoArgsConstructor
@Setter
public class AuthenticationRequestPacket extends CommunicationPacket {
    private static final Logger logger = LoggerManager.getLogger(AuthenticationRequestPacket.class);

    @Override
    public Logger getLogger() {
        return logger;
    }

    /**
     * The username to authenticate.
     * <p>
     * This field contains the identifier of the user attempting to log in.
     * </p>
     */
    private String username;

    /**
     * The hashed password for authentication.
     * <p>
     * This field contains the cryptographic hash of the user's password.
     * </p>
     */
    private String passwordHash;

    /**
     * Returns the type of this packet.
     * <p>
     * The type is categorized as a request packet in the communication protocol.
     * </p>
     *
     * @return the packet type, which is {@code REQUEST}.
     */
    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REQUEST;
    }

    /**
     * Returns the subtype of this packet.
     * <p>
     * The subtype identifies this packet as an {@code AUTHENTICATE} request.
     * </p>
     *
     * @return the subtype string.
     */
    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Request.Types.AUTHENTICATE;
    }

    /**
     * Deserializes a YAML string into an {@code AuthenticationRequestPacket}.
     * <p>
     * This method populates the {@code username} and {@code passwordHash} fields
     * using the corresponding YAML keys.
     * </p>
     *
     * @param yamlString the YAML string to deserialize.
     * @return a populated {@code AuthenticationRequestPacket}.
     * @throws DeserializationException if required, keys are missing or invalid.
     */
    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        super.deserialize(yamlString);
        AuthenticationRequestPacket packet = AuthenticationRequestPacket.builder().build();

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.AuthenticationRequest.USERNAME);
        packet.username = yaml.getString(Constants.Communication.YAML.Keys.Request.AuthenticationRequest.USERNAME);

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.AuthenticationRequest.PASSWORD_HASH);
        packet.passwordHash = yaml.getString(Constants.Communication.YAML.Keys.Request.AuthenticationRequest.PASSWORD_HASH);

        return packet;
    }

    /**
     * Serializes this {@code AuthenticationRequestPacket} into a YAML string.
     * <p>
     * Converts the {@code username} and {@code passwordHash} fields into
     * the corresponding YAML key-value pairs.
     * </p>
     *
     * @return the serialized YAML string representation of this packet.
     */
    @Override
    public String serialize() {
        super.serialize();

        yaml.set(Constants.Communication.YAML.Keys.Request.AuthenticationRequest.USERNAME, username);
        yaml.set(Constants.Communication.YAML.Keys.Request.AuthenticationRequest.PASSWORD_HASH, passwordHash);

        return yaml.saveToString();
    }
}
