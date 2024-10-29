package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.AutoRegisterPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.PacketManager;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.enums.FileState;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.enums.LidState;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.StatusRequestPacket;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog.StatusUpdate;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.ConfigurationSection;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import lombok.*;

import java.util.*;

/**
 * <strong>Meaning:</strong><br>
 * Current status of server.
 * Used to update values of status dialog.
 * @since 1.0.0
 * @see StatusRequestPacket
 */
@AllArgsConstructor
@AutoRegisterPacket
@Builder
@Getter
@NoArgsConstructor
@Setter
public class StatusReplyPacket extends CommunicationPacket {

    public record InteractiveAnimation(String id, String label, String iconName, boolean pauseable) {}

    private FileState fileState;                        // guaranteed
    private String selectedFile;
    private Double currentDraw;                    // not guaranteed
    private Double voltage;
    private LidState lidState;                           // not guaranteed
    private List<InteractiveAnimation> animations;     // only available
    private boolean animationsAvailable;


    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REPLY;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Reply.Types.STATUS;
    }

    @Override
    public Packet deserialize(String yamlString) throws PacketManager.DeserializationException {
        StatusReplyPacket packet = StatusReplyPacket.builder().build();
        YamlConfiguration yaml;
        try {
            yaml = loadYAML(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new PacketManager.DeserializationException(e);
        }

        ensureKeyExists(Constants.Communication.YAML.Keys.Reply.StatusReply.FILE_STATE, yaml);
        try {
            packet.fileState = FileState.valueOf(yaml.getString(Constants.Communication.YAML.Keys.Reply.StatusReply.FILE_STATE));
        } catch (IllegalArgumentException e) {
            throw new PacketManager.DeserializationException(e);
        }

        if (!packet.fileState.equals(FileState.idle)) packet.selectedFile = yaml.getString(Constants.Communication.YAML.Keys.Reply.StatusReply.SELECTED_FILE);
        else packet.selectedFile = "";

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.StatusReply.CURRENT_DRAW, yaml)) {
            packet.currentDraw = yaml.getDouble(Constants.Communication.YAML.Keys.Reply.StatusReply.CURRENT_DRAW);
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.StatusReply.VOLTAGE, yaml)) {
            packet.voltage = yaml.getDouble(Constants.Communication.YAML.Keys.Reply.StatusReply.VOLTAGE);
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.StatusReply.LID_STATE, yaml)) {
            packet.lidState = LidState.fromBool(yaml.getBoolean(Constants.Communication.YAML.Keys.Reply.StatusReply.LID_STATE));
        }

        packet.animations = new ArrayList<>();

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.StatusReply.ANIMATIONS, yaml)) {

            ensureKeyExists(Constants.Communication.YAML.Keys.Reply.StatusReply.ANIMATIONS, yaml);
            ConfigurationSection animationsSection = yaml.getConfigurationSection(Constants.Communication.YAML.Keys.Reply.StatusReply.ANIMATIONS);

            if (animationsSection == null) throw new PacketManager.DeserializationException("Deserialization failed! ", new NullPointerException(Constants.Communication.YAML.Keys.Reply.StatusReply.ANIMATIONS + " wasn't found!"));
            for (String key : animationsSection.getKeys(false)) {

                ensureKeyExists(key + "." + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.LABEL, yaml);
                ensureKeyExists(key + "." + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.ICON, yaml);
                ensureKeyExists(key + "." + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.PAUSEABLE, yaml);

                packet.animations.add(new InteractiveAnimation(
                        key,
                        animationsSection.getString(key + "." + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.LABEL),
                        animationsSection.getString(key + "." + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.ICON),
                        animationsSection.getBoolean(key + "." + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.PAUSEABLE)
                ));
            }
        } else packet.animationsAvailable = false;

        return packet;
    }

    @Override
    public String serialize() {
        YamlConfiguration yaml = saveYAML();

        // Set the object's state into the YAML structure using the same keys as in deserializing
        yaml.set(Constants.Communication.YAML.Keys.Reply.StatusReply.FILE_STATE, fileState.name());
        if (!fileState.equals(FileState.idle)) yaml.set(Constants.Communication.YAML.Keys.Reply.StatusReply.SELECTED_FILE, selectedFile);
        if (currentDraw != null) yaml.set(Constants.Communication.YAML.Keys.Reply.StatusReply.CURRENT_DRAW, currentDraw);
        if (voltage != null) yaml.set(Constants.Communication.YAML.Keys.Reply.StatusReply.VOLTAGE, voltage);
        if (lidState != null) yaml.set(Constants.Communication.YAML.Keys.Reply.StatusReply.LID_STATE, lidState.asBool());

        // Save the list of animations
        if (animationsAvailable && animations != null && !animations.isEmpty()) {
            for (int i = 0; i < animations.size(); i++) {
                InteractiveAnimation animation = animations.get(i);
                String baseKey = Constants.Communication.YAML.Keys.Reply.StatusReply.ANIMATIONS + "." + i;

                yaml.set(baseKey + "." + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.LABEL, animation.label);
                yaml.set(baseKey + "." + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.ICON, animation.iconName);
                yaml.set(baseKey + "." + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.PAUSEABLE, animation.pauseable);
            }
        }

        // Convert the YAML configuration to a string and return it
        return yaml.saveToString();
    }


    @Override
    public void handlePacket() {

        LEDSuiteApplication.getLogger().info(this.toString(), new LEDSuiteLogAreas.COMMUNICATION());

        var statusUpdater = LEDSuiteApplication.getWindow().getStatusDialogUpdateCallback();

        if (statusUpdater != null) {

            statusUpdater.update(
                    StatusUpdate.builder()
                            .fileState(fileState)
                            .lidState(lidState)
                            .currentDraw(currentDraw)
                            .voltage(voltage)
                            .currentFile(fileState.equals(FileState.idle) ? null : selectedFile)
                            .build()
            );
            LEDSuiteApplication.getLogger().info("Updated status using provided status updater!", new LEDSuiteLogAreas.COMMUNICATION());
        } else LEDSuiteApplication.getLogger().info("Couldn't update status because no status updater is currently available!", new LEDSuiteLogAreas.COMMUNICATION());
    }
}
