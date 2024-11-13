package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import lombok.*;

@AllArgsConstructor
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@NoArgsConstructor
@Setter
public class AuthenticationRequestPacket extends CommunicationPacket {

    private String username;
    private String passwordHash;

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REQUEST;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Request.Types.AUTHENTICATE;
    }

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

    @Override
    public String serialize() {
        super.serialize();

        yaml.set(Constants.Communication.YAML.Keys.Request.AuthenticationRequest.USERNAME, username);
        yaml.set(Constants.Communication.YAML.Keys.Request.AuthenticationRequest.PASSWORD_HASH, passwordHash);

        return yaml.saveToString();
    }
}
