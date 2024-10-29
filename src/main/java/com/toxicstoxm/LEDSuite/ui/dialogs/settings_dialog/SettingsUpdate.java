package com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog;

import lombok.Builder;

import java.util.Collection;

/**
 * Data structure for holding the data used to update the settings dialog.
 * @param brightness LED brightness
 * @param selectedColorMode currently selected color mode. Example RGB
 * @param supportedColorModes list of supported color modes
 * @since 1.0.0
 */
@Builder
public record SettingsUpdate(Integer brightness, Integer selectedColorMode, Collection<String> supportedColorModes) {
}
