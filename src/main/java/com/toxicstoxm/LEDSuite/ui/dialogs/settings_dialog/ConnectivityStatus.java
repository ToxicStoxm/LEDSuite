package com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog;

/**
 * Wrapper for connectivity related methods.
 * Used in {@link SettingsDialog}.
 * @since 1.0.0
 */
public interface ConnectivityStatus {
    void connected();
    void disconnected();
    void disconnecting();
    void connecting();
}
