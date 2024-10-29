package com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.AutoRegisterPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.PacketManager;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import lombok.*;

/**
 * <strong>Meaning:</strong><br>
 * Information about a server side error.
 * The user should be notified in some way when receiving such a packet.
 * @since 1.0.0
 */
@AllArgsConstructor
@AutoRegisterPacket
@Builder
@Getter
@NoArgsConstructor
@Setter
public class ServerErrorPacket extends CommunicationPacket {

   private String source;        // guaranteed
   private int code;             // guaranteed
   private String name;          // guaranteed
   private int severity;         // guaranteed

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.ERROR;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Error.;
    }

    @Override
    public Packet deserialize(String yamlString) throws PacketManager.DeserializationException {
        ServerErrorPacket packet = ServerErrorPacket.builder().build();
        YamlConfiguration yaml;
        try {
            yaml = loadYAML(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new PacketManager.DeserializationException(e);
        }

        ensureKeyExists(Constants.Communication.YAML.Keys.Error.NAME, yaml);
        packet.name = yaml.getString(Constants.Communication.YAML.Keys.Error.NAME);

        ensureKeyExists(Constants.Communication.YAML.Keys.Error.CODE, yaml);
        packet.code = yaml.getInt(Constants.Communication.YAML.Keys.Error.CODE);

        ensureKeyExists(Constants.Communication.YAML.Keys.Error.SEVERITY, yaml);
        packet.severity = yaml.getInt(Constants.Communication.YAML.Keys.Error.SEVERITY);

        ensureKeyExists(Constants.Communication.YAML.Keys.Error.SOURCE, yaml);
        packet.source = yaml.getString(Constants.Communication.YAML.Keys.Error.SOURCE);

        return packet;
    }

    @Override
    public String serialize() {
        YamlConfiguration yaml = saveYAML();

        yaml.set(Constants.Communication.YAML.Keys.Error.NAME, name);
        yaml.set(Constants.Communication.YAML.Keys.Error.CODE, code);
        yaml.set(Constants.Communication.YAML.Keys.Error.SEVERITY, severity);
        yaml.set(Constants.Communication.YAML.Keys.Error.SOURCE, source);

        return yaml.saveToString();
    }

    @Override
    public void handlePacket() {
        LEDSuiteApplication.getLogger().warn(toString(), new LEDSuiteLogAreas.COMMUNICATION());
    }
}
