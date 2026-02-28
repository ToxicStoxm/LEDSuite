package com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.server_error;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.gettext.Translations;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs.ErrorData;
import com.toxicstoxm.YAJL.core.Logger;
import com.toxicstoxm.StormYAML.file.YamlConfiguration;
import com.toxicstoxm.StormYAML.yaml.InvalidConfigurationException;
import com.toxicstoxm.YAJL.core.LoggerManager;
import lombok.*;

/**
 * Information about a server-side error.
 * The user should be notified in some way when receiving such a packet.
 * @since 1.0.0
 */
@AllArgsConstructor
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@NoArgsConstructor
@Setter
public class ServerErrorPacket extends CommunicationPacket {
    private static final Logger logger = LoggerManager.getLogger(ServerErrorPacket.class);

    @Override
    public Logger getLogger() {
        return logger;
    }

    private String source;        // guaranteed
    private String message;          // guaranteed

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.ERROR;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Error.Types.SERVER;
    }

    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        ServerErrorPacket packet = ServerErrorPacket.builder().build();
        YamlConfiguration yaml;
        try {
            yaml = loadYAML(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new DeserializationException("Invalid YAML format", e);
        }

        ensureKeyExists(Constants.Communication.YAML.Keys.Error.ServerError.MESSAGE, yaml);
        packet.message = yaml.getString(Constants.Communication.YAML.Keys.Error.ServerError.MESSAGE);

        ensureKeyExists(Constants.Communication.YAML.Keys.Error.ServerError.SOURCE, yaml);
        packet.source = yaml.getString(Constants.Communication.YAML.Keys.Error.ServerError.SOURCE);

        return packet;
    }

    @Override
    public String serialize() {
        YamlConfiguration yaml = saveYAML();

        yaml.set(Constants.Communication.YAML.Keys.Error.ServerError.MESSAGE, message);
        yaml.set(Constants.Communication.YAML.Keys.Error.ServerError.SOURCE, source);

        return yaml.saveToString();
    }

    @Override
    public void handlePacket() {
        logger.warn("Server Error: Source: {}", source);

        LEDSuiteApplication.handleError(
                ErrorData.builder()
                        .message(message)
                        .heading(Translations.getText("Server encountered problems"))
                        .log(false)
                        .enableReporting(false)
                        .build()
        );
    }
}
