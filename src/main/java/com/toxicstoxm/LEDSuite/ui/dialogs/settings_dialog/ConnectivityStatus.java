package com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog;

import org.jetbrains.annotations.NotNull;

/**
 * Wrapper for connectivity related methods.
 * Used in {@link SettingsDialog}.
 * @since 1.0.0
 */
public interface ConnectivityStatus {
    void setServerState(@NotNull ServerState serverState);
}
