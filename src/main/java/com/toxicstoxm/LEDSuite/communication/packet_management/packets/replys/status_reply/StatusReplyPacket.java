package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.status_reply;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.ErrorCode;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.StatusRequestPacket;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog.StatusUpdate;
import com.toxicstoxm.YAJSI.api.yaml.ConfigurationSection;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <strong>Meaning:</strong><br>
 * Current status of server.
 * Used to update values of status dialog.
 * @since 1.0.0
 * @see StatusRequestPacket
 */
@AllArgsConstructor
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@NoArgsConstructor
@Setter
public class StatusReplyPacket extends CommunicationPacket {

    public record Animation(String id, String label, String iconName, boolean pauseable) {}

    private FileState fileState;                        // guaranteed
    private String selectedFile;                        // not guaranteed
    private Double currentDraw;                         // not guaranteed
    private Double voltage;                             // not guaranteed
    private LidState lidState;                          // not guaranteed
    private List<Animation> animations;                 // only if available
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
    public Packet deserialize(String yamlString) throws DeserializationException {
        super.deserialize(yamlString);
        StatusReplyPacket packet = StatusReplyPacket.builder().build();

        ensureKeyExists(Constants.Communication.YAML.Keys.Reply.StatusReply.FILE_STATE);
        try {
            packet.fileState = FileState.valueOf(yaml.getString(Constants.Communication.YAML.Keys.Reply.StatusReply.FILE_STATE));
        } catch (IllegalArgumentException e) {
            throw new DeserializationException(e, ErrorCode.InvalidFileState);
        }

        if (!packet.fileState.equals(FileState.idle)) packet.selectedFile = yaml.getString(Constants.Communication.YAML.Keys.Reply.StatusReply.SELECTED_FILE);
        else packet.selectedFile = "";

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.StatusReply.CURRENT_DRAW)) {
            packet.currentDraw = yaml.getDouble(Constants.Communication.YAML.Keys.Reply.StatusReply.CURRENT_DRAW);
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.StatusReply.VOLTAGE)) {
            packet.voltage = yaml.getDouble(Constants.Communication.YAML.Keys.Reply.StatusReply.VOLTAGE);
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.StatusReply.LID_STATE)) {
            packet.lidState = LidState.fromBool(yaml.getBoolean(Constants.Communication.YAML.Keys.Reply.StatusReply.LID_STATE));
        }

        packet.animations = new ArrayList<>();

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.StatusReply.ANIMATIONS)) {
            ConfigurationSection animationsSection = yaml.getConfigurationSection(Constants.Communication.YAML.Keys.Reply.StatusReply.ANIMATIONS);
            if (animationsSection == null) throw new DeserializationException("Deserialization failed! Failed to deserialize " + Constants.Communication.YAML.Keys.Reply.StatusReply.ANIMATIONS + " section!", ErrorCode.StatusUpdateInvalidAnimationsSection);

            for (String key : animationsSection.getKeys(false)) {

                String base = key + ".";

                ensureKeyExists(base + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.LABEL, animationsSection);
                ensureKeyExists(base + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.ICON, animationsSection);
                ensureKeyExists(base + Constants.Communication.YAML.Keys.General.FILE_NAME, animationsSection);
                ensureKeyExists(base + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.PAUSEABLE, animationsSection);

                packet.animations.add(new Animation(
                        animationsSection.getString(base + Constants.Communication.YAML.Keys.General.FILE_NAME),
                        animationsSection.getString(base + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.LABEL),
                        animationsSection.getString(base + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.ICON),
                        animationsSection.getBoolean(base + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.PAUSEABLE)
                ));
            }
        } else packet.animationsAvailable = false;

        return packet;
    }

    @Override
    public String serialize() {
        super.serialize();

        // Set the object's state into the YAML structure using the same keys as in deserializing
        yaml.set(Constants.Communication.YAML.Keys.Reply.StatusReply.FILE_STATE, fileState.name());
        if (!fileState.equals(FileState.idle)) yaml.set(Constants.Communication.YAML.Keys.Reply.StatusReply.SELECTED_FILE, selectedFile);
        yaml.set(Constants.Communication.YAML.Keys.Reply.StatusReply.CURRENT_DRAW, currentDraw);
        yaml.set(Constants.Communication.YAML.Keys.Reply.StatusReply.VOLTAGE, voltage);
        yaml.set(Constants.Communication.YAML.Keys.Reply.StatusReply.LID_STATE, lidState.asBool());

        // Save the list of animations
        if (animationsAvailable && animations != null && !animations.isEmpty()) {
            for (Animation animation : animations) {
                String baseKey = Constants.Communication.YAML.Keys.Reply.StatusReply.ANIMATIONS + "." + UUID.randomUUID() + ".";

                yaml.set(baseKey + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.LABEL, animation.label);
                yaml.set(baseKey + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.ICON, animation.iconName);
                yaml.set(baseKey + Constants.Communication.YAML.Keys.General.FILE_NAME, animation.id);
                yaml.set(baseKey + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.PAUSEABLE, animation.pauseable);
            }
        }

        // Convert the YAML configuration to a string and return it
        return yaml.saveToString();
    }


    @Override
    public void handlePacket() {

        var statusDialogEndpoint = LEDSuiteApplication.getWindow().getStatusDialog();

        if (statusDialogEndpoint != null) {

            statusDialogEndpoint.updater().update(
                    StatusUpdate.builder()
                            .fileState(fileState)
                            .lidState(lidState)
                            .currentDraw(currentDraw)
                            .voltage(voltage)
                            .currentFile(fileState.equals(FileState.idle) ? null : selectedFile)
                            .build()
            );
            LEDSuiteApplication.getLogger().verbose("Updated status using provided status updater!", new LEDSuiteLogAreas.COMMUNICATION());

            if (animationsAvailable || animations == null) {
                animations = new ArrayList<>();
            }

        } else LEDSuiteApplication.getLogger().debug("Couldn't update status because no status updater is currently available!", new LEDSuiteLogAreas.COMMUNICATION());

        LEDSuiteApplication.getWindow().updateAnimations(animations);
        LEDSuiteApplication.getWindow().setAnimationControlButtonsState(fileState);

    }
}
