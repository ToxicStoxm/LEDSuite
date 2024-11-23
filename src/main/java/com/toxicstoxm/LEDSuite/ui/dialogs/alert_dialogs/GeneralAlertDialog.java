package com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs;

import com.toxicstoxm.LEDSuite.time.Action;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import org.gnome.adw.AlertDialog;
import org.gnome.adw.ResponseAppearance;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.jetbrains.annotations.NotNull;

import java.lang.foreign.MemorySegment;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
public class GeneralAlertDialog extends AlertDialog implements com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs.AlertDialog<AlertDialogData> {

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
    
    private final HashMap<String, Action> registeredResponses = new HashMap<>();

    @Override
    public com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs.AlertDialog<AlertDialogData> configure(@NotNull AlertDialogData data) {
        String heading = data.heading();
        if (heading != null) setHeading(heading);

        String body = data.body();
        if (body != null) setBody(body);

        List<AlertDialogResponse> responses = data.responses();

        if (responses != null) {
            ResponseCallback responseCb = response -> {

                if (registeredResponses.containsKey(response)) {
                    registeredResponses.get(response).run();
                }

            };

            for (AlertDialogResponse response : data.responses()) {
                String label = response.label();
                if (label != null) {
                    String id = Objects.requireNonNullElse(response.id(), String.valueOf(UUID.randomUUID()));
                    Action cb = Objects.requireNonNullElse(response.responseCallback(), () -> {});
                    ResponseAppearance appearance = Objects.requireNonNullElse(response.appearance(), ResponseAppearance.DEFAULT);

                    registeredResponses.put(id, cb);
                    addResponse(id, response.label());
                    setResponseAppearance(id, appearance);
                    setResponseEnabled(id, response.activated());
                }
            }

            onResponse(responseCb);
        }
        return this;
    }
}
