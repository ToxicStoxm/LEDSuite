package com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog;

import lombok.Builder;

/**
 * Data structure for holding the settings data provided by the settings dialog.
 * <p>
 * This record is used to store the settings related to the LED configuration
 * such as brightness, selected color mode, and whether the server should
 * restore its previous state after a reboot. It is typically used in the
 * context of managing LED configurations via the settings dialog in the application.
 * </p>
 *
 * @param brightness The brightness level of the LED.
 * @param selectedColorMode The currently selected color mode (e.g., RGB).
 * @param restorePreviousState Flag indicating whether the server should restore its previous state after a reboot.
 * @since 1.0.0
 */
@Builder
public record SettingsData(
        Integer brightness,
        String selectedColorMode,
        Boolean restorePreviousState
) {}
