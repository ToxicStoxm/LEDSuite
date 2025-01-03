package com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs;

import com.toxicstoxm.LEDSuite.gettext.Translations;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import lombok.Builder;
import org.gnome.adw.ResponseAppearance;
import org.gnome.glib.GLib;
import org.gnome.gtk.Widget;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Message dialog which is used to display client messages to the user.
 * @since 1.0.0
 */
public class MessageDialog {

    private final AlertDialog<AlertDialogData> alertDialog;
    private static final List<AlertDialogResponse> responses;

    static {
        responses = new ArrayList<>();

        responses.add(
                AlertDialogResponse.builder()
                        .id(String.valueOf(UUID.randomUUID()))
                        .label(Translations.getText("Ok"))
                        .activated(true)
                        .appearance(ResponseAppearance.SUGGESTED)
                        .responseCallback(() -> LEDSuiteApplication.getLogger().debug("User acknowledged notification dialog."))
                        .build()
        );
    }


    @Builder
    public MessageDialog(@NotNull MessageData messageData) {
        alertDialog = GeneralAlertDialog.create().configure(
                AlertDialogData.builder()
                        .heading(generateHeading(messageData.heading(), messageData.source()))
                        .body(messageData.message())
                        .responses(computeResponses(messageData.responses()))
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
        } else sb.append(Translations.getText("Client Message"));

        return sb.toString();
    }

    private List<AlertDialogResponse> computeResponses(List<AlertDialogResponse> responses) {
        if (responses != null && !responses.isEmpty()) return responses;
        return MessageDialog.responses;
    }

    public void present(Widget parent) {
        GLib.idleAddOnce(() -> alertDialog.present(parent));
    }
}
