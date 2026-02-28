package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.SettingsReplyPacket;
import com.toxicstoxm.YAJL.core.Logger;
import com.toxicstoxm.YAJL.core.LoggerManager;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a request to retrieve the current settings from the server.
 * <p>
 * This packet is sent to the server when the client needs to fetch the current configuration
 * settings, such as display preferences, system settings, or other configurable options.
 * The server will respond with a {@link SettingsReplyPacket}, which contains the requested settings.
 * </p>
 *
 * @since 1.0.0
 * @see SettingsReplyPacket The packet the server will respond with, containing the current settings.
 */
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@Setter
public class SettingsRequestPacket extends CommunicationPacket {
    private static final Logger logger = LoggerManager.getLogger(SettingsRequestPacket.class);

    @Override
    public Logger getLogger() {
        return logger;
    }

    /**
     * Gets the packet type. This value is used to identify the type of packet being sent in the communication protocol.
     *
     * @return the packet type as a string (e.g., "REQUEST").
     */
    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REQUEST;
    }

    /**
     * Gets the packet subtype. This value specifies the particular kind of request being made.
     * In this case, it is a request for the current settings from the server.
     *
     * @return the packet subtype (e.g., "SETTINGS").
     */
    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Request.Types.SETTINGS;
    }

    /**
     * Deserializes the YAML string into a {@link SettingsRequestPacket} instance.
     * Since this packet does not contain any data, the method simply returns a new instance of the packet.
     *
     * @param yamlString the YAML string representing the packet data (ignored in this case as no data is included).
     * @return a newly created {@code SettingsRequestPacket} instance.
     * @throws DeserializationException if an error occurs during deserialization (not applicable here, but required by the method signature).
     */
    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        return SettingsRequestPacket.builder().build();
    }
}
