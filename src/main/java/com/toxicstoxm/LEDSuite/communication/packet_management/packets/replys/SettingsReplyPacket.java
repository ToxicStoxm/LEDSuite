package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.MenuRequestPacket;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog.SettingsUpdate;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import lombok.*;
import org.gnome.gtk.Gtk;

import java.util.Collection;

/**
 * <strong>Meaning:</strong><br>
 * Current server settings used to update the setting dialogs values.
 * @since 1.0.0
 * @see MenuRequestPacket
 */
@AllArgsConstructor
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@NoArgsConstructor
@Setter
public class SettingsReplyPacket extends CommunicationPacket {

    private Integer brightness;
    private String selectedColorMode;
    private Boolean restorePreviousState;
    private Collection<String> availableColorModes;

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REPLY;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Reply.Types.SETTINGS;
    }

    @Override
    public String serialize() {
        YamlConfiguration yaml = saveYAML();

        if (brightness != null) {
            yaml.set(Constants.Communication.YAML.Keys.Reply.SettingsReply.BRIGHTNESS, brightness);
        }

        if (selectedColorMode != null) {
            yaml.set(Constants.Communication.YAML.Keys.Reply.SettingsReply.SELECTED_COLOR_MODE, selectedColorMode);
        }

        if (restorePreviousState != null) {
            yaml.set(Constants.Communication.YAML.Keys.Reply.SettingsReply.RESTORE_PREVIOUS_STATE_ON_BOOT, restorePreviousState);
        }

        if (availableColorModes != null && !availableColorModes.isEmpty()) {
            yaml.set(Constants.Communication.YAML.Keys.Reply.SettingsReply.AVAILABLE_COLOR_MODES, availableColorModes);
        }

        return yaml.saveToString();
    }

    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        SettingsReplyPacket packet = SettingsReplyPacket.builder().build();
        YamlConfiguration yaml;
        try {
            yaml = loadYAML(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new DeserializationException(e);
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.SettingsReply.BRIGHTNESS, yaml)) {
            packet.brightness = (Integer) yaml.get(Constants.Communication.YAML.Keys.Reply.SettingsReply.BRIGHTNESS);
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.SettingsReply.SELECTED_COLOR_MODE, yaml)) {
            packet.selectedColorMode = yaml.getString(Constants.Communication.YAML.Keys.Reply.SettingsReply.SELECTED_COLOR_MODE);
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.SettingsReply.AVAILABLE_COLOR_MODES, yaml)) {
            packet.availableColorModes = yaml.getStringList(Constants.Communication.YAML.Keys.Reply.SettingsReply.AVAILABLE_COLOR_MODES);
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.SettingsReply.RESTORE_PREVIOUS_STATE_ON_BOOT, yaml)) {
            packet.restorePreviousState = yaml.getBoolean(Constants.Communication.YAML.Keys.Reply.SettingsReply.RESTORE_PREVIOUS_STATE_ON_BOOT);
        }

        return packet;
    }

    @Override
    public void handlePacket() {

        var settingsDialog = LEDSuiteApplication.getWindow().getSettingsDialog();
        if (settingsDialog != null) {
            settingsDialog.updater().update(
                    SettingsUpdate.builder()
                            .brightness(brightness)
                            .selectedColorMode(availableColorModes != null ? availableColorModes.stream().toList().indexOf(selectedColorMode) : Gtk.ACCESSIBLE_VALUE_UNDEFINED)
                            .supportedColorModes(availableColorModes)
                            .restorePreviousState(restorePreviousState)
                            .build()
            );
            LEDSuiteApplication.getLogger().info("Updated settings using provided settings updater!", new LEDSuiteLogAreas.COMMUNICATION());
        } else LEDSuiteApplication.getLogger().info("Couldn't update settings because no settings updater is currently available!", new LEDSuiteLogAreas.COMMUNICATION());

    }
}
