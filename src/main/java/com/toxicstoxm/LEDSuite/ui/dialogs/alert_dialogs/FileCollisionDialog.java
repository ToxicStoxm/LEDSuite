package com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs;

import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import org.gnome.adw.AlertDialog;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;

import java.lang.foreign.MemorySegment;

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

    // Register the GtkTemplate type for the dialog
    private static final Type gtype = TemplateTypes.register(FileCollisionDialog.class);

    /**
     * Constructs a new instance of the dialog, using the provided memory address.
     *
     * @param address the memory segment address used for creating the dialog instance
     */
    public FileCollisionDialog(MemorySegment address) {
        super(address);
    }

    /**
     * Retrieves the GtkType for this dialog.
     *
     * @return the type associated with the FileCollisionDialog
     */
    public static Type getType() {
        return gtype;
    }

    /**
     * Creates a new instance of the FileCollisionDialog.
     *
     * @return a new FileCollisionDialog instance
     */
    public static FileCollisionDialog create() {
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
     * This method is called when the user interacts with the dialog and selects a response.
     * It runs the configured {@link ResponseCallback} if it is set.
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
