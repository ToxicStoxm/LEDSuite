package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog.SettingsData;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import lombok.*;
import org.jetbrains.annotations.NotNull;

/**
 * <strong>Meaning:</strong><br>
 * Request for changing a setting from the settings dialog.
 * @since 1.0.0
 */
@AllArgsConstructor
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@NoArgsConstructor
@Setter
public class SettingsChangeRequestPacket extends CommunicationPacket {

    private Integer brightness;
    private String selectedColorMode;
    private Boolean restorePreviousState;

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REQUEST;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Request.Types.SETTINGS_CHANGE;
    }

    public static SettingsChangeRequestPacket fromSettingsData(@NotNull SettingsData settingsData) {
        return SettingsChangeRequestPacket.builder()
                .brightness(settingsData.brightness())
                .selectedColorMode(settingsData.selectedColorMode())
                .restorePreviousState(settingsData.restorePreviousState())
                .build();
    }

    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        SettingsChangeRequestPacket packet = SettingsChangeRequestPacket.builder().build();
        YamlConfiguration yaml;
        try {
            yaml = loadYAML(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new DeserializationException(e);
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Request.SettingsChangeRequest.BRIGHTNESS, yaml)) {
            packet.brightness = (Integer) yaml.get(Constants.Communication.YAML.Keys.Request.SettingsChangeRequest.BRIGHTNESS);
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Request.SettingsChangeRequest.SELECTED_COLOR_MODE, yaml)) {
            packet.selectedColorMode = yaml.getString(Constants.Communication.YAML.Keys.Request.SettingsChangeRequest.SELECTED_COLOR_MODE);
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Request.SettingsChangeRequest.RESTORE_PREVIOUS_STATE_ON_BOOT, yaml)) {
            packet.restorePreviousState = yaml.getBoolean(Constants.Communication.YAML.Keys.Request.SettingsChangeRequest.RESTORE_PREVIOUS_STATE_ON_BOOT);
        }

        return packet;
    }

    @Override
    public String serialize() {
        YamlConfiguration yaml = saveYAML();

        if (brightness != null) {
            yaml.set(Constants.Communication.YAML.Keys.Request.SettingsChangeRequest.BRIGHTNESS, brightness);
        }

        if (selectedColorMode != null && !selectedColorMode.isBlank()) {
            yaml.set(Constants.Communication.YAML.Keys.Request.SettingsChangeRequest.SELECTED_COLOR_MODE, selectedColorMode);
        }

        if (restorePreviousState != null) {
            yaml.set(Constants.Communication.YAML.Keys.Request.SettingsChangeRequest.RESTORE_PREVIOUS_STATE_ON_BOOT, restorePreviousState);
        }

        return yaml.saveToString();
    }
}
