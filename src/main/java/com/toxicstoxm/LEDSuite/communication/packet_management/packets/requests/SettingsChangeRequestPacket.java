package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.PacketManager;
import com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog.SettingsData;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Builder
@Getter
@Setter
public class SettingsChangeRequestPacket extends CommunicationPacket {

    private Integer brightness;
    private String selectedColorMode;

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
                .build();
    }

    @Override
    public Packet deserialize(String yamlString) throws PacketManager.DeserializationException {
        SettingsChangeRequestPacket packet = SettingsChangeRequestPacket.builder().build();
        YamlConfiguration yaml;
        try {
            yaml = loadYAML(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new PacketManager.DeserializationException(e);
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Request.SettingsChangeRequest.BRIGHTNESS, yaml)) {
            packet.brightness = (Integer) yaml.get(Constants.Communication.YAML.Keys.Request.SettingsChangeRequest.BRIGHTNESS);
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Request.SettingsChangeRequest.SELECTED_COLOR_MODE, yaml)) {
            packet.selectedColorMode = yaml.getString(Constants.Communication.YAML.Keys.Request.SettingsChangeRequest.SELECTED_COLOR_MODE);
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

        return yaml.saveToString();
    }
}