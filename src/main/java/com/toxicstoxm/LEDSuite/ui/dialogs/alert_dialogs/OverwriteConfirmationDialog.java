package com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs;

import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import org.gnome.adw.AlertDialog;
import org.gnome.glib.Type;
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

    // Register the GtkTemplate type for the dialog
    private static final Type gtype = TemplateTypes.register(OverwriteConfirmationDialog.class);

    /**
     * Constructs a new instance of the overwrite confirmation dialog using the provided memory address.
     *
     * @param address the memory segment address used to create the dialog instance
     */
    public OverwriteConfirmationDialog(MemorySegment address) {
        super(address);
    }

    /**
     * Retrieves the GtkType for this dialog.
     *
     * @return the type associated with the OverwriteConfirmationDialog
     */
    public static Type getType() {
        return gtype;
    }

    /**
     * Creates and returns a new instance of the OverwriteConfirmationDialog.
     *
     * @return a new OverwriteConfirmationDialog instance
     */
    public static OverwriteConfirmationDialog create() {
        return GObject.newInstance(getType());
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
        if (responseCallback != null) {
            responseCallback.run(response);
        }
    }
}
