package com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.message.ServerMessagePacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.ServerMessageResponseRequestPacket;
import com.toxicstoxm.LEDSuite.gettext.Translations;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.YAJL.Logger;
import lombok.Builder;
import org.gnome.glib.GLib;
import org.gnome.gtk.Widget;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Message dialog which is used to display server messages to the user.
 * @since 1.0.0
 */
public class ServerMessageDialog {

    private static final Logger logger = Logger.autoConfigureLogger();

    private final GeneralAlertDialog alertDialog;

    @Builder
    public ServerMessageDialog(@NotNull ServerMessagePacket packet) {
        logger.verbose("Creating server message dialog from message packet -> {}", packet);
        alertDialog = new GeneralAlertDialog();

        List<AlertDialogResponse> responses = packet.getResponses();

        if (packet.getNoResponseHandlerID() != null) {
            responses.add(
                    AlertDialogResponse.builder()
                            .id(alertDialog.getCloseResponse())
                            .responseCallback(() -> {
                                LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                                        ServerMessageResponseRequestPacket.builder()
                                                .responseID(packet.getNoResponseHandlerID())
                                                .build().serialize()
                                );
                            })
                            .build()
            );
        }

        alertDialog.configure(
                AlertDialogData.builder()
                        .heading(generateHeading(packet.getHeading(), packet.getSource()))
                        .body(packet.getMessage())
                        .responses(responses)
                        .build()
        );
    }

    /**
     * Generates a heading by combining the two provided strings.
     * @param heading the message heading
     * @param source the message source / sender
     * @return the generated heading
     */
    private @NotNull String generateHeading(String heading, String source) {
        logger.verbose("Generating message dialog heading from -> '{}' and '{}'", heading, source);
        StringBuilder sb = new StringBuilder();
        if (source != null && !source.isBlank()) {
            sb.append(source).append(" - ");
        }
        if (heading != null && !heading.isBlank()) {
            sb.append(heading);
        } else sb.append(Translations.getText("Server Message"));

        logger.verbose("Generated message dialog heading -> '{}'", sb.toString());
        return sb.toString();
    }

    public void present(Widget parent) {
        GLib.idleAddOnce(() -> alertDialog.present(parent));
    }
}
