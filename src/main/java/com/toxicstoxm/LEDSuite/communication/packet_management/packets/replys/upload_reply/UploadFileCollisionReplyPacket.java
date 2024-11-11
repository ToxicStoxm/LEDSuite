package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.upload_reply;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.ErrorCode;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.FileUploadRequestPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.RenameRequestPacket;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialog.OverwriteConfirmationDialog;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import lombok.*;
import org.gnome.glib.GLib;

/**
 * <strong>Meaning:</strong><br>
 * An already existing animation on the server has the same name as the file that is being uploaded currently.
 * The client should ask the user if the file should be renamed or overwritten and then notify the server of the users decision.
 * @since 1.0.0
 * @see FileUploadRequestPacket
 */
@AllArgsConstructor
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@NoArgsConstructor
@Setter
public class UploadFileCollisionReplyPacket extends CommunicationPacket {

    private String currentName;

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REPLY;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Reply.Types.UPLOAD_FILE_COLLISION_REPLY;
    }

    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        UploadFileCollisionReplyPacket packet = UploadFileCollisionReplyPacket.builder().build();
        YamlConfiguration yaml;
        try {
            yaml = loadYAML(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new DeserializationException(e, ErrorCode.FailedToParseYAML);
        }

        ensureKeyExists(Constants.Communication.YAML.Keys.Reply.UploadFileCollisionReply.FILE_NAME, yaml);
        packet.currentName = yaml.getString(Constants.Communication.YAML.Keys.Reply.UploadFileCollisionReply.FILE_NAME);

        return packet;
    }

    @Override
    public String serialize() {
        YamlConfiguration yaml = saveYAML();

        yaml.set(Constants.Communication.YAML.Keys.Reply.UploadFileCollisionReply.FILE_NAME, currentName);

        return yaml.saveToString();
    }

    @Override
    public void handlePacket() {
        LEDSuiteApplication.getLogger().info("File name collision occurred. Displaying options.", new LEDSuiteLogAreas.COMMUNICATION());

        LEDSuiteApplication.getWindow().displayFileCollisionDialog(response -> {
            LEDSuiteApplication.getLogger().info("File collision dialog response result: " + response, new LEDSuiteLogAreas.UI_CONSTRUCTION());

            switch (response) {
                case "cancel" -> {
                    LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                            RenameRequestPacket.builder()
                                    .requestFile(currentName)
                                    .newName("")
                                    .build().serialize());
                }
                case "rename" -> {
                    LEDSuiteApplication.getLogger().info("Rename selected.", new LEDSuiteLogAreas.USER_INTERACTIONS());
                }
                case "overwrite" -> {
                    var confirmationDialog = OverwriteConfirmationDialog.create();

                    confirmationDialog.onResponse(confirmationResponse -> {
                        if (confirmationResponse.equals("cancel")) {
                            new LEDSuiteRunnable() {
                                @Override
                                public void run() {
                                    GLib.idleAddOnce(() -> handlePacket());
                                }
                            }.runTaskLaterAsynchronously(100);

                        } else if (confirmationResponse.equals("yes")) {
                            LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                                    RenameRequestPacket.builder()
                                            .requestFile(currentName)
                                            .newName(currentName)
                                            .build().serialize());
                        }
                    });

                    confirmationDialog.present(LEDSuiteApplication.getWindow());
                }
            }

        }, "Animation with name '" + currentName + "' already exists.");
    }
}
