package com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs;

import com.toxicstoxm.LEDSuite.time.Action;
import com.toxicstoxm.YAJL.Logger;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import org.gnome.adw.ResponseAppearance;
import org.jetbrains.annotations.NotNull;

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
public class GeneralAlertDialog extends org.gnome.adw.AlertDialog implements AlertDialog<AlertDialogData> {

    private static final Logger logger = Logger.autoConfigureLogger();

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
        logger.verbose("Received response -> '{}'", response);
        if (responseCallback != null) {
            logger.verbose("Calling response handler");
            responseCallback.run(response);
        } else {
            logger.warn("Ignoring response, no response handler found or specified!");
        }
    }
    
    private final HashMap<String, Action> registeredResponses = new HashMap<>();

    @Override
    public AlertDialog<AlertDialogData> configure(@NotNull AlertDialogData data) {
        logger.verbose("Configuring alert dialog -> '{}'", data);
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
                if (response != null) {
                    String label = response.label();
                    Action action = Objects.requireNonNullElse(response.responseCallback(), () -> {});
                    if (label != null) {
                        String id = Objects.requireNonNullElse(response.id(), String.valueOf(UUID.randomUUID()));
                        ResponseAppearance appearance = Objects.requireNonNullElse(response.appearance(), ResponseAppearance.DEFAULT);

                        registeredResponses.put(id, action);
                        addResponse(id, response.label());
                        setResponseAppearance(id, appearance);
                        setResponseEnabled(id, response.activated());
                    } else if (Objects.equals(response.id(), getCloseResponse())) {
                        registeredResponses.put(getCloseResponse(), action);
                    }
                }
            }

            onResponse(responseCb);
        }
        return this;
    }
}
