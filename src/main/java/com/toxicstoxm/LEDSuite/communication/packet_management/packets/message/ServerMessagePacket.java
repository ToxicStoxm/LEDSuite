package com.toxicstoxm.LEDSuite.communication.packet_management.packets.message;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.ServerMessageResponseRequestPacket;
import com.toxicstoxm.LEDSuite.tools.YamlTools;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs.AlertDialogResponse;
import com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs.ServerMessageDialog;
import com.toxicstoxm.YAJL.Logger;
import com.toxicstoxm.YAJSI.api.yaml.ConfigurationSection;
import lombok.*;
import org.gnome.adw.ResponseAppearance;

import java.util.ArrayList;
import java.util.List;

/**
 * This packet contains general information from the server or interactive animations that should be displayed to the user.
 * @since 1.0.0
 */
@AllArgsConstructor
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@NoArgsConstructor
@Setter
public class ServerMessagePacket extends CommunicationPacket {

    private static final Logger logger = Logger.autoConfigureLogger();

    @Override
    public Logger getLogger() {
        return logger;
    }

    private String message;
    private String heading;
    private String source;
    @Singular
    private List<AlertDialogResponse> responses;

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.MESSAGE;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Message.Types.SERVER;
    }

    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {

        setMessage(getStringIfAvailable(Constants.Communication.YAML.Keys.Message.ServerMessage.MESSAGE));
        heading = yaml.getString(Constants.Communication.YAML.Keys.Message.ServerMessage.HEADING);
        source =  yaml.getString(Constants.Communication.YAML.Keys.Message.ServerMessage.SOURCE);

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Message.ServerMessage.RESPONSES)) {
            ConfigurationSection responsesSection = yaml.getConfigurationSection(Constants.Communication.YAML.Keys.Message.ServerMessage.RESPONSES);
            if (responsesSection != null) {
                responses = new ArrayList<>();
                for (String key : responsesSection.getKeys(false)) {
                    responses.add(
                            AlertDialogResponse.builder()
                                    .id(key)
                                    .label(YamlTools.getStringIfAvailable(
                                            Constants.Communication.YAML.Keys.Message.ServerMessage.Responses.LABEL,
                                            responsesSection
                                    ))
                                    .activated(YamlTools.getBooleanIfAvailable(
                                            Constants.Communication.YAML.Keys.Message.ServerMessage.Responses.ACTIVE,
                                            responsesSection
                                    ))
                                    .appearance(ResponseAppearance.valueOf(
                                            YamlTools.getStringIfAvailable(
                                                    Constants.Communication.YAML.Keys.Message.ServerMessage.Responses.STYLE,
                                                    "default",
                                                    responsesSection
                                            ).toUpperCase()
                                    ))
                                    .responseCallback(() -> {
                                        LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                                                ServerMessageResponseRequestPacket.builder()
                                                        .responseID(key)
                                                        .build().serialize()
                                        );
                                    })
                                    .build()
                    );
                }
            }
        }

        return super.deserialize(yamlString);
    }

    @Override
    public String serialize() {
        super.serialize();

        yaml.set(Constants.Communication.YAML.Keys.Message.ServerMessage.MESSAGE, message);
        yaml.set(Constants.Communication.YAML.Keys.Message.ServerMessage.HEADING, heading);
        yaml.set(Constants.Communication.YAML.Keys.Message.ServerMessage.SOURCE, source);

        if (responses != null && !responses.isEmpty()) {
            ConfigurationSection responsesSection = yaml.createSection(Constants.Communication.YAML.Keys.Message.ServerMessage.RESPONSES);
            responses.forEach(response -> {
                ConfigurationSection responseSection = responsesSection.createSection(response.id());
                responseSection.set(Constants.Communication.YAML.Keys.Message.ServerMessage.Responses.LABEL, response.label());
                responseSection.set(Constants.Communication.YAML.Keys.Message.ServerMessage.Responses.ACTIVE, response.activated());
                responseSection.set(Constants.Communication.YAML.Keys.Message.ServerMessage.Responses.STYLE, response.appearance());
            });
        }

        return yaml.saveToString();
    }

    @Override
    public void handlePacket() {
        ServerMessageDialog.builder().packet(this).build().present(LEDSuiteApplication.getWindow().asApplicationWindow());
    }
}
