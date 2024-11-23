package com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs;

import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import org.gnome.adw.AlertDialog;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;

import java.lang.foreign.MemorySegment;

/**
 * A general-purpose alert dialog used to display messages and handle user responses in the LEDSuite application.
 * <p>
 * This dialog can be used to display simple messages to the user with customizable response options.
 * It uses a Gtk template, defined in {@code GeneralAlertDialog.ui}, for the layout of the dialog.
 * The dialog allows specifying a callback to handle user responses.
 * </p>
 *
 * @since 1.0.0
 */
@GtkTemplate(name = "GeneralAlertDialog", ui = "/com/toxicstoxm/LEDSuite/GeneralAlertDialog.ui")
public class GeneralAlertDialog extends AlertDialog {

    // Register the GtkTemplate type for the dialog
    private static final Type gtype = TemplateTypes.register(GeneralAlertDialog.class);

    /**
     * Constructs a new instance of the dialog, using the provided memory address.
     *
     * @param address the memory segment address used for creating the dialog instance
     */
    public GeneralAlertDialog(MemorySegment address) {
        super(address);
    }

    /**
     * Retrieves the GtkType for this dialog.
     *
     * @return the type associated with the GeneralAlertDialog
     */
    public static Type getType() {
        return gtype;
    }

    /**
     * Creates and returns a new instance of the GeneralAlertDialog.
     *
     * @return a new GeneralAlertDialog instance
     */
    public static GeneralAlertDialog create() {
        return GObject.newInstance(getType());
    }

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
     * This method is invoked when the user interacts with the dialog and selects a response.
     * It will execute the configured {@link ResponseCallback} if it is set.
     *
     * @param response the response selected by the user (e.g., "accept", "cancel")
     */
    @Override
    protected void response(String response) {
        if (responseCallback != null) {
            responseCallback.run(response);
        }
    }
}
