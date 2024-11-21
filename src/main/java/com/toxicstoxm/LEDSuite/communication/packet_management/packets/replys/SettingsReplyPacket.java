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
    public Packet deserialize(String yamlString) throws DeserializationException {
        super.deserialize(yamlString);
        SettingsReplyPacket packet = SettingsReplyPacket.builder().build();

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.SettingsReply.BRIGHTNESS)) {
            packet.brightness = (Integer) yaml.get(Constants.Communication.YAML.Keys.Reply.SettingsReply.BRIGHTNESS);
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.SettingsReply.SELECTED_COLOR_MODE)) {
            packet.selectedColorMode = yaml.getString(Constants.Communication.YAML.Keys.Reply.SettingsReply.SELECTED_COLOR_MODE);
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.SettingsReply.AVAILABLE_COLOR_MODES)) {
            packet.availableColorModes = yaml.getStringList(Constants.Communication.YAML.Keys.Reply.SettingsReply.AVAILABLE_COLOR_MODES);
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.SettingsReply.RESTORE_PREVIOUS_STATE_ON_BOOT)) {
            packet.restorePreviousState = yaml.getBoolean(Constants.Communication.YAML.Keys.Reply.SettingsReply.RESTORE_PREVIOUS_STATE_ON_BOOT);
        }

        return packet;
    }

    @Override
    public String serialize() {
        super.serialize();

        yaml.set(Constants.Communication.YAML.Keys.Reply.SettingsReply.BRIGHTNESS, brightness);
        yaml.set(Constants.Communication.YAML.Keys.Reply.SettingsReply.SELECTED_COLOR_MODE, selectedColorMode);
        yaml.set(Constants.Communication.YAML.Keys.Reply.SettingsReply.RESTORE_PREVIOUS_STATE_ON_BOOT, restorePreviousState);

        if (!availableColorModes.isEmpty()) {
            yaml.set(Constants.Communication.YAML.Keys.Reply.SettingsReply.AVAILABLE_COLOR_MODES, availableColorModes);
        }

        return yaml.saveToString();
    }

    @Override
    public void handlePacket() {

        LEDSuiteApplication.getWindow().update(
                SettingsUpdate.builder()
                        .brightness(brightness)
                        .selectedColorMode(availableColorModes != null ? availableColorModes.stream().toList().indexOf(selectedColorMode) : Gtk.ACCESSIBLE_VALUE_UNDEFINED)
                        .supportedColorModes(availableColorModes)
                        .restorePreviousState(restorePreviousState)
                        .build()
        );
        LEDSuiteApplication.getLogger().info("Updated settings using provided settings updater!", new LEDSuiteLogAreas.COMMUNICATION());
    }
}
