package com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs;

import com.toxicstoxm.YAJL.Logger;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import org.gnome.adw.AlertDialog;
import org.gnome.gobject.GObject;

import java.lang.foreign.MemorySegment;

/**
 * A dialog that prompts the user for confirmation before overwriting data.
 * <p>
 * This dialog is used when the application needs to ask the user if they are sure about overwriting existing data.
 * It provides options to confirm or cancel the action. The dialog uses a Gtk template, defined in
 * {@code OverwriteConfirmationDialog.ui}, for the layout.
 * </p>
 *
 * @since 1.0.0
 */
@GtkTemplate(name = "OverwriteConfirmationDialog", ui = "/com/toxicstoxm/LEDSuite/OverwriteConfirmationDialog.ui")
public class OverwriteConfirmationDialog extends AlertDialog {

    private static final Logger logger = Logger.autoConfigureLogger();

    static {
        TemplateTypes.register(OverwriteConfirmationDialog.class);
    }

    /**
     * Constructs a new instance of the overwrite confirmation dialog using the provided memory address.
     *
     * @param address the memory segment address used to create the dialog instance
     */
    public OverwriteConfirmationDialog(MemorySegment address) {
        super(address);
    }

    /**
     * Creates and returns a new instance of the OverwriteConfirmationDialog.
     *
     * @return a new OverwriteConfirmationDialog instance
     */
    public static OverwriteConfirmationDialog create() {
        logger.verbose("Creating new overwrite confirmation dialog");
        return GObject.newInstance(OverwriteConfirmationDialog.class);
    }

    // Callback to handle user response actions
    private ResponseCallback responseCallback;

    /**
     * Sets the callback to be executed when a response is received from the dialog.
     *
     * @param responseCallback the callback to be executed when the user responds
     */
    public void onResponse(ResponseCallback responseCallback) {
        this.responseCallback = responseCallback;
    }

    /**
     * Handles the response triggered by the user in the dialog.
     * This method is invoked when the user interacts with the dialog and selects a response.
     * It will execute the configured {@link ResponseCallback} if it is set.
     *
     * @param response the response selected by the user (e.g., "confirm", "cancel")
     */
    @Override
    protected void response(String response) {
        logger.verbose("Received rename response -> '{}'", response);
        if (responseCallback != null) {
            logger.verbose("Calling response handler");
            responseCallback.run(response);
        } else {
            logger.warn("Ignoring response, no response handler found or specified!");
        }
    }
}
