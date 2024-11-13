package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.upload_reply;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.FileUploadRequestPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.RenameRequestPacket;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialog.OverwriteConfirmationDialog;
import com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialog.RenameDialog;
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
        super.deserialize(yamlString);
        UploadFileCollisionReplyPacket packet = UploadFileCollisionReplyPacket.builder().build();

        ensureKeyExists(Constants.Communication.YAML.Keys.General.FILE_NAME);
        packet.currentName = yaml.getString(Constants.Communication.YAML.Keys.General.FILE_NAME);

        return packet;
    }

    @Override
    public String serialize() {
        super.serialize();

        yaml.set(Constants.Communication.YAML.Keys.General.FILE_NAME, currentName);

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
                    var renameDialog = RenameDialog.create(currentName);

                    renameDialog.onResponse(renameResponse -> {
                        String newName = renameDialog.getNewName();

                        if (renameResponse.equals("cancel")) {
                            new LEDSuiteRunnable() {
                                @Override
                                public void run() {
                                    GLib.idleAddOnce(() -> handlePacket());
                                }
                            }.runTaskLaterAsynchronously(100);
                        } else if (renameResponse.equals("rename")) {
                            if (newName == null || newName.isBlank() || newName.equals(currentName)) throw new RuntimeException("New file name invalid '" + newName + "'!");

                            LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                                    RenameRequestPacket.builder()
                                            .requestFile(currentName)
                                            .newName(newName)
                                            .build().serialize()
                            );

                        }
                    });

                    GLib.idleAddOnce(() -> renameDialog.present(LEDSuiteApplication.getWindow()));
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

                    GLib.idleAddOnce(() -> confirmationDialog.present(LEDSuiteApplication.getWindow()));
                }
            }

        }, "Animation with name '" + currentName + "' already exists.");
    }
}
