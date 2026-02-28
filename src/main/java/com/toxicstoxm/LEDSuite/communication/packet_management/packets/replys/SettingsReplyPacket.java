package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.MenuRequestPacket;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog.SettingsUpdate;
import com.toxicstoxm.YAJL.core.Logger;
import com.toxicstoxm.YAJL.core.LoggerManager;
import lombok.*;
import org.gnome.gtk.Gtk;

import java.util.Collection;

/**
 * Represents the current server settings, used to update the settings dialog values in the application.
 *
 * <p><strong>Usage:</strong><br>
 * Typically sent as a reply to {@link MenuRequestPacket}, this packet contains settings such as brightness,
 * selected color mode, available color modes, and whether to restore the previous state on boot.</p>
 *
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
    private static final Logger logger = LoggerManager.getLogger(SettingsReplyPacket.class);

    @Override
    public Logger getLogger() {
        return logger;
    }

    /**
     * The brightness level of the system.
     */
    private Integer brightness;

    /**
     * The currently selected color mode.
     */
    private String selectedColorMode;

    /**
     * Whether to restore the previous state on boot.
     */
    private Boolean restorePreviousState;

    /**
     * A collection of available color modes.
     */
    private Collection<String> availableColorModes;

    /**
     * Specifies the type of the packet, identifying it as a reply.
     *
     * @return the packet type as a string
     */
    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REPLY;
    }

    /**
     * Specifies the subtype of the packet, identifying it as a settings reply.
     *
     * @return the packet subtype as a string
     */
    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Reply.Types.SETTINGS;
    }

    /**
     * Deserializes the YAML string into a {@link SettingsReplyPacket} object.
     *
     * @param yamlString the YAML string to deserialize
     * @return the deserialized {@link SettingsReplyPacket}
     * @throws DeserializationException if required keys are missing or invalid
     */
    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        super.deserialize(yamlString);

        brightness = yaml.getInt(Constants.Communication.YAML.Keys.Reply.SettingsReply.BRIGHTNESS);
        selectedColorMode = yaml.getString(Constants.Communication.YAML.Keys.Reply.SettingsReply.SELECTED_COLOR_MODE);
        availableColorModes = yaml.getStringList(Constants.Communication.YAML.Keys.Reply.SettingsReply.AVAILABLE_COLOR_MODES);
        restorePreviousState = yaml.getBoolean(Constants.Communication.YAML.Keys.Reply.SettingsReply.RESTORE_PREVIOUS_STATE_ON_BOOT);

        return this;
    }

    /**
     * Serializes the {@link SettingsReplyPacket} into a YAML string.
     *
     * @return the serialized YAML string
     */
    @Override
    public String serialize() {
        super.serialize();

        yaml.set(Constants.Communication.YAML.Keys.Reply.SettingsReply.BRIGHTNESS, brightness);
        yaml.set(Constants.Communication.YAML.Keys.Reply.SettingsReply.SELECTED_COLOR_MODE, selectedColorMode);
        yaml.set(Constants.Communication.YAML.Keys.Reply.SettingsReply.RESTORE_PREVIOUS_STATE_ON_BOOT, restorePreviousState);

        if (availableColorModes != null && !availableColorModes.isEmpty()) {
            yaml.set(Constants.Communication.YAML.Keys.Reply.SettingsReply.AVAILABLE_COLOR_MODES, availableColorModes);
        }

        return yaml.saveToString();
    }

    /**
     * Handles the packet by updating the application settings and logging the update.
     */
    @Override
    public void handlePacket() {
        int selectedColorModeIndex = Gtk.ACCESSIBLE_VALUE_UNDEFINED;
        if (availableColorModes != null && selectedColorMode != null) {
            selectedColorModeIndex = availableColorModes.stream().toList().indexOf(selectedColorMode);
        }

        LEDSuiteApplication.getWindow().update(
                SettingsUpdate.builder()
                        .brightness(brightness)
                        .selectedColorMode(selectedColorModeIndex)
                        .supportedColorModes(availableColorModes)
                        .restorePreviousState(restorePreviousState)
                        .build()
        );

        logger.verbose("Updated settings using the provided settings updater");
    }
}
