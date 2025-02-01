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
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.tools.ExceptionTools;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.animation_menu.AnimationMenu;
import com.toxicstoxm.YAJL.Logger;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import lombok.*;
import org.gnome.glib.GLib;

/**
 * <p>Represents a reply packet containing an animation menu. This packet is typically sent in response to a menu request,
 * and it contains a serialized representation of the menu, typically in YAML format.</p>
 * <p>This packet is used to handle the deserialization and display of animation menus in the user interface.</p>
 *
 * @since 1.0.0
 * @see MenuRequestPacket
 * @see MenuErrorPacket
 */
@AllArgsConstructor
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@NoArgsConstructor
@Setter
public class MenuReplyPacket extends CommunicationPacket {

    private static final Logger logger = Logger.autoConfigureLogger();

    @Override
    public Logger getLogger() {
        return logger;
    }

    /**
     * A serialized YAML string representing the animation menu.
     * This YAML content defines the structure and contents of the menu that should be displayed.
     */
    private String menuYAML;

    /**
     * Returns the type of the packet as defined in the communication protocol.
     *
     * @return the type of the packet, which is {@link Constants.Communication.YAML.Values.General.PacketTypes#REPLY}.
     */
    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REPLY;
    }

    /**
     * Returns the subtype of the packet, which indicates that this is a menu reply packet.
     *
     * @return the subtype of the packet, which is {@link Constants.Communication.YAML.Values.Reply.Types#MENU}.
     */
    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Reply.Types.MENU;
    }

    /**
     * Serializes the contents of this packet into a YAML string.
     *
     * @return a YAML string representing the contents of this {@link MenuReplyPacket}.
     */
    @Override
    public String serialize() {
        super.serialize();
        yaml.set(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT, menuYAML);
        return yaml.saveToString();
    }

    /**
     * Deserializes the given YAML string into a {@link MenuReplyPacket}.
     *
     * @param yamlString the serialized YAML string representing the menu.
     * @return the deserialized {@link MenuReplyPacket}.
     * @throws DeserializationException if there is an error during deserialization.
     */
    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        MenuReplyPacket packet = MenuReplyPacket.builder().build();
        packet.menuYAML = yamlString;
        return packet;
    }

    private static boolean lock = false;

    /**
     * Handles the packet by deserializing the animation menu and displaying it in the user interface.
     * <p>If another menu reply is already being processed, the packet will be ignored to prevent concurrency issues.</p>
     * <p>If an error occurs during deserialization or while displaying the menu, an error report is sent back to the server.</p>
     */
    @Override
    public void handlePacket() {
        if (lock) {
            logger.warn("Voiding menu reply because another one is currently being handled!");
            return;
        }
        lock = true;

        long start = System.currentTimeMillis();
        GLib.idleAddOnce(() -> LEDSuiteApplication.getWindow().setAnimationListSensitive(false));

        new LEDSuiteRunnable() {
            @Override
            public void run() {

                ErrorCode errorCode = null;
                String errorMessage = null;

                var animationMenuManager = LEDSuiteApplication.getAnimationMenuManager();

                if (animationMenuManager != null) {
                    try {
                        AnimationMenu menu = animationMenuManager.deserializeAnimationMenu(menuYAML);
                        GLib.idleAddOnce(() -> LEDSuiteApplication.getWindow().displayAnimationMenu(menu));
                    } catch (DeserializationException e) {
                        logger.warn("Failed to handle menu reply! Deserialization failed: " + e.getMessage());
                        errorCode = e.getErrorCode();
                        errorMessage = e.getMessage();
                        e.printStackTrace(message -> logger.stacktrace(message));
                    }
                } else {
                    logger.warn("Couldn't handle menu reply packet because animation menu manager is not available!");
                    errorCode = ErrorCode.GenericClientError;
                }

                if (errorCode == null) {
                    logger.debug("Animation menu reply was handled in " + (System.currentTimeMillis() - start) + "ms!");
                } else {
                    String fileName = null;

                    try {
                        YamlConfiguration yaml = new YamlConfiguration();
                        yaml.loadFromString(menuYAML);
                        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.FILENAME, yaml)) {
                            fileName = yaml.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.FILENAME);
                        }
                    } catch (InvalidConfigurationException ex) {
                        logger.warn("Failed to get file name for error reporting menu deserialization failure!");
                        ExceptionTools.printStackTrace(ex, message -> logger.stacktrace(message));
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
                GLib.idleAddOnce(() -> LEDSuiteApplication.getWindow().setAnimationListSensitive(true));
                lock = false;
            }
        }.runTaskAsynchronously();
    }
}
