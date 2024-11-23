package com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog;

import lombok.Builder;

import java.util.Collection;

/**
 * Data structure for holding the data used to update the settings dialog.
 * This record holds the necessary settings information that is used to update the user interface
 * in the settings dialog, including brightness, color mode, supported color modes, and the state restoration flag.
 *
 * @param brightness The LED brightness level. Ranges from 0 to 100.
 * @param selectedColorMode The currently selected color mode (e.g., RGB, HSV). This is an integer value that maps to the selected mode.
 * @param supportedColorModes A collection of color modes supported by the system (e.g., RGB, HSV, etc.).
 * @param restorePreviousState A boolean flag indicating whether the server should restore its previous state after a reboot.
 * @since 1.0.0
 */
@Builder
public record SettingsUpdate(
        Integer brightness,
        Integer selectedColorMode,
        Collection<String> supportedColorModes,
        Boolean restorePreviousState
) {
}
