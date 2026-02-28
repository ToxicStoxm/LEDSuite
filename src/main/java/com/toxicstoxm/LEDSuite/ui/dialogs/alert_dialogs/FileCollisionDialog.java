package com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs;

import com.toxicstoxm.YAJL.core.Logger;
import com.toxicstoxm.YAJL.core.LoggerManager;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import org.gnome.adw.AlertDialog;

/**
 * A dialog used to handle file collision scenarios in the LEDSuite application.
 * <p>
 * This dialog is presented when there is a file collision, allowing the user to decide how to proceed.
 * The dialog is customized with a UI template defined in {@code FileCollisionDialog.ui} and provides
 * a response callback for handling user interaction.
 * </p>
 *
 * @since 1.0.0
 */
@GtkTemplate(name = "FileCollisionDialog", ui = "/com/toxicstoxm/LEDSuite/FileCollisionDialog.ui")
public class FileCollisionDialog extends AlertDialog {
    private static final Logger logger = LoggerManager.getLogger(FileCollisionDialog.class);

    // Callback to handle user response actions
    private ResponseCallback responseCallback;

    /**
     * Sets the callback to be executed when a response is received from the dialog.
     *
     * @param responseCallback the callback to be executed on response
     */
    public void onResponse(ResponseCallback responseCallback) {
        this.responseCallback = responseCallback;
    }

    /**
     * Handles the response triggered by the user in the dialog.
     * This method is called when the user interacts with the dialog and selects a response.
     * It runs the configured {@link ResponseCallback} if it is set.
     *
     * @param response the response selected by the user (e.g., "accept", "cancel")
     */
    @Override
    protected void response(String response) {
        logger.verbose("Received response -> '{}'", response);
        if (responseCallback != null) {
            logger.verbose("Calling response handler");
            responseCallback.run(response);
        } else {
            logger.warn("Ignoring response, no response handler found or specified!");
        }
    }
}
