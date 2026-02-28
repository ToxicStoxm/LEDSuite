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
 * This packet is used as callback for MessageDialog responses.
 * When the user picks a response, this packet will be sent to the server containing the UUID,
 * which was assigned to it by the server.
 * @since 1.0.0
 */
@AllArgsConstructor
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@NoArgsConstructor
@Setter
public class ServerMessageResponseRequestPacket extends CommunicationPacket {
    private static final Logger logger = LoggerManager.getLogger(ServerMessageResponseRequestPacket.class);

    @Override
    public Logger getLogger() {
        return logger;
    }

    private String responseID;

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REQUEST;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Request.Types.MESSAGE_RESPONSE_REQUEST;
    }

    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        super.deserialize(yamlString);
        ServerMessageResponseRequestPacket packet = ServerMessageResponseRequestPacket.builder().build();

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.ServerMessageResponseRequest.RESPONSE_ID);
        packet.setResponseID(
                getStringIfAvailable(Constants.Communication.YAML.Keys.Request.ServerMessageResponseRequest.RESPONSE_ID)
        );

        return packet;
    }

    @Override
    public String serialize() {
        super.serialize();

        yaml.set(Constants.Communication.YAML.Keys.Request.ServerMessageResponseRequest.RESPONSE_ID, responseID);

        return yaml.saveToString();
    }
}
