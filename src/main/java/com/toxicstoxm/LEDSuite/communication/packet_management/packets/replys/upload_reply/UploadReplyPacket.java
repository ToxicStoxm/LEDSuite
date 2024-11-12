package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.upload_reply;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.ErrorCode;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.FileUploadRequestPacket;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialog.OverwriteConfirmationDialog;
import com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialog.RenameDialog;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import lombok.*;
import org.gnome.glib.GLib;

@AllArgsConstructor
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@NoArgsConstructor
@Setter
public class UploadReplyPacket extends CommunicationPacket {

    private boolean uploadPermitted;
    private String fileName;

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REPLY;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Reply.Types.UPLOAD;
    }

    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        UploadReplyPacket packet = UploadReplyPacket.builder().build();
        YamlConfiguration yaml;
        try {
            yaml = loadYAML(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new DeserializationException(e, ErrorCode.FailedToParseYAML);
        }

        ensureKeyExists(Constants.Communication.YAML.Keys.Reply.UploadReply.FILE_NAME, yaml);
        packet.fileName = yaml.getString(Constants.Communication.YAML.Keys.Reply.UploadReply.FILE_NAME);

        ensureKeyExists(Constants.Communication.YAML.Keys.Reply.UploadReply.UPLOAD_PERMITTED, yaml);
        packet.uploadPermitted = yaml.getBoolean(Constants.Communication.YAML.Keys.Reply.UploadReply.UPLOAD_PERMITTED);

        return packet;
    }

    @Override
    public String serialize() {
        YamlConfiguration yaml = saveYAML();

        yaml.set(Constants.Communication.YAML.Keys.Reply.UploadReply.FILE_NAME, fileName);
        yaml.set(Constants.Communication.YAML.Keys.Reply.UploadReply.UPLOAD_PERMITTED, uploadPermitted);

        return yaml.saveToString();
    }

    @Override
    public void handlePacket() {
        if (!uploadPermitted) {
            LEDSuiteApplication.getLogger().info("File name collision occurred. Displaying options.", new LEDSuiteLogAreas.COMMUNICATION());

            LEDSuiteApplication.getWindow().displayFileCollisionDialog(response -> {
                LEDSuiteApplication.getLogger().info("File collision dialog response result: " + response, new LEDSuiteLogAreas.UI_CONSTRUCTION());

                switch (response) {
                    case "cancel" -> LEDSuiteApplication.getPendingUploads().remove(fileName);
                    case "rename" -> {
                        LEDSuiteApplication.getLogger().info("Rename selected.", new LEDSuiteLogAreas.USER_INTERACTIONS());
                        var renameDialog = RenameDialog.create(fileName);

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
                                if (newName == null || newName.isBlank() || newName.equals(fileName)) throw new RuntimeException("New file name invalid '" + newName + "'!");

                                LEDSuiteApplication.UploadAction action = LEDSuiteApplication.getPendingUploads().remove(fileName);
                                if (action != null) LEDSuiteApplication.getPendingUploads().put(newName, action);

                                LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                                        FileUploadRequestPacket.builder()
                                                .requestFile(newName)
                                                .sha256(LEDSuiteApplication.getPendingUploads().get(newName).checksum())
                                                .uploadSessionId(LEDSuiteApplication.getPendingUploads().get(newName).uploadID())
                                                .build().serialize());

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
                                        FileUploadRequestPacket.builder()
                                                .requestFile(fileName)
                                                .forceOverwrite(true)
                                                .sha256(LEDSuiteApplication.getPendingUploads().get(fileName).checksum())
                                                .uploadSessionId(LEDSuiteApplication.getPendingUploads().get(fileName).uploadID())
                                                .build().serialize());
                            }
                        });

                       GLib.idleAddOnce(() -> confirmationDialog.present(LEDSuiteApplication.getWindow()));
                    }
                }

            }, "Animation with name '" + fileName + "' already exists.");
        } else {
            LEDSuiteApplication.callPendingUpload(fileName);
        }
    }
}
