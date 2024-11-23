package com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs;

import lombok.Builder;

/**
 * A data class representing the content and responses for an alert dialog.
 * <p>
 * This record class encapsulates the data required to configure an alert dialog,
 * including the heading, body, and possible responses that the dialog can have.
 * It is typically used to initialize and customize alert dialogs in the user interface.
 * </p>
 *
 * @param heading the heading or title of the alert dialog
 * @param body the body or message content of the alert dialog
 * @param responses the set of possible responses that the alert dialog can have, used to specify buttons or actions
 * @since 1.0.0
 */
@Builder
public record AlertDialogData(
        String heading,
        String body,
        AlertDialogResponse... responses
) {}
