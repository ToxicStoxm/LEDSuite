package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.ErrorCode;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.menu_error.MenuErrorPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.menu_error.Severity;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.MenuRequestPacket;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.tools.YamlTools;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import lombok.*;
import org.gnome.adw.Clamp;
import org.gnome.adw.Spinner;
import org.gnome.gtk.Align;

/**
 * <strong>Meaning:</strong><br>
 * Structure and contents of an animation menu.
 * @since 1.0.0
 * @see MenuRequestPacket
 */
@AllArgsConstructor
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@NoArgsConstructor
@Setter
public class MenuReplyPacket extends CommunicationPacket {

    private String menuYAML;

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REPLY;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Reply.Types.MENU;
    }

    @Override
    public String serialize() {
        YamlConfiguration yaml = saveYAML();
        yaml.set(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT, menuYAML);
        return yaml.saveToString();
    }

    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        MenuReplyPacket packet = MenuReplyPacket.builder().build();
        YamlConfiguration yaml;
        try {
            yaml = loadYAML(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new DeserializationException(e);
        }

        //ensureKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT, yaml);
        packet.menuYAML = yaml.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT);
        return packet;
    }

    @Override
    public void handlePacket() {

        long start = System.currentTimeMillis();
        LEDSuiteApplication.getWindow().animationList.setSensitive(false);

        LEDSuiteApplication.getWindow().changeMainContent(
                Clamp.builder()
                        .setChild(Spinner.builder().build())
                        .setMaximumSize(50)
                        .setHalign(Align.CENTER)
                        .setHexpand(true)
                        .setTighteningThreshold(50)
                        .build()

        );

        new LEDSuiteRunnable() {
            @Override
            public void run() {

                ErrorCode errorCode = null;
                String errorMessage = null;

                var animationMenuManager = LEDSuiteApplication.getAnimationMenuManager();

                if (animationMenuManager != null) {
                    try {
                        LEDSuiteApplication.getWindow().displayAnimationManu(animationMenuManager.deserializeAnimationMenu(menuYAML));
                    } catch (DeserializationException e) {
                        LEDSuiteApplication.getLogger().warn("Failed to handle menu reply! Deserialization failed: " + e.getMessage());
                        errorCode = e.getErrorCode();
                        errorMessage = e.getMessage();
                    }
                } else {
                    LEDSuiteApplication.getLogger().warn("Couldn't handle menu reply packet because animation menu manager is not available!", new LEDSuiteLogAreas.COMMUNICATION());
                    errorCode = ErrorCode.GenericClientError;
                }

                if (errorCode == null) {
                    LEDSuiteApplication.getLogger().info("Animation menu reply was handled in " + (System.currentTimeMillis() - start) + "ms!", new LEDSuiteLogAreas.YAML());
                } else {
                    String fileName = null;

                    try {
                        YamlConfiguration yaml = new YamlConfiguration();
                        yaml.loadFromString(menuYAML);
                        if (YamlTools.checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.FILENAME, yaml)) {
                            fileName = yaml.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.FILENAME);
                        }
                    } catch (InvalidConfigurationException ex) {
                        LEDSuiteApplication.getLogger().warn("Failed to get file name for error reporting menu deserialization failure!", new LEDSuiteLogAreas.COMMUNICATION());
                    }

                    LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                            MenuErrorPacket.builder()
                                    .code(errorCode)
                                    .fileName(fileName)
                                    .severity(Severity.SEVERE)
                                    .message(errorMessage)
                                    .build().serialize()
                    );
                }
                LEDSuiteApplication.getWindow().animationList.setSensitive(true);
            }
        }.runTaskAsynchronously();
    }
}