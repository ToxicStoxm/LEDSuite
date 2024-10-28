package com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog;

import lombok.Builder;

import java.util.Collection;

@Builder
public record SettingsUpdate(Integer brightness, Integer selectedColorMode, Collection<String> supportedColorModes) {
}
