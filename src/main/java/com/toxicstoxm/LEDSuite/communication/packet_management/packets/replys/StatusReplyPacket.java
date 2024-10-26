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
import lombok.Getter;

import java.util.*;

@Builder
@Getter
public class StatusReplyPacket extends CommunicationPacket {

    public record InteractiveAnimation(String id, String label, String iconName, boolean pauseable) {}

    private boolean isFileLoaded;                    // guaranteed
    private String fileState;                        // guaranteed
    private String selectedFile;                     // only if isFileLoaded is true
    private double currentDraw;
    private boolean currentDrawAvailable;            // not guaranteed
    private double voltage;
    private boolean voltageAvailable;                // not guaranteed
    private boolean lidState;
    private boolean lidStateAvailable;               // not guaranteed
    private List<InteractiveAnimation> animations;   // only available
    private boolean animationsAvailable;


    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.PacketTypes.REPLY;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.ReplyTypes.STATUS;
    }

    @Override
    public Packet deserialize(String yamlString) throws PacketManager.DeserializationException {
        YamlConfiguration yaml;
        try {
            yaml = loadYAML(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new PacketManager.DeserializationException(e);
        }

        ensureKeyExists(Constants.Communication.YAML.Keys.Status.IS_FILE_LOADED, yaml);
        isFileLoaded = yaml.getBoolean(Constants.Communication.YAML.Keys.Status.IS_FILE_LOADED);

        ensureKeyExists(Constants.Communication.YAML.Keys.Status.FILE_STATE, yaml);
        fileState = yaml.getString(Constants.Communication.YAML.Keys.Status.FILE_STATE);

        if (isFileLoaded) selectedFile = yaml.getString(Constants.Communication.YAML.Keys.Status.SELECTED_FILE);
        else selectedFile = "";

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Status.CURRENT_DRAW, yaml)) {
            currentDraw = yaml.getDouble(Constants.Communication.YAML.Keys.Status.CURRENT_DRAW);
        } else currentDrawAvailable = false;

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Status.VOLTAGE, yaml)) {
            voltage = yaml.getDouble(Constants.Communication.YAML.Keys.Status.VOLTAGE);
        } else voltageAvailable = false;

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Status.LID_STATE, yaml)) {
            lidState = yaml.getBoolean(Constants.Communication.YAML.Keys.Status.LID_STATE);
        } lidStateAvailable = false;

        animations = new ArrayList<>();

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Status.ANIMATIONS, yaml)) {

            ensureKeyExists(Constants.Communication.YAML.Keys.Status.ANIMATIONS, yaml);
            ConfigurationSection animationsSection = yaml.getConfigurationSection(Constants.Communication.YAML.Keys.Status.ANIMATIONS);

            if (animationsSection == null) throw new PacketManager.DeserializationException("Deserialization failed! ", new NullPointerException(Constants.Communication.YAML.Keys.Status.ANIMATIONS + " wasn't found!"));
            for (String key : animationsSection.getKeys(false)) {

                ensureKeyExists(key + "." + Constants.Communication.YAML.Keys.Status.AnimationList.LABEL, yaml);
                ensureKeyExists(key + "." + Constants.Communication.YAML.Keys.Status.AnimationList.ICON, yaml);
                ensureKeyExists(key + "." + Constants.Communication.YAML.Keys.Status.AnimationList.PAUSEABLE, yaml);

                animations.add(new InteractiveAnimation(
                        key,
                        animationsSection.getString(key + "." + Constants.Communication.YAML.Keys.Status.AnimationList.LABEL),
                        animationsSection.getString(key + "." + Constants.Communication.YAML.Keys.Status.AnimationList.ICON),
                        animationsSection.getBoolean(key + "." + Constants.Communication.YAML.Keys.Status.AnimationList.PAUSEABLE)
                ));
            }
        } else animationsAvailable = false;

        return this;
    }

    @Override
    public String serialize() {
        YamlConfiguration yaml = saveYAML();

        // Set the object's state into the YAML structure using the same keys as in deserializing
        yaml.set(Constants.Communication.YAML.Keys.Status.IS_FILE_LOADED, isFileLoaded);
        yaml.set(Constants.Communication.YAML.Keys.Status.FILE_STATE, fileState);
        if (isFileLoaded) yaml.set(Constants.Communication.YAML.Keys.Status.SELECTED_FILE, selectedFile);
        if (currentDrawAvailable) yaml.set(Constants.Communication.YAML.Keys.Status.CURRENT_DRAW, currentDraw);
        if (voltageAvailable) yaml.set(Constants.Communication.YAML.Keys.Status.VOLTAGE, voltage);
        if (lidStateAvailable) yaml.set(Constants.Communication.YAML.Keys.Status.LID_STATE, lidState);

        // Save the list of animations
        if (animationsAvailable && animations != null && !animations.isEmpty()) {
            for (int i = 0; i < animations.size(); i++) {
                InteractiveAnimation animation = animations.get(i);
                String baseKey = Constants.Communication.YAML.Keys.Status.ANIMATIONS + "." + i;

                yaml.set(baseKey + "." + Constants.Communication.YAML.Keys.Status.AnimationList.LABEL, animation.label);
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
