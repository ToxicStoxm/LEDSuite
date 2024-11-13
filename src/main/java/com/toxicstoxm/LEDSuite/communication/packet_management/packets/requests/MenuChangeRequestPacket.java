package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import lombok.*;

/**
 * <strong>Meaning:</strong><br>
 * Request for changing a setting in an animation menu.
 * Used to notify the server if the user changes a setting in some animation menu.
 * @since 1.0.0
 */
@AllArgsConstructor
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@NoArgsConstructor
@Setter
public class MenuChangeRequestPacket extends CommunicationPacket {

    private String objectId;
    private String objectValue;
    private String fileName;

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REQUEST;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Request.Types.MENU_CHANGE;
    }

    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        super.deserialize(yamlString);
        MenuChangeRequestPacket packet = MenuChangeRequestPacket.builder().build();

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.MenuChangeRequest.OBJECT_ID);
        packet.objectId = yaml.getString(Constants.Communication.YAML.Keys.Request.MenuChangeRequest.OBJECT_ID);

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.MenuChangeRequest.OBJECT_VALUE);
        packet.objectValue = yaml.getString(Constants.Communication.YAML.Keys.Request.MenuChangeRequest.OBJECT_VALUE);

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.MenuChangeRequest.FILE_NAME);
        packet.fileName = yaml.getString(Constants.Communication.YAML.Keys.Request.MenuChangeRequest.FILE_NAME);

        return packet;
    }

    @Override
    public String serialize() {
        super.serialize();

        yaml.set(Constants.Communication.YAML.Keys.Request.MenuChangeRequest.OBJECT_ID, objectId);
        yaml.set(Constants.Communication.YAML.Keys.Request.MenuChangeRequest.OBJECT_VALUE, objectValue);
        yaml.set(Constants.Communication.YAML.Keys.Request.MenuChangeRequest.FILE_NAME, fileName);

        return yaml.saveToString();
    }
}
