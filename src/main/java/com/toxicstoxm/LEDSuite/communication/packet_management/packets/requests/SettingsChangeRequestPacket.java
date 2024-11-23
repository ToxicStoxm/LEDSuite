package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog.SettingsData;
import lombok.*;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a request to change settings from the settings dialog.
 * <p>
 * This packet is used when the user modifies the settings such as brightness, color mode,
 * or the option to restore the previous state on boot.
 * The packet is then sent to the server to apply those changes.
 * </p>
 *
 * <strong>Fields:</strong>
 * <ul>
 *     <li><b>brightness</b> - The desired brightness level.</li>
 *     <li><b>selectedColorMode</b> - The selected color mode for the display.</li>
 *     <li><b>restorePreviousState</b> - A flag indicating whether to restore the previous state on boot.</li>
 * </ul>
 *
 * @since 1.0.0
 */
@AllArgsConstructor
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@NoArgsConstructor
@Setter
public class SettingsChangeRequestPacket extends CommunicationPacket {

    /**
     * The desired brightness level.
     */
    private Integer brightness;

    /**
     * The selected color mode for the display.
     */
    private String selectedColorMode;

    /**
     * A flag indicating whether to restore the previous state on boot.
     */
    private Boolean restorePreviousState;

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REQUEST;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Request.Types.SETTINGS_CHANGE;
    }

    /**
     * Converts a {@link SettingsData} object into a {@code SettingsChangeRequestPacket}.
     * <p>
     * This method is used to easily create a packet from the settings data
     * provided by the settings dialog.
     * </p>
     *
     * @param settingsData the settings data object containing the user's settings.
     * @return a {@code SettingsChangeRequestPacket} populated with the provided settings data.
     */
    public static SettingsChangeRequestPacket fromSettingsData(@NotNull SettingsData settingsData) {
        return SettingsChangeRequestPacket.builder()
                .brightness(settingsData.brightness())
                .selectedColorMode(settingsData.selectedColorMode())
                .restorePreviousState(settingsData.restorePreviousState())
                .build();
    }

    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        super.deserialize(yamlString);
        SettingsChangeRequestPacket packet = SettingsChangeRequestPacket.builder().build();

        // Deserialize the brightness setting if the key exists in the YAML string
        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Request.SettingsChangeRequest.BRIGHTNESS)) {
            packet.brightness = (Integer) yaml.get(Constants.Communication.YAML.Keys.Request.SettingsChangeRequest.BRIGHTNESS);
        }

        // Deserialize the selected color mode if the key exists in the YAML string
        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Request.SettingsChangeRequest.SELECTED_COLOR_MODE)) {
            packet.selectedColorMode = yaml.getString(Constants.Communication.YAML.Keys.Request.SettingsChangeRequest.SELECTED_COLOR_MODE);
        }

        // Deserialize the restore previous state flag if the key exists in the YAML string
        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Request.SettingsChangeRequest.RESTORE_PREVIOUS_STATE_ON_BOOT)) {
            packet.restorePreviousState = yaml.getBoolean(Constants.Communication.YAML.Keys.Request.SettingsChangeRequest.RESTORE_PREVIOUS_STATE_ON_BOOT);
        }

        return packet;
    }

    @Override
    public String serialize() {
        super.serialize();

        // Serialize the brightness value into the YAML string
        yaml.set(Constants.Communication.YAML.Keys.Request.SettingsChangeRequest.BRIGHTNESS, brightness);

        // Serialize the selected color mode value into the YAML string if not blank
        if (!selectedColorMode.isBlank()) {
            yaml.set(Constants.Communication.YAML.Keys.Request.SettingsChangeRequest.SELECTED_COLOR_MODE, selectedColorMode);
        }

        // Serialize the restore previous state flag into the YAML string
        yaml.set(Constants.Communication.YAML.Keys.Request.SettingsChangeRequest.RESTORE_PREVIOUS_STATE_ON_BOOT, restorePreviousState);

        return yaml.saveToString();
    }
}
