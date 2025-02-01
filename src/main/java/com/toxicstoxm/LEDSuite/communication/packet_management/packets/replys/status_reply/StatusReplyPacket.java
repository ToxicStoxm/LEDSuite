package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.status_reply;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.ErrorCode;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.StatusRequestPacket;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog.StatusUpdate;
import com.toxicstoxm.YAJL.Logger;
import com.toxicstoxm.YAJSI.api.yaml.ConfigurationSection;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents the current status of the server and is used to update values in the status dialog.
 * This packet contains information about file states, lid state, current draw, voltage, animations, and the username.
 *
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

    private static final Logger logger = Logger.autoConfigureLogger();

    @Override
    public Logger getLogger() {
        return logger;
    }

    /**
     * Represents an animation with details like id, label, icon, and pauseable state.
     */
    @Builder
    public record Animation(String id, String label, String iconString, boolean pauseable, boolean iconIsName, long lastAccessed) {}

    private FileState fileState;                        // Guaranteed field representing the file's state.
    private String selectedFile;                        // Not guaranteed, representing the selected file name.
    private Double currentDraw;                         // Not guaranteed, representing the current draw in amperes.
    private Double voltage;                             // Not guaranteed, representing the voltage.
    private LidState lidState;                          // Not guaranteed, representing the state of the lid.
    private List<Animation> animations;                 // Optional list of animations, only available if the key exists.
    private boolean animationsAvailable;                // Flag indicating whether animations are available.
    private String username;                            // Not guaranteed, represents the username.

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REPLY;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Reply.Types.STATUS;
    }

    /**
     * Deserializes the given YAML string into a {@link StatusReplyPacket} object.
     *
     * @param yamlString the YAML string to deserialize
     * @return the deserialized {@link StatusReplyPacket}
     * @throws DeserializationException if deserialization fails
     */
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

        // Handle optional fields (file state specific)
        if (!packet.fileState.equals(FileState.idle)) {
            packet.selectedFile = yaml.getString(Constants.Communication.YAML.Keys.Reply.StatusReply.SELECTED_FILE);
        } else {
            packet.selectedFile = "";
        }

        // Deserialize other optional fields
        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.StatusReply.CURRENT_DRAW)) {
            packet.currentDraw = yaml.getDouble(Constants.Communication.YAML.Keys.Reply.StatusReply.CURRENT_DRAW);
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.StatusReply.VOLTAGE)) {
            packet.voltage = yaml.getDouble(Constants.Communication.YAML.Keys.Reply.StatusReply.VOLTAGE);
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.StatusReply.LID_STATE)) {
            packet.lidState = LidState.fromBool(yaml.getBoolean(Constants.Communication.YAML.Keys.Reply.StatusReply.LID_STATE));
        }

        // Deserialize animations if available
        packet.animations = new ArrayList<>();
        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.StatusReply.ANIMATIONS)) {
            animationsAvailable = true;
            ConfigurationSection animationsSection = yaml.getConfigurationSection(Constants.Communication.YAML.Keys.Reply.StatusReply.ANIMATIONS);
            if (animationsSection == null) {
                throw new DeserializationException("Deserialization failed! Failed to deserialize " + Constants.Communication.YAML.Keys.Reply.StatusReply.ANIMATIONS + " section!", ErrorCode.StatusUpdateInvalidAnimationsSection);
            }

            for (String key : animationsSection.getKeys(false)) {
                String base = key + ".";
                // By default, the icon string is assumed to be a gnome icon name
                boolean iconIsName = true;
                String iconString = null;

                ensureKeyExists(base + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.LABEL, animationsSection);
                ensureKeyExists(base + Constants.Communication.YAML.Keys.General.FILE_NAME, animationsSection);
                ensureKeyExists(base + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.PAUSEABLE, animationsSection);
                ensureKeyExists(base + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.LAST_ACCESSED, animationsSection);

                // Check if an icon name or icon string (base64 encoded image file) was provided,
                // If yes load the according value into iconString and set iconIsName accordingly
                if (checkIfKeyExists(base + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.ICON_NAME, animationsSection)) {
                    iconString = animationsSection.getString(base + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.ICON_NAME);
                } else if (checkIfKeyExists(base + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.ICON, animationsSection)) {
                    iconString = animationsSection.getString(base + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.ICON);
                    iconIsName = false;
                }

                // Throw an error if no icon string or icon name was specified
                if (iconString == null || iconString.isBlank()) {
                    throw new DeserializationException("No icon name or icon string was specified for animation '" + key + "'!");
                }

                // Assemble animation from extracted values
                packet.animations.add(Animation.builder()
                        .id(animationsSection.getString(base + Constants.Communication.YAML.Keys.General.FILE_NAME))
                        .label(animationsSection.getString(base + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.LABEL))
                        .iconString(iconString)
                        .pauseable(animationsSection.getBoolean(base + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.PAUSEABLE))
                        .iconIsName(iconIsName)
                        .lastAccessed(animationsSection.getLong(base + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.LAST_ACCESSED))
                        .build()
                );
            }
        } else {
            packet.animationsAvailable = false;
        }

        // Deserialize username if present
        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.StatusReply.USERNAME)) {
            packet.username = yaml.getString(Constants.Communication.YAML.Keys.Reply.StatusReply.USERNAME);
        }

        return packet;
    }

    /**
     * Serializes the {@link StatusReplyPacket} to a YAML string.
     *
     * @return the serialized YAML string
     */
    @Override
    public String serialize() {
        super.serialize();

        // Set the object's state into the YAML structure using the same keys as in deserializing
        yaml.set(Constants.Communication.YAML.Keys.Reply.StatusReply.FILE_STATE, fileState.name());
        if (!fileState.equals(FileState.idle)) {
            yaml.set(Constants.Communication.YAML.Keys.Reply.StatusReply.SELECTED_FILE, selectedFile);
        }
        yaml.set(Constants.Communication.YAML.Keys.Reply.StatusReply.CURRENT_DRAW, currentDraw);
        yaml.set(Constants.Communication.YAML.Keys.Reply.StatusReply.VOLTAGE, voltage);
        yaml.set(Constants.Communication.YAML.Keys.Reply.StatusReply.LID_STATE, lidState.asBool());
        yaml.set(Constants.Communication.YAML.Keys.Reply.StatusReply.USERNAME, username);

        // Serialize the list of animations if available
        if (animationsAvailable && animations != null && !animations.isEmpty()) {
            for (Animation animation : animations) {
                String baseKey = Constants.Communication.YAML.Keys.Reply.StatusReply.ANIMATIONS + "." + UUID.randomUUID() + ".";

                yaml.set(baseKey + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.LABEL, animation.label);
                yaml.set(baseKey + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.ICON, animation.iconString);
                yaml.set(baseKey + Constants.Communication.YAML.Keys.General.FILE_NAME, animation.id);
                yaml.set(baseKey + Constants.Communication.YAML.Keys.Reply.StatusReply.AnimationList.PAUSEABLE, animation.pauseable);
            }
        }

        // Convert the YAML configuration to a string and return it
        return yaml.saveToString();
    }

    /**
     * Handles the status update by updating the UI with the latest server status.
     * This includes file state, lid state, current draw, voltage, animations, and username.
     */
    @Override
    public void handlePacket() {
        LEDSuiteApplication.getWindow().update(
                StatusUpdate.builder()
                        .fileState(fileState)
                        .lidState(lidState)
                        .currentDraw(currentDraw)
                        .voltage(voltage)
                        .currentFile(fileState.equals(FileState.idle) ? null : selectedFile)
                        .build()
        );

        if (animationsAvailable || animations == null) {
            animations = new ArrayList<>();
        }

        logger.verbose("Updated status using provided status updater!");

        LEDSuiteApplication.getWindow().updateAnimations(animations);
        LEDSuiteApplication.getWindow().setAnimationControlButtonsState(fileState, selectedFile);
    }
}
