package com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.message.ServerMessagePacket;
import com.toxicstoxm.LEDSuite.gettext.Translations;
import lombok.Builder;
import org.gnome.glib.GLib;
import org.gnome.gtk.Widget;
import org.jetbrains.annotations.NotNull;

/**
 * Message dialog which is used to display server messages to the user.
 * @since 1.0.0
 */
public class ServerMessageDialog {

    private final AlertDialog<AlertDialogData> alertDialog;

    @Builder
    public ServerMessageDialog(@NotNull ServerMessagePacket packet) {
        alertDialog = GeneralAlertDialog.create().configure(
                AlertDialogData.builder()
                        .heading(generateHeading(packet.getHeading(), packet.getSource()))
                        .body(packet.getMessage())
                        .responses(packet.getResponses())
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
        StringBuilder sb = new StringBuilder();
        if (source != null && !source.isBlank()) {
            sb.append(source).append(" - ");
        }
        if (heading != null && !heading.isBlank()) {
            sb.append(heading);
        } else sb.append(Translations.getText("Server Message"));

        return sb.toString();
    }

    public void present(Widget parent) {
        GLib.idleAddOnce(() -> alertDialog.present(parent));
    }
}