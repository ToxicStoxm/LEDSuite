package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.AutoRegisterPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.PacketManager;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import lombok.*;

/**
 * <strong>Meaning:</strong><br>
 * Request for renaming the specified file to the specified name.
 * @since 1.0.0
 */
@AllArgsConstructor
@AutoRegisterPacket
@Builder
@Getter
@NoArgsConstructor
@Setter
public class RenameRequestPacket extends CommunicationPacket {

    private String requestFile;
    private String newName;

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REQUEST;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Request.Types.RENAME_REQUEST;
    }

    @Override
    public Packet deserialize(String yamlString) throws PacketManager.DeserializationException {
        RenameRequestPacket packet = RenameRequestPacket.builder().build();
        YamlConfiguration yaml;
        try {
            yaml = loadYAML(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new PacketManager.DeserializationException(e);
        }

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.General.FILE, yaml);
        packet.requestFile = yaml.getString(Constants.Communication.YAML.Keys.Request.General.FILE);

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.RenameRequest.NEW_NAME, yaml);
        packet.newName = yaml.getString(Constants.Communication.YAML.Keys.Request.RenameRequest.NEW_NAME);

        return packet;
    }

    @Override
    public String serialize() {
        YamlConfiguration yaml = saveYAML();

        yaml.set(Constants.Communication.YAML.Keys.Request.General.FILE, requestFile);

        yaml.set(Constants.Communication.YAML.Keys.Request.RenameRequest.NEW_NAME, newName);

        return yaml.saveToString();
    }
}
