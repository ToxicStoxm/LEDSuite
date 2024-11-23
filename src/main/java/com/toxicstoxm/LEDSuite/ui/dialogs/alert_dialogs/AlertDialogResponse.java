package com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs;

import com.toxicstoxm.LEDSuite.time.Action;
import lombok.Builder;
import org.gnome.adw.ResponseAppearance;

/**
 * A data class representing a response option for an alert dialog.
 * <p>
 * This record class encapsulates the data for a single response (e.g., button) within an alert dialog.
 * It defines the response's identifier, label (text), activation state, appearance, and the callback
 * to be executed when the response is triggered.
 * </p>
 *
 * @param id the unique identifier for this response (used to identify the response)
 * @param label the label or text displayed on the response button (e.g., "OK", "Cancel")
 * @param activated a flag indicating whether the response is activated or not (can be used for enabling/disabling)
 * @param appearance the appearance of the response button, controlling its visual style (e.g., destructive, positive)
 * @param responseCallback the callback action to be executed when the response is triggered
 * @since 1.0.0
 */
@Builder
public record AlertDialogResponse(
        String id,
        String label,
        boolean activated,
        ResponseAppearance appearance,
        Action responseCallback) {
}
