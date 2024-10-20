package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.PacketManager;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.ConfigurationSection;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import lombok.Builder;

import java.util.*;

@Builder
public class StatusReplyPacket extends CommunicationPacket {

    public record InteractiveAnimation(String id, String label, String iconName, boolean pauseable) {}

    private boolean isFileLoaded;
    private String fileState;
    public String selectedFile;
    public double currentDraw;
    public double voltage;
    public boolean lidState;
    public List<InteractiveAnimation> animations;

    @Override
    public String getType() {
        return "reply";
    }

    @Override
    public String getSubType() {
        return "status";
    }

    @Override
    public Packet deserialize(String yamlString) throws PacketManager.DeserializationException {
        YamlConfiguration yaml;
        try {
            yaml = loadYAML(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new PacketManager.DeserializationException(e);
        }

        isFileLoaded = yaml.getBoolean(Constants.Communication.YAML.Keys.Status.IS_FILE_LOADED);
        fileState = yaml.getString(Constants.Communication.YAML.Keys.Status.FILE_STATE);
        if (isFileLoaded) selectedFile = yaml.getString(Constants.Communication.YAML.Keys.Status.SELECTED_FILE);
        else selectedFile = "";
        currentDraw = yaml.getDouble(Constants.Communication.YAML.Keys.Status.CURRENT_DRAW);
        voltage = yaml.getDouble(Constants.Communication.YAML.Keys.Status.VOLTAGE);
        lidState = yaml.getBoolean(Constants.Communication.YAML.Keys.Status.LID_STATE);
        animations = new ArrayList<>();

        ConfigurationSection animationsSection = yaml.getConfigurationSection(Constants.Communication.YAML.Keys.Status.ANIMATIONS);
        if (animationsSection != null) {
            for (String key : animationsSection.getKeys(false)) {
                animations.add(new InteractiveAnimation(
                        key,
                        animationsSection.getString(key + "." + Constants.Communication.YAML.Keys.Status.AnimationList.NAME),
                        animationsSection.getString(key + "." + Constants.Communication.YAML.Keys.Status.AnimationList.ICON),
                        animationsSection.getBoolean(key + "." + Constants.Communication.YAML.Keys.Status.AnimationList.PAUSEABLE)
                ));
            }
        }

        return this;
    }

    @Override
    public String serialize() {
        YamlConfiguration yaml = new YamlConfiguration();

        // Set the object's state into the YAML structure using the same keys as in deserialize
        yaml.set(Constants.Communication.YAML.Keys.Status.IS_FILE_LOADED, isFileLoaded);
        yaml.set(Constants.Communication.YAML.Keys.Status.FILE_STATE, fileState);
        yaml.set(Constants.Communication.YAML.Keys.Status.SELECTED_FILE, isFileLoaded ? selectedFile : "");
        yaml.set(Constants.Communication.YAML.Keys.Status.CURRENT_DRAW, currentDraw);
        yaml.set(Constants.Communication.YAML.Keys.Status.VOLTAGE, voltage);
        yaml.set(Constants.Communication.YAML.Keys.Status.LID_STATE, lidState);

        // Save the list of animations
        if (animations != null && !animations.isEmpty()) {
            for (int i = 0; i < animations.size(); i++) {
                InteractiveAnimation animation = animations.get(i);
                String baseKey = Constants.Communication.YAML.Keys.Status.ANIMATIONS + "." + i;

                yaml.set(baseKey + "." + Constants.Communication.YAML.Keys.Status.AnimationList.NAME, animation.label);
                yaml.set(baseKey + "." + Constants.Communication.YAML.Keys.Status.AnimationList.ICON, animation.iconName);
                yaml.set(baseKey + "." + Constants.Communication.YAML.Keys.Status.AnimationList.PAUSEABLE, animation.pauseable);
            }
        }

        // Convert the YAML configuration to a string and return it
        return yaml.saveToString();
    }


    @Override
    public void handlePacket() {
        LEDSuiteApplication.getLogger().info(toString(), new LEDSuiteLogAreas.COMMUNICATION());
    }
}
