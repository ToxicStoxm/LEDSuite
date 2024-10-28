package com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog;

import lombok.Builder;

@Builder
public record SettingsData(Integer brightness, String selectedColorMode) {
}
