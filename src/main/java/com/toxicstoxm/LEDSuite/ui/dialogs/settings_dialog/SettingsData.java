package com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog;

import lombok.Builder;

/**
 * Data structure for holding the data provided by the settings dialog.
 * @param brightness LED brightness
 * @param selectedColorMode currently selected color mode. Example RGB
 * @param restorePreviousState if the server should restore it's previous state after a reboot
 * @since 1.0.0
 */
@Builder
public record SettingsData(Integer brightness, String selectedColorMode, Boolean restorePreviousState) {
}
