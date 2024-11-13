package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.media_request;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;

/**
 * Wrapper class for request packets related to animation control.
 * E.g.: {@link PlayRequestPacket}, {@link PauseRequestPacket}
 * @since 1.0.0
 */
public abstract class MediaRequestPacket extends CommunicationPacket {
    public abstract void setRequestFile(String requestFile);
    public abstract String getRequestFile();

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REQUEST;
    }

    @Override
    public abstract String getSubType();

    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        super.deserialize(yamlString);

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.General.FILE_NAME);
        setRequestFile(yaml.getString(Constants.Communication.YAML.Keys.Request.General.FILE_NAME));

        return this;
    }

    @Override
    public String serialize() {
        super.serialize();

        yaml.set(Constants.Communication.YAML.Keys.Request.General.FILE_NAME, getRequestFile());

        return yaml.saveToString();
    }
}
