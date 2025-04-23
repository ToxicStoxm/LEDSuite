package com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs;

import com.toxicstoxm.LEDSuite.gettext.Translations;
import com.toxicstoxm.YAJL.Logger;
import lombok.Builder;
import org.gnome.adw.ResponseAppearance;
import org.gnome.glib.GLib;
import org.gnome.gtk.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Message dialog which is used to display client messages to the user.
 * @since 1.0.0
 */
public class MessageDialog {

    private static final Logger logger = Logger.autoConfigureLogger();

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
                        .responseCallback(() -> logger.debug("User acknowledged notification dialog"))
                        .build()
        );
    }


    @Builder
    public MessageDialog(@NotNull MessageData messageData) {
        logger.verbose("Creating new message dialog from message data -> '{}'", messageData);
        alertDialog = new GeneralAlertDialog().configure(
                AlertDialogData.builder()
                        .heading(generateHeading(messageData.heading(), messageData.source()))
                        .body(messageData.message())
                        .responses(providedOrDefaultResponses(messageData.responses()))
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
        } else sb.append(Translations.getText("Client Message"));

        logger.verbose("Generated message dialog heading -> '{}'", sb.toString());
        return sb.toString();
    }

    /**
     * Checks if responses are provided, if not returns the default responses.
     * @param responses response handlers to use, or {@code null} to use the default ones
     * @return the response handlers to use to handle responses from the user via the dialog
     */
    private List<AlertDialogResponse> providedOrDefaultResponses(@Nullable List<AlertDialogResponse> responses) {
        if (responses != null && !responses.isEmpty()) {
            logger.verbose("Custom responses provided");
            return responses;
        }
        logger.verbose("Using default responses");
        return MessageDialog.responses;
    }

    public void present(Widget parent) {
        logger.verbose("Received display request");
        GLib.idleAddOnce(() -> alertDialog.present(parent));
    }
}
