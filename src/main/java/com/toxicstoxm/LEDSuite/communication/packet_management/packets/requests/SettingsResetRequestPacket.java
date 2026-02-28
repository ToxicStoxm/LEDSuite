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
 * Represents a request to reset the current settings on the server.
 * <p>
 * This packet is sent when the client requests
 * that the server reset its configuration settings to the default or a predefined state.
 * The server will typically respond with a {@link SettingsReplyPacket},
 * which may indicate whether the reset was successful.
 * </p>
 *
 * @since 1.0.0
 * @see SettingsReplyPacket The packet the server will respond with, confirming the reset of settings.
 */
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@Setter
public class SettingsResetRequestPacket extends CommunicationPacket {
    private static final Logger logger = LoggerManager.getLogger(SettingsResetRequestPacket.class);

    @Override
    public Logger getLogger() {
        return logger;
    }

    /**
     * Gets the packet type. This is used to identify the overall category of the packet (e.g., "REQUEST").
     *
     * @return the packet type as a string (e.g., "REQUEST").
     */
    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REQUEST;
    }

    /**
     * Gets the packet subtype. This specifies the specific kind of request being made—in this case, a settings reset request.
     *
     * @return the packet subtype (e.g., "SETTINGS_RESET").
     */
    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Request.Types.SETTINGS_RESET;
    }

    /**
     * Deserializes the YAML string into a {@link SettingsResetRequestPacket} instance.
     * As this packet contains no additional data, it returns a newly created instance without processing the YAML content.
     *
     * @param yamlString the YAML string representing the packet data (ignored in this case).
     * @return a newly created {@code SettingsResetRequestPacket} instance.
     * @throws DeserializationException if an error occurs during deserialization (not applicable here, but required by the method signature).
     */
    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        return SettingsResetRequestPacket.builder().build();
    }
}
